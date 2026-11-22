package dev.nextpixel.cafebar919.ui.podesavanja.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.domain.Pin
import dev.nextpixel.cafebar919.repo.PinServis
import dev.nextpixel.cafebar919.repo.PromjenaPina
import dev.nextpixel.cafebar919.repo.ProvjeraPina
import dev.nextpixel.cafebar919.ui.podesavanja.PIN_01
import dev.nextpixel.cafebar919.ui.podesavanja.cekajIstek
import dev.nextpixel.cafebar919.ui.podesavanja.pin02
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant

const val PIN_PROMIJENJEN = "PIN je promijenjen."
const val PIN_ZABRANJEN = "Taj PIN je lako pogoditi. Izaberi drugi."
const val PIN_NEPOKLAPANJE = "PIN-ovi se ne poklapaju. Upiši novi PIN ponovo."
private const val GRESKA_NEUSPJELO = "Čuvanje nije uspjelo."

enum class KorakPina(val broj: Int, val naziv: String) {
    TRENUTNI(1, "Trenutni PIN"),
    NOVI(2, "Novi PIN"),
    PONOVI(3, "Ponovi novi PIN"),
}

/** Šta ekran vidi: samo broj unesenih cifara, nikad same cifre. */
data class PromjenaPinaStanje(
    val korak: KorakPina = KorakPina.TRENUTNI,
    val unesene: Int = 0,
    val greska: String? = null,
    val blokiran: Boolean = false,
    val provjera: Boolean = false,
) {
    val omogucen get() = !blokiran && !provjera
}

/**
 * V3: promjena PIN-a u tri koraka. Cifre žive samo u memoriji ovog ViewModela i brišu se
 * pri odlasku sa ekrana, pri blokadi, pri grešci i poslije uspjeha.
 */
class PromjenaPinaViewModel(private val pin: PinServis, private val clock: Clock) : ViewModel() {
    private val _stanje = MutableStateFlow(PromjenaPinaStanje())
    private val _poruke = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val stanje: StateFlow<PromjenaPinaStanje> = _stanje.asStateFlow()
    val poruke: SharedFlow<String> = _poruke.asSharedFlow()

    private var unos = ""
    private var trenutni = ""
    private var novi = ""

    private var istek: Job? = null

    init {
        viewModelScope.launch {
            pin.blokiranDo()?.let { do_ ->
                _stanje.update { it.copy(blokiran = true, greska = pin02(do_, clock)) }
                cekajOdblokiranje(do_)
            }
        }
    }

    /** Kad blokada istekne, tastatura se vraća i poruka PIN-02 nestaje. */
    private fun cekajOdblokiranje(do_: Instant) {
        istek?.cancel()
        istek = viewModelScope.launch {
            cekajIstek(do_, clock)
            _stanje.update { it.copy(blokiran = false, greska = null) }
        }
    }

    fun cifra(c: Char) {
        val s = _stanje.value
        if (!s.omogucen || unos.length >= Pin.DUZINA) return
        unos += c
        _stanje.value = s.copy(unesene = unos.length, greska = null)
        if (unos.length == Pin.DUZINA) zavrsiKorak()
    }

    fun brisi() {
        val s = _stanje.value
        if (!s.omogucen) return
        unos = unos.dropLast(1)
        _stanje.value = s.copy(unesene = unos.length, greska = null)
    }

    private fun zavrsiKorak() {
        val uneseno = unos // uhvaćeno prije korutine
        unos = ""
        val s = _stanje.value.copy(unesene = 0)
        when (s.korak) {
            KorakPina.TRENUTNI -> {
                _stanje.value = s.copy(provjera = true)
                viewModelScope.launch {
                    when (val r = pin.provjeri(uneseno)) {
                        ProvjeraPina.Tacan -> {
                            trenutni = uneseno
                            _stanje.value = s.copy(korak = KorakPina.NOVI)
                        }
                        is ProvjeraPina.Pogresan -> _stanje.value = s.copy(greska = PIN_01)
                        is ProvjeraPina.Blokiran -> {
                            _stanje.value = s.copy(blokiran = true, greska = pin02(r.do_, clock))
                            cekajOdblokiranje(r.do_)
                        }
                    }
                }
            }
            KorakPina.NOVI ->
                if (Pin.zabranjen(uneseno)) {
                    _stanje.value = s.copy(greska = PIN_ZABRANJEN)
                } else {
                    novi = uneseno
                    _stanje.value = s.copy(korak = KorakPina.PONOVI)
                }
            KorakPina.PONOVI ->
                if (uneseno != novi) {
                    novi = ""
                    _stanje.value = s.copy(korak = KorakPina.NOVI, greska = PIN_NEPOKLAPANJE)
                } else {
                    _stanje.value = s.copy(provjera = true)
                    val zaPromjenu = trenutni to novi
                    viewModelScope.launch { promijeni(zaPromjenu.first, zaPromjenu.second) }
                }
        }
    }

    private suspend fun promijeni(stari: String, noviPin: String) {
        val r = pin.promijeni(stari, noviPin)
        obrisi()
        when (r) {
            PromjenaPina.Uspjeh -> {
                _stanje.value = PromjenaPinaStanje()
                _poruke.emit(PIN_PROMIJENJEN)
            }
            PromjenaPina.PogresanTrenutni -> _stanje.value = PromjenaPinaStanje(greska = PIN_01)
            is PromjenaPina.Blokiran -> {
                _stanje.value = PromjenaPinaStanje(blokiran = true, greska = pin02(r.do_, clock))
                cekajOdblokiranje(r.do_)
            }
            PromjenaPina.Zabranjen -> _stanje.value = PromjenaPinaStanje(greska = PIN_ZABRANJEN)
            PromjenaPina.NeispravanFormat -> _stanje.value = PromjenaPinaStanje(greska = GRESKA_NEUSPJELO)
        }
    }

    private fun obrisi() {
        unos = ""
        trenutni = ""
        novi = ""
    }

    override fun onCleared() = obrisi()

    class Fabrika(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PromjenaPinaViewModel(container.pin(), container.clock) as T
    }
}
