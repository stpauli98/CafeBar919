package dev.nextpixel.cafebar919.ui.podesavanja.kategorije

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.repo.KategorijaPrikaz
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

const val GRESKA_KATEGORIJA_POSTOJI = "Ta kategorija već postoji."
const val GRESKA_OBAVEZNO = "Obavezno polje."
private const val GRESKA_NEUSPJELO = "Čuvanje nije uspjelo."
private const val PORUKA_U_UPOTREBI = "Kategorija sa aktivnim pićima ne može se isključiti dok se pića ne premjeste ili isključe."

/** Sadržaj prozora „Nova kategorija“ / „Izmjena · {naziv}“. */
data class KategorijaForma(
    val id: Long?,
    val naziv: String = "",
    val pocetniNaziv: String = "",
    val greskaNaziv: String? = null,
    val potvrdaOdbacivanja: Boolean = false,
    val upis: Boolean = false,
) {
    val izmijenjena: Boolean get() = Validacija.ocisti(naziv) != Validacija.ocisti(pocetniNaziv)
}

class KategorijeViewModel(private val repo: PodesavanjaRepo) : ViewModel() {
    private val _prozor = MutableStateFlow<KategorijaForma?>(null)
    private val _poruke = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** Sve kategorije (aktivne i isključene) po redoslijedu; `null` dok se ne učita. */
    val lista: StateFlow<List<KategorijaPrikaz>?> = repo.kategorije.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val prozor: StateFlow<KategorijaForma?> = _prozor.asStateFlow()
    val poruke: SharedFlow<String> = _poruke.asSharedFlow()

    fun prebaci(id: Long, aktivna: Boolean) {
        viewModelScope.launch {
            val ishod = repo.postaviAktivnaKategorija(id, aktivna)
            if (ishod is Ishod.Odbijeno) {
                _poruke.emit(
                    when (ishod.razlog) {
                        Odbijanje.NAZIV_POSTOJI -> GRESKA_KATEGORIJA_POSTOJI
                        Odbijanje.U_UPOTREBI -> PORUKA_U_UPOTREBI
                        else -> GRESKA_NEUSPJELO
                    },
                )
            }
        }
    }

    /** Pomjera red `od` na mjesto `na` i upisuje redoslijed SVIH kategorija (i isključenih). */
    fun pomjeri(od: Int, na: Int) {
        val l = lista.value ?: return
        if (od == na || od !in l.indices || na !in l.indices) return
        val ids = l.map { it.k.id }.toMutableList().apply { add(na, removeAt(od)) }
        viewModelScope.launch { repo.redoslijedKategorija(ids) }
    }

    // ---- prozor ----

    fun otvoriNovu() { _prozor.value = KategorijaForma(id = null) }

    fun otvoriIzmjenu(id: Long) {
        val k = lista.value?.firstOrNull { it.k.id == id }?.k ?: return
        _prozor.value = KategorijaForma(id = id, naziv = k.naziv, pocetniNaziv = k.naziv)
    }

    fun promijeniNaziv(naziv: String) = izmijeni { it.copy(naziv = naziv, greskaNaziv = null) }

    /** Greška se javlja kad se izađe iz polja (O6), ne dok se kuca. */
    fun nazivNapusten() = izmijeni { it.copy(greskaNaziv = greskaNaziva(it)) }

    fun sacuvaj() {
        val f = _prozor.value ?: return
        if (f.upis) return
        val greska = greskaNaziva(f)
        if (greska != null) {
            _prozor.value = f.copy(greskaNaziv = greska)
            return
        }
        _prozor.value = f.copy(upis = true)
        viewModelScope.launch {
            when (val ishod = repo.sacuvajKategoriju(f.id, f.naziv)) {
                Ishod.Ok -> {
                    _prozor.value = null
                    _poruke.emit("Sačuvano: ${Validacija.ocisti(f.naziv)}")
                }
                is Ishod.Odbijeno -> {
                    // Odbijanje se prikazuje u prozoru (toast bi bio iza scrima) i nikad se ne guta.
                    val tekst = when (ishod.razlog) {
                        Odbijanje.NAZIV_POSTOJI -> GRESKA_KATEGORIJA_POSTOJI
                        Odbijanje.NAZIV_PRAZAN -> GRESKA_OBAVEZNO
                        else -> GRESKA_NEUSPJELO
                    }
                    _prozor.update { it?.copy(upis = false, greskaNaziv = tekst) }
                }
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

    private fun izmijeni(promjena: (KategorijaForma) -> KategorijaForma) { _prozor.update { it?.let(promjena) } }

    private fun greskaNaziva(f: KategorijaForma): String? {
        if (Validacija.ocisti(f.naziv).isEmpty()) return GRESKA_OBAVEZNO
        val l = lista.value ?: return null
        val bilaAktivna = f.id == null || l.firstOrNull { it.k.id == f.id }?.k?.aktivna == true
        val kljuc = Validacija.kljucNaziva(f.naziv)
        val zauzeto = bilaAktivna && l.any { it.k.aktivna && it.k.id != f.id && Validacija.kljucNaziva(it.k.naziv) == kljuc }
        return if (zauzeto) GRESKA_KATEGORIJA_POSTOJI else null
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = KategorijeViewModel(container.repo()) as T
    }
}
