package dev.nextpixel.cafebar919.ui.sank

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.data.RadnikE
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.LocalDate

const val GRESKA_ISPOMOC_PRAZNO = "Upiši ime."
const val GRESKA_ISPOMOC_POSTOJI = "To ime već postoji večeras."
const val GRESKA_ISPOMOC_PREDUGO = "Ime je predugo."

/** S0: ko radi večeras. `prozorIspomoci != null` znači da je S0.1 otvoren; vrijednost je upisano ime. */
data class NovaNocStanje(
    val danas: LocalDate,
    val konobari: List<RadnikE>,
    val zaSankom: List<RadnikE>,
    val oznaceni: Set<Long>,
    val ispomoc: List<Pair<String, Boolean>>,
    val ispomocDozvoljena: Boolean,
    val prozorIspomoci: String?,
    val greskaIspomoci: String?,
    val otvara: Boolean,
    /** Otvaranje nije uspjelo (baza); baner iznad dugmeta, čisti se pri sljedećem pokušaju. */
    val greskaOtvaranja: Boolean = false,
) {
    /** Označeni stalni radnici (samo prikazani) i označena ispomoć (samo kad je ispomoć dozvoljena). */
    val brojRadnika: Int
        get() = (konobari + zaSankom).count { it.id in oznaceni } +
            if (ispomocDozvoljena) ispomoc.count { it.second } else 0
}

class NovaNocViewModel(
    podesavanja: PodesavanjaRepo,
    private val noc: NocRepo,
    private val clock: Clock,
    private val poslijeOtvaranja: () -> Unit,
) : ViewModel() {
    private val _danas = MutableStateFlow(LocalDate.now(clock))
    private val _oznaceni = MutableStateFlow<Set<Long>>(emptySet())
    private val _ispomoc = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    private val _prozor = MutableStateFlow<String?>(null)
    private val _greska = MutableStateFlow<String?>(null)
    private val _otvara = MutableStateFlow(false)
    private val _greskaOtvaranja = MutableStateFlow(false)

    private val dio1 = combine(podesavanja.radnici, podesavanja.postavke, _oznaceni, _ispomoc, _danas) { radnici, postavke, oznaceni, ispomoc, danas ->
        val aktivni = radnici.filter { it.aktivan }
        NovaNocStanje(
            danas = danas,
            konobari = aktivni.filter { it.uloga == Uloga.KONOBAR },
            zaSankom = aktivni.filter { it.uloga == Uloga.ZA_SANKOM },
            oznaceni = oznaceni,
            ispomoc = ispomoc,
            ispomocDozvoljena = postavke.ispomocDozvoljena,
            prozorIspomoci = null,
            greskaIspomoci = null,
            otvara = false,
        )
    }

    val stanje: StateFlow<NovaNocStanje> = combine(dio1, _prozor, _greska, _otvara, _greskaOtvaranja) { s, prozor, greska, otvara, greskaOtvaranja ->
        s.copy(prozorIspomoci = prozor, greskaIspomoci = greska, otvara = otvara, greskaOtvaranja = greskaOtvaranja)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        NovaNocStanje(_danas.value, emptyList(), emptyList(), emptySet(), emptyList(), true, null, null, false),
    )

    fun prebaci(radnikId: Long) = _oznaceni.update { if (radnikId in it) it - radnikId else it + radnikId }

    fun prebaciIspomoc(ime: String) = _ispomoc.update { lista -> lista.map { if (it.first == ime) ime to !it.second else it } }

    fun otvoriIspomoc() {
        _greska.value = null
        _prozor.value = ""
    }

    fun zatvoriIspomoc() {
        _prozor.value = null
        _greska.value = null
    }

    fun imeIspomoci(s: String) {
        if (_prozor.value == null) return
        _prozor.value = s
        _greska.value = null
    }

    /** Dodaje ispomoć označenu; pri grešci prozor ostaje otvoren sa porukom ispod polja. */
    fun dodajIspomoc() {
        val ime = _prozor.value ?: return
        val s = stanje.value
        val zauzeta = (s.konobari + s.zaSankom).map { it.ime } + _ispomoc.value.map { it.first }
        when (val ishod = Validacija.provjeriIspomoc(ime, zauzeta)) {
            Ishod.Ok -> {
                _ispomoc.update { it + (Validacija.ocisti(ime) to true) }
                _prozor.value = null
                _greska.value = null
            }
            is Ishod.Odbijeno -> _greska.value = when (ishod.razlog) {
                Odbijanje.NAZIV_PRAZAN -> GRESKA_ISPOMOC_PRAZNO
                Odbijanje.NAZIV_PREDUG -> GRESKA_ISPOMOC_PREDUGO
                else -> GRESKA_ISPOMOC_POSTOJI
            }
        }
    }

    /** Ponovo računa datum iz sata (tablet ostavljen na S0 preko ponoći). */
    fun osvjeziDan() { _danas.value = LocalDate.now(clock) }

    /** Otvara noć sa označenima; dok traje, drugi dodir se ignoriše. */
    fun otvoriNoc() {
        if (_otvara.value) return
        _otvara.value = true
        _greskaOtvaranja.value = false
        val s = stanje.value
        val radnici = (s.konobari + s.zaSankom).filter { it.id in s.oznaceni }.map { it.id }
        val ispomoc = if (s.ispomocDozvoljena) s.ispomoc.filter { it.second }.map { it.first } else emptyList()
        viewModelScope.launch {
            try {
                noc.otvoriNoc(radnici, ispomoc)
                poslijeOtvaranja()
                // `otvara` ostaje true: ekran odlazi sa S0 čim noć postane aktivna.
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("CafeBar919", "Noć nije otvorena", e)
                _greskaOtvaranja.value = true
                _otvara.value = false
            }
        }
    }

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NovaNocViewModel(container.repo(), container.noc(), container.clock, poslijeOtvaranja = container::provjeriProstor) as T
    }
}
