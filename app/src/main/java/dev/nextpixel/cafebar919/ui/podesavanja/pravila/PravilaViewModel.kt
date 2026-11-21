package dev.nextpixel.cafebar919.ui.podesavanja.pravila

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.data.PostavkeE
import dev.nextpixel.cafebar919.data.RazlogE
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

const val GRESKA_RAZLOG_POSTOJI = "Taj razlog već postoji."
private const val GRESKA_OBAVEZNO = "Obavezno polje."
private const val GRESKA_NEUSPJELO = "Čuvanje nije uspjelo."

// Granice i koraci iz spec 7 #27.
const val ROK_MIN_S = 0
const val ROK_MAX_S = 300
const val ROK_KORAK_S = 10
const val ISTORIJA_MIN_DANA = 7
const val ISTORIJA_MAX_DANA = 90
const val ZAKLJUCAVANJE_MIN_S = 60
const val ZAKLJUCAVANJE_MAX_S = 600
const val ZAKLJUCAVANJE_KORAK_S = 60

/** Potvrda smanjenja istorije koje bi obrisalo `noci` zatvorenih noći starijih od `dana` dana (spec 7 #33). */
data class PotvrdaIstorije(val dana: Int, val noci: Int)

/** Sadržaj prozora „Novi razlog“. */
data class RazlogForma(val tekst: String = "", val greska: String? = null, val upis: Boolean = false)

class PravilaViewModel(private val repo: PodesavanjaRepo) : ViewModel() {
    private val _potvrda = MutableStateFlow<PotvrdaIstorije?>(null)
    private val _razlog = MutableStateFlow<RazlogForma?>(null)
    private val _poruke = MutableSharedFlow<String>(extraBufferCapacity = 8)

    /** `null` dok se ne učita. */
    val postavke: StateFlow<PostavkeE?> = repo.postavke.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val razlozi: StateFlow<List<RazlogE>?> = repo.razlozi.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val potvrda: StateFlow<PotvrdaIstorije?> = _potvrda.asStateFlow()
    val razlog: StateFlow<RazlogForma?> = _razlog.asStateFlow()
    val poruke: SharedFlow<String> = _poruke.asSharedFlow()

    // ---- steperi: svaki dodir se odmah čuva ----

    fun rokBezPina(smjer: Int) {
        val p = postavke.value ?: return
        sacuvaj { repo.postaviRokBezPina(p.rokBezPinaS + smjer * ROK_KORAK_S) }
    }

    fun zakljucavanje(smjer: Int) {
        val p = postavke.value ?: return
        sacuvaj { repo.postaviZakljucavanje(p.zakljucavanjeS + smjer * ZAKLJUCAVANJE_KORAK_S) }
    }

    /** Povećanje ide odmah; smanjenje koje bi obrisalo noći traži potvrdu, bez nje se vrijednost ne mijenja. */
    fun istorija(smjer: Int) {
        val nova = (postavke.value ?: return).istorijaDana + smjer
        if (smjer >= 0) {
            sacuvaj { repo.postaviIstoriju(nova) }
            return
        }
        viewModelScope.launch {
            val noci = repo.nociKojeBiNestale(nova)
            if (noci > 0) _potvrda.value = PotvrdaIstorije(nova, noci) else prikaziGresku(repo.postaviIstoriju(nova))
        }
    }

    fun smanjiIstoriju() {
        val p = _potvrda.value ?: return
        _potvrda.value = null
        sacuvaj { repo.postaviIstoriju(p.dana) }
    }

    fun nastaviUnos() { _potvrda.value = null }

    fun postaviIspomoc(dozvoljena: Boolean) {
        viewModelScope.launch { repo.postaviIspomoc(dozvoljena) }
    }

    private fun sacuvaj(upis: suspend () -> Ishod) {
        viewModelScope.launch { prikaziGresku(upis()) }
    }

    private suspend fun prikaziGresku(ishod: Ishod) {
        if (ishod is Ishod.Odbijeno) _poruke.emit(GRESKA_NEUSPJELO)
    }

    // ---- razlozi ----

    fun ukloniRazlog(id: Long) {
        viewModelScope.launch { repo.ukloniRazlog(id) }
    }

    fun otvoriRazlog() { _razlog.value = RazlogForma() }

    fun promijeniRazlog(tekst: String) { _razlog.update { it?.copy(tekst = tekst, greska = null) } }

    /** Greška se javlja kad se izađe iz polja (O6), ne dok se kuca. */
    fun razlogNapusten() { _razlog.update { f -> f?.copy(greska = greskaRazloga(f.tekst)) } }

    fun odustaniOdRazloga() { _razlog.value = null }

    fun dodajRazlog() {
        val f = _razlog.value ?: return
        if (f.upis) return
        val greska = greskaRazloga(f.tekst)
        if (greska != null) {
            _razlog.value = f.copy(greska = greska)
            return
        }
        _razlog.value = f.copy(upis = true, greska = null)
        viewModelScope.launch {
            when (val ishod = repo.dodajRazlog(f.tekst)) {
                Ishod.Ok -> _razlog.value = null
                // Odbijanje se prikazuje u prozoru i nikad se ne guta.
                is Ishod.Odbijeno -> _razlog.update {
                    it?.copy(
                        upis = false,
                        greska = when (ishod.razlog) {
                            Odbijanje.NAZIV_POSTOJI -> GRESKA_RAZLOG_POSTOJI
                            Odbijanje.NAZIV_PRAZAN -> GRESKA_OBAVEZNO
                            else -> GRESKA_NEUSPJELO
                        },
                    )
                }
            }
        }
    }

    private fun greskaRazloga(tekst: String): String? {
        if (Validacija.ocisti(tekst).isEmpty()) return GRESKA_OBAVEZNO
        val kljuc = Validacija.kljucNaziva(tekst)
        return if (razlozi.value?.any { Validacija.kljucNaziva(it.tekst) == kljuc } == true) GRESKA_RAZLOG_POSTOJI else null
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PravilaViewModel(container.repo()) as T
    }
}
