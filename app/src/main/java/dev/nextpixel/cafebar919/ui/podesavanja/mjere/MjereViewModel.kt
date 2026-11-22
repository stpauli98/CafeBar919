package dev.nextpixel.cafebar919.ui.podesavanja.mjere

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.repo.MjeraPrikaz
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

const val GRESKA_MJERA_POSTOJI = "Ta mjera već postoji."
const val GRESKA_LITRE = "Litre moraju biti od 0,01 do 5,00."
const val GRESKA_OBAVEZNO = "Obavezno polje."
private const val GRESKA_NEUSPJELO = "Čuvanje nije uspjelo."
private const val PORUKA_U_UPOTREBI = "Mjera koju koristi neko piće ne može se isključiti dok se ne skine sa tog pića."

enum class PoljeMjere { NAZIV, NATPIS, U_ZBIRU, LITARA }

/** Sadržaj prozora „Nova mjera“ / „Izmjena · {naziv}“. Litre se drže kao tekst (zarez ili tačka). */
data class MjeraForma(
    val id: Long?,
    val naziv: String = "",
    val natpis: String = "",
    val uZbiru: String = "",
    val litara: String = "",
    val pocetna: Pocetna = Pocetna(),
    val greske: Map<PoljeMjere, String> = emptyMap(),
    /** Odbijeno čuvanje koje ne pripada nijednom polju; prikazuje se uz dugmad. */
    val greska: String? = null,
    val potvrdaOdbacivanja: Boolean = false,
    val upis: Boolean = false,
) {
    data class Pocetna(val naziv: String = "", val natpis: String = "", val uZbiru: String = "", val litara: String = "")

    val izmijenjena: Boolean
        get() = Validacija.ocisti(naziv) != Validacija.ocisti(pocetna.naziv) ||
            Validacija.ocisti(natpis) != Validacija.ocisti(pocetna.natpis) ||
            Validacija.ocisti(uZbiru) != Validacija.ocisti(pocetna.uZbiru) ||
            litara.trim() != pocetna.litara.trim()
}

/** „KORISTE JE“: 0 → „nema pića“ (spec 7 #14), 1–2 imena spojena zarezom, više → „{n} pića“. */
internal fun koristeJe(imena: List<String>): String = when (imena.size) {
    0 -> "nema pića"
    1, 2 -> imena.joinToString(", ")
    else -> Format.brojac(imena.size, "piće", "pića", "pića")
}

class MjereViewModel(private val repo: PodesavanjaRepo) : ViewModel() {
    private val _prozor = MutableStateFlow<MjeraForma?>(null)
    private val _poruke = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** Sve mjere (aktivne i isključene) po redoslijedu; `null` dok se ne učita. */
    val lista: StateFlow<List<MjeraPrikaz>?> = repo.mjere.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val prozor: StateFlow<MjeraForma?> = _prozor.asStateFlow()
    val poruke: SharedFlow<String> = _poruke.asSharedFlow()

    fun prebaci(id: Long, aktivna: Boolean) {
        viewModelScope.launch {
            val ishod = repo.postaviAktivnaMjera(id, aktivna)
            if (ishod is Ishod.Odbijeno) {
                _poruke.emit(
                    when (ishod.razlog) {
                        Odbijanje.NAZIV_POSTOJI -> GRESKA_MJERA_POSTOJI
                        Odbijanje.U_UPOTREBI -> PORUKA_U_UPOTREBI
                        else -> GRESKA_NEUSPJELO
                    },
                )
            }
        }
    }

    // ---- prozor ----

    fun otvoriNovu() { _prozor.value = MjeraForma(id = null) }

    fun otvoriIzmjenu(id: Long) {
        val m = lista.value?.firstOrNull { it.m.id == id }?.m ?: return
        val litara = m.litara?.let { Format.litre(it) }.orEmpty()
        _prozor.value = MjeraForma(
            id = id,
            naziv = m.naziv,
            natpis = m.natpis,
            uZbiru = m.uZbiru,
            litara = litara,
            pocetna = MjeraForma.Pocetna(m.naziv, m.natpis, m.uZbiru, litara),
        )
    }

    fun promijeniNaziv(v: String) = izmijeni { it.copy(naziv = v).bezGreske(PoljeMjere.NAZIV) }
    fun promijeniNatpis(v: String) = izmijeni { it.copy(natpis = v).bezGreske(PoljeMjere.NATPIS) }
    fun promijeniUZbiru(v: String) = izmijeni { it.copy(uZbiru = v).bezGreske(PoljeMjere.U_ZBIRU) }
    fun promijeniLitre(v: String) = izmijeni { it.copy(litara = v).bezGreske(PoljeMjere.LITARA) }

    /** Greška polja se javlja kad se izađe iz njega (O6), ne dok se kuca. */
    fun poljeNapusteno(polje: PoljeMjere) = izmijeni { f ->
        val g = greska(f, polje)
        f.copy(greske = if (g != null) f.greske + (polje to g) else f.greske - polje)
    }

    fun sacuvaj() {
        val f = _prozor.value ?: return
        if (f.upis) return
        val greske = PoljeMjere.entries.mapNotNull { p -> greska(f, p)?.let { p to it } }.toMap()
        if (greske.isNotEmpty()) {
            _prozor.value = f.copy(greske = greske, greska = null)
            return
        }
        _prozor.value = f.copy(upis = true, greske = emptyMap(), greska = null)
        val litara = Validacija.parsirajLitre(f.litara)
        viewModelScope.launch {
            when (val ishod = repo.sacuvajMjeru(f.id, f.naziv, f.natpis, f.uZbiru, litara)) {
                Ishod.Ok -> {
                    _prozor.value = null
                    _poruke.emit("Sačuvano: ${Validacija.ocisti(f.naziv)}")
                }
                is Ishod.Odbijeno -> {
                    // Odbijanje se prikazuje u prozoru (toast bi bio iza scrima) i nikad se ne guta.
                    val polje: Pair<PoljeMjere, String>? = when (ishod.razlog) {
                        Odbijanje.NAZIV_POSTOJI -> PoljeMjere.NAZIV to GRESKA_MJERA_POSTOJI
                        Odbijanje.NAZIV_PRAZAN -> PoljeMjere.NAZIV to GRESKA_OBAVEZNO
                        Odbijanje.NATPIS_PRAZAN -> PoljeMjere.NATPIS to GRESKA_OBAVEZNO
                        Odbijanje.U_ZBIRU_PRAZNO -> PoljeMjere.U_ZBIRU to GRESKA_OBAVEZNO
                        Odbijanje.LITRE_VAN_OPSEGA -> PoljeMjere.LITARA to GRESKA_LITRE
                        else -> null
                    }
                    _prozor.update {
                        if (polje != null) it?.copy(upis = false, greske = it.greske + polje)
                        else it?.copy(upis = false, greska = GRESKA_NEUSPJELO)
                    }
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

    private fun izmijeni(promjena: (MjeraForma) -> MjeraForma) { _prozor.update { it?.let(promjena) } }

    private fun MjeraForma.bezGreske(p: PoljeMjere) = copy(greske = greske - p, greska = null)

    private fun greska(f: MjeraForma, polje: PoljeMjere): String? = when (polje) {
        PoljeMjere.NAZIV -> when {
            Validacija.ocisti(f.naziv).isEmpty() -> GRESKA_OBAVEZNO
            nazivZauzet(f) -> GRESKA_MJERA_POSTOJI
            else -> null
        }
        PoljeMjere.NATPIS -> if (Validacija.ocisti(f.natpis).isEmpty()) GRESKA_OBAVEZNO else null
        PoljeMjere.U_ZBIRU -> if (Validacija.ocisti(f.uZbiru).isEmpty()) GRESKA_OBAVEZNO else null
        // Prazno = komad (null); nečitljiv tekst (NaN) i vrijednost van 0,01–5,00 daju istu poruku.
        PoljeMjere.LITARA -> if (Validacija.provjeriLitre(Validacija.parsirajLitre(f.litara)) is Ishod.Odbijeno) GRESKA_LITRE else null
    }

    private fun nazivZauzet(f: MjeraForma): Boolean {
        val l = lista.value ?: return false
        val bilaAktivna = f.id == null || l.firstOrNull { it.m.id == f.id }?.m?.aktivna == true
        val kljuc = Validacija.kljucNaziva(f.naziv)
        return bilaAktivna && l.any { it.m.aktivna && it.m.id != f.id && Validacija.kljucNaziva(it.m.naziv) == kljuc }
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MjereViewModel(container.repo()) as T
    }
}
