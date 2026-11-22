package dev.nextpixel.cafebar919.ui.podesavanja.ljudi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.data.RadnikE
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val GRESKA_RADNIK_POSTOJI = "Radnik sa tim imenom već postoji."
private const val GRESKA_OBAVEZNO = "Obavezno polje."
private const val GRESKA_NEUSPJELO = "Čuvanje nije uspjelo."

/** Opis uloge u redu (V2): izvodi se iz uloge. */
internal fun opisUloge(uloga: Uloga): String = when (uloga) {
    Uloga.KONOBAR -> "nosi piće gostima"
    Uloga.ZA_SANKOM -> "ne izlazi iz šanka, prodaje piće"
}

/** Sadržaj prozora „Novi radnik“ / „Izmjena · {ime}“. */
data class RadnikForma(
    val id: Long?,
    val ime: String = "",
    val uloga: Uloga = Uloga.KONOBAR,
    val pocetnoIme: String = "",
    val pocetnaUloga: Uloga = Uloga.KONOBAR,
    val greska: String? = null,
    val potvrdaOdbacivanja: Boolean = false,
    val upis: Boolean = false,
) {
    val izmijenjena: Boolean
        get() = Validacija.ocisti(ime) != Validacija.ocisti(pocetnoIme) || uloga != pocetnaUloga
}

class LjudiViewModel(private val repo: PodesavanjaRepo) : ViewModel() {
    private val _prozor = MutableStateFlow<RadnikForma?>(null)
    private val _iskljucivanje = MutableStateFlow<RadnikE?>(null)
    private val _poruke = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** Svi radnici (uključeni i isključeni) po redoslijedu; `null` dok se ne učita. */
    val lista: StateFlow<List<RadnikE>?> = repo.radnici.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val prozor: StateFlow<RadnikForma?> = _prozor.asStateFlow()

    /** Radnik za kojeg se traži potvrda isključivanja. */
    val iskljucivanje: StateFlow<RadnikE?> = _iskljucivanje.asStateFlow()
    val poruke: SharedFlow<String> = _poruke.asSharedFlow()

    /** Promjena uloge se čuva odmah. */
    fun postaviUlogu(id: Long, uloga: Uloga) {
        val r = lista.value?.firstOrNull { it.id == id } ?: return
        if (r.uloga == uloga) return
        viewModelScope.launch {
            val ishod = repo.sacuvajRadnika(id, r.ime, uloga)
            if (ishod is Ishod.Odbijeno) _poruke.emit(poruka(ishod.razlog))
        }
    }

    /** Uključivanje ide odmah; isključivanje traži potvrdu. */
    fun prebaci(id: Long, aktivan: Boolean) {
        val r = lista.value?.firstOrNull { it.id == id } ?: return
        if (!aktivan) {
            _iskljucivanje.value = r
            return
        }
        viewModelScope.launch {
            val ishod = repo.postaviAktivanRadnik(id, true)
            if (ishod is Ishod.Odbijeno) _poruke.emit(poruka(ishod.razlog))
        }
    }

    fun potvrdiIskljucivanje() {
        val r = _iskljucivanje.value ?: return
        _iskljucivanje.value = null
        viewModelScope.launch {
            val ishod = repo.postaviAktivanRadnik(r.id, false)
            if (ishod is Ishod.Odbijeno) _poruke.emit(poruka(ishod.razlog))
        }
    }

    fun odustaniOdIskljucivanja() { _iskljucivanje.value = null }

    // ---- prozor ----

    fun otvoriNovi() { _prozor.value = RadnikForma(id = null) }

    fun otvoriIzmjenu(id: Long) {
        val r = lista.value?.firstOrNull { it.id == id } ?: return
        _prozor.value = RadnikForma(id = id, ime = r.ime, uloga = r.uloga, pocetnoIme = r.ime, pocetnaUloga = r.uloga)
    }

    fun promijeniIme(v: String) = izmijeni { it.copy(ime = v, greska = null) }
    fun promijeniUlogu(u: Uloga) = izmijeni { it.copy(uloga = u) }

    /** Greška se javlja kad se izađe iz polja (O6), ne dok se kuca. */
    fun imeNapusteno() = izmijeni { it.copy(greska = greskaImena(it)) }

    fun sacuvaj() {
        val f = _prozor.value ?: return
        if (f.upis) return
        val greska = greskaImena(f)
        if (greska != null) {
            _prozor.value = f.copy(greska = greska)
            return
        }
        _prozor.value = f.copy(upis = true, greska = null)
        viewModelScope.launch {
            when (val ishod = repo.sacuvajRadnika(f.id, f.ime, f.uloga)) {
                Ishod.Ok -> {
                    _prozor.value = null
                    _poruke.emit("Sačuvano: ${Validacija.ocisti(f.ime)}")
                }
                // Odbijanje se prikazuje u prozoru (toast bi bio iza scrima) i nikad se ne guta.
                is Ishod.Odbijeno -> _prozor.update { it?.copy(upis = false, greska = poruka(ishod.razlog)) }
            }
        }
    }

    /** „Odustani“, scrim i sistemsko nazad: sa izmjenama pita (O6), bez izmjena zatvara. */
    fun odustani() {
        val f = _prozor.value ?: return
        if (f.potvrdaOdbacivanja) return
        if (f.izmijenjena) _prozor.value = f.copy(potvrdaOdbacivanja = true) else _prozor.value = null
    }

    fun nastaviUnos() = izmijeni { it.copy(potvrdaOdbacivanja = false) }

    fun odbaci() { _prozor.value = null }

    private fun izmijeni(promjena: (RadnikForma) -> RadnikForma) { _prozor.update { it?.let(promjena) } }

    private fun poruka(razlog: Odbijanje): String = when (razlog) {
        Odbijanje.NAZIV_POSTOJI -> GRESKA_RADNIK_POSTOJI
        Odbijanje.NAZIV_PRAZAN -> GRESKA_OBAVEZNO
        else -> GRESKA_NEUSPJELO
    }

    private fun greskaImena(f: RadnikForma): String? {
        if (Validacija.ocisti(f.ime).isEmpty()) return GRESKA_OBAVEZNO
        val l = lista.value ?: return null
        val kljuc = Validacija.kljucNaziva(f.ime)
        return if (l.any { it.aktivan && it.id != f.id && Validacija.kljucNaziva(it.ime) == kljuc }) GRESKA_RADNIK_POSTOJI else null
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LjudiViewModel(container.repo()) as T
    }
}
