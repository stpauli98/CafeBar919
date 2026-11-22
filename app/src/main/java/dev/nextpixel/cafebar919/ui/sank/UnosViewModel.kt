package dev.nextpixel.cafebar919.ui.sank

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.data.KategorijaE
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.NocPrimalacE
import dev.nextpixel.cafebar919.data.TuraSaStavkama
import dev.nextpixel.cafebar919.domain.Nacrt
import dev.nextpixel.cafebar919.domain.Pretraga
import dev.nextpixel.cafebar919.domain.StavkaNacrta
import dev.nextpixel.cafebar919.repo.ArtikalPrikaz
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.Potvrda
import dev.nextpixel.cafebar919.repo.TuraRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.util.UUID

private const val TAG = "CafeBar919"

/**
 * Stanje S1. `pica` je katalog filtriran kategorijom i upitom, `stavke` su samo važeće (piće i mjera još postoje),
 * `imaPica` znači da katalog nije prazan (inače T2), a `rokS` je rok za poništavanje bez PIN-a.
 */
data class UnosStanje(
    val noc: NocE,
    val primaoci: List<NocPrimalacE>,
    val zbirovi: Map<Long, String>,
    val kategorije: List<KategorijaE>,
    val izabranaKategorija: Long?,
    val upit: String,
    val pica: List<ArtikalPrikaz>,
    val imaPica: Boolean,
    val primalacId: Long?,
    val stavke: List<StavkaNacrta>,
    val upisuje: Boolean,
    val greskaUpisa: Boolean,
    val posljednje: List<TuraSaStavkama>,
    val rokS: Int,
)

sealed interface UnosDogadjaj {
    data class Upisana(val turaId: Long, val ime: String, val brojStavki: Int, val rokS: Int) : UnosDogadjaj
}

/**
 * S1: izbor primaoca, pića i potvrda ture.
 *
 * Nacrt u memoriji je istina; svaka izmjena se odmah upisuje u bazu pod istom bravom kao potvrda, a upis čita stanje
 * tek kad dobije bravu, pa stariji upis nikad ne pregazi noviji. Greške baze se hvataju (UPIS-01): pad upisa nacrta
 * ostavlja stanje u memoriji, a sljedeća izmjena ili potvrda upisuje ponovo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UnosViewModel(
    private val ture: TuraRepo,
    noc: NocRepo,
    @Suppress("UNUSED_PARAMETER") clock: Clock,
) : ViewModel() {
    private data class Tura(val primalacId: Long?, val stavke: List<StavkaNacrta>)
    private data class Filteri(val kategorija: Long?, val upit: String)
    private data class NocPodaci(val noc: NocE, val primaoci: List<NocPrimalacE>, val zbirovi: Map<Long, String>, val posljednje: List<TuraSaStavkama>)
    private data class Lokalno(val tura: Tura?, val filteri: Filteri, val upisuje: Boolean, val greska: Boolean)

    private val brava = Mutex()

    /** Ključ nacrta; poslije upisane ture postaje `noviKljuc`, inače bi sljedeća potvrda vratila staru turu. */
    private var kljuc: String? = null

    /** null dok se nacrt ne pročita; stavke su sirove (i one čije je piće u međuvremenu isključeno). */
    private val _tura = MutableStateFlow<Tura?>(null)
    private val _filteri = MutableStateFlow(Filteri(null, ""))
    private val _upisuje = MutableStateFlow(false)
    private val _greskaUpisa = MutableStateFlow(false)
    private val _dogadjaji = MutableSharedFlow<UnosDogadjaj>(extraBufferCapacity = 8)

    val dogadjaji: SharedFlow<UnosDogadjaj> = _dogadjaji.asSharedFlow()

    private val katalog: StateFlow<List<ArtikalPrikaz>?> = ture.katalog.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val nocPodaci: Flow<NocPodaci?> = noc.aktivna.distinctUntilChanged().flatMapLatest { n ->
        if (n == null) flowOf(null)
        else combine(noc.primaoci(n.id), ture.zbiroviPoPrimaocu(n.id), ture.posljednje(n.id)) { p, z, l -> NocPodaci(n, p, z, l) }
    }

    private val lokalno = combine(_tura, _filteri, _upisuje, _greskaUpisa) { t, f, u, g -> Lokalno(t, f, u, g) }

    val stanje: StateFlow<UnosStanje?> =
        combine(nocPodaci, katalog, ture.rokBezPina, lokalno) { np, kat, rok, l ->
            val t = l.tura
            if (np == null || kat == null || t == null) null else sastavi(np, kat, rok, l, t)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { ucitaj(noc) }
    }

    /** Čita nacrt; primalac koji nije primalac aktivne noći postaje null. */
    private suspend fun ucitaj(noc: NocRepo) {
        val n = try {
            ture.nacrt()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Bez nacrta iz baze počinje prazna tura sa novim ključem; prva izmjena ga upisuje.
            Log.e(TAG, "Nacrt nije pročitan", e)
            kljuc = UUID.randomUUID().toString()
            _tura.value = Tura(null, emptyList())
            return
        }
        // Ako se primaoci ne pročitaju, primaoca i dalje skriva `sastavi`, a potvrdu odbija repozitorij.
        val ids = try {
            noc.aktivna.first()?.let { a -> noc.primaoci(a.id).first().map { it.id } }.orEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Primaoci nisu pročitani", e)
            null
        }
        kljuc = n.kljuc
        _tura.value = Tura(n.primalacId?.takeIf { ids == null || it in ids }, Nacrt.izJsona(n.stavke))
    }

    private fun sastavi(np: NocPodaci, kat: List<ArtikalPrikaz>, rok: Int, l: Lokalno, tura: Tura): UnosStanje {
        val kategorije = kat.map { it.kategorija }.distinctBy { it.id }.sortedWith(compareBy({ it.redoslijed }, { it.id }))
        val izabrana = l.filteri.kategorija?.takeIf { id -> kategorije.any { it.id == id } }
        val pica = kat.filter { (izabrana == null || it.kategorija.id == izabrana) && Pretraga.odgovara(it.a.naziv, l.filteri.upit) }
        return UnosStanje(
            noc = np.noc,
            primaoci = np.primaoci,
            zbirovi = np.zbirovi,
            kategorije = kategorije,
            izabranaKategorija = izabrana,
            upit = l.filteri.upit,
            pica = pica,
            imaPica = kat.isNotEmpty(),
            primalacId = tura.primalacId?.takeIf { id -> np.primaoci.any { it.id == id } },
            stavke = TuraRepo.vazeceStavke(tura.stavke, kat),
            upisuje = l.upisuje,
            greskaUpisa = l.greska,
            posljednje = np.posljednje,
            rokS = rok,
        )
    }

    // ---- izmjene ture ----

    /** Dodir na izabranog primaoca ga odznačava. */
    fun izaberiPrimaoca(id: Long) {
        if (stanje.value?.primaoci?.any { it.id == id } != true) return
        izmijeni { it.copy(primalacId = if (it.primalacId == id) null else id) }
    }

    fun dodaj(artikalId: Long, mjeraId: Long) = promijeni(artikalId, mjeraId, 1)

    fun promijeni(artikalId: Long, mjeraId: Long, delta: Int) {
        val k = katalog.value ?: return
        if (k.none { p -> p.a.id == artikalId && p.mjere.any { it.id == mjeraId } }) return
        izmijeni { it.copy(stavke = Nacrt.promijeni(it.stavke, artikalId, mjeraId, delta)) }
    }

    /** Mijenja turu u memoriji i odmah upisuje nacrt; dok traje upis ture, izmjene se ignorišu. */
    private fun izmijeni(promjena: (Tura) -> Tura) {
        if (_upisuje.value) return
        val stara = _tura.value ?: return
        val nova = promjena(stara)
        if (nova == stara) return
        _tura.value = nova
        viewModelScope.launch { sacuvaj() }
    }

    private suspend fun sacuvaj() = brava.withLock {
        val t = _tura.value ?: return@withLock
        val k = kljuc ?: return@withLock
        try {
            ture.sacuvajNacrt(k, t.primalacId, t.stavke)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Stanje u memoriji ostaje istina; sljedeća izmjena ili potvrda upisuje ponovo.
            Log.e(TAG, "Nacrt nije sačuvan", e)
        }
    }

    // ---- filteri ----

    /** null = „Sve“. */
    fun izaberiKategoriju(id: Long?) {
        _filteri.value = _filteri.value.copy(kategorija = id)
    }

    /** Neprazan upit traži kroz sva pića, pa bira „Sve“ (odluka #35). */
    fun upit(s: String) {
        val f = _filteri.value
        _filteri.value = if (s.isNotBlank()) Filteri(null, s) else f.copy(upit = s)
    }

    // ---- potvrda i poništavanje ----

    /**
     * Upisuje turu sa ključem nacrta. Drugi dodir dok upis traje se ignoriše; poslije UPIS-01 sljedeći poziv
     * („Pokušaj ponovo“) šalje isti ključ, pa baza dobija najviše jednu turu.
     */
    fun potvrdi() {
        if (_upisuje.value) return
        if (_tura.value == null || stanje.value == null) return
        _upisuje.value = true
        viewModelScope.launch {
            try {
                brava.withLock { upisi() }
            } finally {
                _upisuje.value = false
            }
        }
    }

    private suspend fun upisi() {
        val t = _tura.value ?: return
        val k = kljuc ?: return
        val stavke = katalog.value?.let { TuraRepo.vazeceStavke(t.stavke, it) } ?: t.stavke
        val r = try {
            ture.potvrdi(k, t.primalacId, stavke)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Tura nije upisana", e)
            Potvrda.NijeUpisana
        }
        when (r) {
            is Potvrda.Upisana -> {
                kljuc = r.noviKljuc
                _tura.value = Tura(null, emptyList())
                _greskaUpisa.value = false
                _dogadjaji.emit(UnosDogadjaj.Upisana(r.turaId, r.ime, r.brojStavki, stanje.value?.rokS ?: 0))
            }
            Potvrda.NijeUpisana -> _greskaUpisa.value = true
            is Potvrda.Odbijena -> _greskaUpisa.value = false
        }
    }

    /** Poništava bez PIN-a; odbijanje (npr. rok istekao) i greška baze ne mijenjaju ništa. */
    fun ponisti(turaId: Long) {
        viewModelScope.launch {
            try {
                ture.ponistiBezPina(turaId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Tura nije poništena", e)
            }
        }
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            UnosViewModel(container.ture(), container.noc(), container.clock) as T
    }
}
