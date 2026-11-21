package dev.nextpixel.cafebar919.ui.podesavanja.artikli

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.data.MjeraE
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.repo.ArtikalPrikaz
import dev.nextpixel.cafebar919.repo.KategorijaPrikaz
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val GRESKA_NAZIV_POSTOJI = "Taj naziv već postoji · ARTIKAL-01"
private const val NAJVISE_MJERA = 3

/** Sadržaj prozora V1.1/V1.2. `pocetna` služi za otkrivanje izmjena (potvrda pri izlasku, O6). */
data class ArtikalForma(
    val id: Long?,
    val naziv: String = "",
    val kategorijaId: Long? = null,
    val mjereIds: Set<Long> = emptySet(),
    val pocetna: Pocetna = Pocetna(),
    val greskaNaziv: String? = null,
    /** V1.2: „Promjena važi od sljedeće ture…“, samo kad artikal ima unose u aktivnoj noći. */
    val napomena: String? = null,
    val potvrdaOdbacivanja: Boolean = false,
    val upis: Boolean = false,
) {
    data class Pocetna(val naziv: String = "", val kategorijaId: Long? = null, val mjereIds: Set<Long> = emptySet())

    val izmijenjena: Boolean
        get() = Validacija.ocisti(naziv) != Validacija.ocisti(pocetna.naziv) ||
            kategorijaId != pocetna.kategorijaId || mjereIds != pocetna.mjereIds

    val moguceSacuvati: Boolean
        get() = Validacija.ocisti(naziv).isNotEmpty() && kategorijaId != null && mjereIds.size in 1..NAJVISE_MJERA
}

class ArtikliViewModel(private val repo: PodesavanjaRepo) : ViewModel() {
    private val _filter = MutableStateFlow<Long?>(null)
    private val _prozor = MutableStateFlow<ArtikalForma?>(null)
    private val _isklj = MutableStateFlow<ArtikalPrikaz?>(null)
    private val _poruke = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** Svi artikli (aktivni i isključeni) po redoslijedu; `null` dok se ne učita. */
    private val svi: StateFlow<List<ArtikalPrikaz>?> =
        repo.artikli.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val kategorije: StateFlow<List<KategorijaPrikaz>> =
        repo.kategorije.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val mjere: StateFlow<List<MjeraE>> =
        repo.mjere.map { l -> l.map { it.m } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Filter po kategoriji; `null` = Sve. Isključena ili nepostojeća kategorija se tretira kao Sve. */
    val filter: StateFlow<Long?> =
        combine(_filter, kategorije) { f, kat -> f?.takeIf { id -> kat.any { it.k.id == id && it.k.aktivna } } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Artikli koje ekran crta (poslije filtera); `null` dok se ne učita. */
    val lista: StateFlow<List<ArtikalPrikaz>?> =
        combine(svi, filter) { s, f -> s?.let { l -> if (f == null) l else l.filter { it.kategorija.id == f } } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Broj aktivnih pića, uz „Sve“. */
    val brojAktivnih: StateFlow<Int> =
        svi.map { l -> l.orEmpty().count { it.a.aktivan } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val prozor: StateFlow<ArtikalForma?> = _prozor.asStateFlow()

    /** Artikal čije isključivanje čeka potvrdu. */
    val potvrdaIskljucivanja: StateFlow<ArtikalPrikaz?> = _isklj.asStateFlow()

    /** Kratke poruke za toast. */
    val poruke: SharedFlow<String> = _poruke.asSharedFlow()

    fun postaviFilter(kategorijaId: Long?) { _filter.value = kategorijaId }

    // ---- lista ----

    fun prebaci(id: Long, aktivan: Boolean) {
        val artikal = svi.value?.firstOrNull { it.a.id == id } ?: return
        if (!aktivan) {
            _isklj.value = artikal
            return
        }
        viewModelScope.launch {
            val ishod = repo.postaviAktivanArtikal(id, true)
            if (ishod is Ishod.Odbijeno) poruka(ishod.razlog, artikal)?.let { _poruke.emit(it) }
        }
    }

    fun potvrdiIskljucivanje() {
        val a = _isklj.value ?: return
        _isklj.value = null
        viewModelScope.launch { repo.postaviAktivanArtikal(a.a.id, false) }
    }

    fun odustaniOdIskljucivanja() { _isklj.value = null }

    /**
     * Pomjera red `od` na mjesto `na` (indeksi u prikazanoj listi) i upisuje redoslijed SVIH artikala.
     * Sa aktivnim filterom se miješaju samo prikazani; ostali zadržavaju relativna mjesta.
     */
    fun pomjeri(od: Int, na: Int) {
        val svi = svi.value ?: return
        val prikazani = lista.value ?: return
        if (od == na || od !in prikazani.indices || na !in prikazani.indices) return
        val novi = prikazani.map { it.a.id }.toMutableList().apply { add(na, removeAt(od)) }
        val prikazaniIds = prikazani.map { it.a.id }.toSet()
        val sljedeci = novi.iterator()
        val sviIds = svi.map { if (it.a.id in prikazaniIds) sljedeci.next() else it.a.id }
        viewModelScope.launch { repo.redoslijedArtikala(sviIds) }
    }

    // ---- prozor ----

    fun otvoriNovi() { _prozor.value = ArtikalForma(id = null) }

    fun otvoriIzmjenu(id: Long) {
        val p = svi.value?.firstOrNull { it.a.id == id } ?: return
        val mjereIds = p.mjere.map { it.id }.toSet()
        _prozor.value = ArtikalForma(
            id = id,
            naziv = p.a.naziv,
            kategorijaId = p.a.kategorijaId,
            mjereIds = mjereIds,
            pocetna = ArtikalForma.Pocetna(p.a.naziv, p.a.kategorijaId, mjereIds),
        )
        viewModelScope.launch {
            val unosi = repo.unosiUAktivnojNoci(id)
            if (unosi.isNotEmpty()) {
                val tekst = "Promjena važi od sljedeće ture. Ove noći je već uneseno ${Format.zbirMjera(unosi)}, to ostaje kako je uneseno."
                _prozor.update { f -> if (f?.id == id) f.copy(napomena = tekst) else f }
            }
        }
    }

    fun promijeniNaziv(naziv: String) = izmijeni { it.copy(naziv = naziv, greskaNaziv = null) }

    fun izaberiKategoriju(id: Long) = izmijeni { it.copy(kategorijaId = id) }

    fun prebaciMjeru(id: Long) = izmijeni { f ->
        when {
            id in f.mjereIds -> f.copy(mjereIds = f.mjereIds - id)
            f.mjereIds.size >= NAJVISE_MJERA -> f
            else -> f.copy(mjereIds = f.mjereIds + id)
        }
    }

    /** Greška o duplikatu se javlja kad se izađe iz polja (O6), ne dok se kuca. */
    fun nazivNapusten() = izmijeni { f ->
        if (f.naziv.isBlank()) f else f.copy(greskaNaziv = if (nazivZauzet(f)) GRESKA_NAZIV_POSTOJI else null)
    }

    fun sacuvaj() {
        val f = _prozor.value ?: return
        if (f.upis) return
        if (!f.moguceSacuvati) return
        _prozor.value = f.copy(upis = true)
        val izabrane = mjere.value.filter { it.id in f.mjereIds }.map { it.id }
        viewModelScope.launch {
            when (val ishod = repo.sacuvajArtikal(f.id, f.naziv, f.kategorijaId, izabrane)) {
                Ishod.Ok -> {
                    _prozor.value = null
                    _poruke.emit("Sačuvano: ${Validacija.ocisti(f.naziv)}")
                }
                is Ishod.Odbijeno -> {
                    _prozor.update { it?.copy(upis = false) }
                    when (ishod.razlog) {
                        Odbijanje.NAZIV_POSTOJI -> _prozor.update { it?.copy(greskaNaziv = GRESKA_NAZIV_POSTOJI) }
                        else -> poruka(ishod.razlog, null, f)?.let { _poruke.emit(it) }
                    }
                }
            }
        }
    }

    /** „Odustani“, dodir na scrim i sistemsko nazad: sa izmjenama pita (O6), bez izmjena zatvara. */
    fun odustani() {
        val f = _prozor.value ?: return
        if (f.potvrdaOdbacivanja) return
        if (f.izmijenjena) _prozor.value = f.copy(potvrdaOdbacivanja = true) else _prozor.value = null
    }

    fun nastaviUnos() = izmijeni { it.copy(potvrdaOdbacivanja = false) }

    fun odbaci() { _prozor.value = null }

    private fun izmijeni(promjena: (ArtikalForma) -> ArtikalForma) { _prozor.update { it?.let(promjena) } }

    private fun nazivZauzet(f: ArtikalForma): Boolean {
        val l = svi.value ?: return false
        val bioAktivan = f.id == null || l.firstOrNull { it.a.id == f.id }?.a?.aktivan == true
        val kljuc = Validacija.kljucNaziva(f.naziv)
        return bioAktivan && l.any { it.a.aktivan && it.a.id != f.id && Validacija.kljucNaziva(it.a.naziv) == kljuc }
    }

    /** Toast za odbijeno uključivanje ili čuvanje; `null` kad razlog nema poruke. */
    private fun poruka(razlog: Odbijanje, artikal: ArtikalPrikaz?, forma: ArtikalForma? = null): String? = when (razlog) {
        Odbijanje.NAZIV_POSTOJI -> GRESKA_NAZIV_POSTOJI
        Odbijanje.KATEGORIJA_ISKLJUCENA -> {
            val id = artikal?.a?.kategorijaId ?: forma?.kategorijaId
            kategorije.value.firstOrNull { it.k.id == id }?.let { "Prvo uključi kategoriju ${it.k.naziv}." }
        }
        Odbijanje.MJERA_ISKLJUCENA -> {
            val ids = artikal?.mjere?.map { it.id }?.toSet() ?: forma?.mjereIds.orEmpty()
            mjere.value.firstOrNull { it.id in ids && !it.aktivna }?.let { "Prvo uključi mjeru ${it.naziv}." }
        }
        else -> null
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ArtikliViewModel(container.repo()) as T
    }
}
