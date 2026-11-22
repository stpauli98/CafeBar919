package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.CafeBarApp
import dev.nextpixel.cafebar919.repo.PinServis
import dev.nextpixel.cafebar919.repo.ProvjeraPina
import dev.nextpixel.cafebar919.ui.components.PinUnos
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private const val DUZINA_PIN = 6
internal const val PIN_01 = "PIN nije tačan. Unesi ponovo. · PIN-01"

/** Minut se zaokružuje naviše: blokada do 22:05:40 javlja 22:06, jer ranije pokušaj još ne prolazi. */
internal fun pin02(do_: Instant, clock: Clock): String {
    val naMinut = do_.truncatedTo(ChronoUnit.MINUTES).let { if (it == do_) it else it.plus(1, ChronoUnit.MINUTES) }
    val vrijeme = DateTimeFormatter.ofPattern("HH:mm").withZone(clock.zone).format(naMinut)
    return "Previše pogrešnih pokušaja. Novi pokušaj je moguć u $vrijeme. · PIN-02"
}

/** Čeka da ubrizgani sat dostigne `do_` (provjera svake sekunde, pa je pomjeranje sata uvijek vidljivo). */
internal suspend fun cekajIstek(do_: Instant, clock: Clock) {
    while (clock.instant().isBefore(do_)) {
        delay(Duration.between(clock.instant(), do_).toMillis().coerceIn(1, 1000))
    }
}

/**
 * V0: prozor za PIN vlasnika. Provjera ide automatski na šestoj cifri; dok traje, tastatura je
 * onemogućena. Pogrešan PIN prazni tačke (PIN-01), blokada onemogućava tastaturu (PIN-02).
 */
@Composable
fun PinProzor(
    pin: PinServis,
    onOtkljucano: () -> Unit,
    onOdustani: () -> Unit,
    clock: Clock = (LocalContext.current.applicationContext as CafeBarApp).clock,
) {
    var cifre by remember { mutableStateOf("") }
    var poruka by remember { mutableStateOf<String?>(null) }
    var blokadaDo by remember { mutableStateOf<Instant?>(null) }
    var provjera by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val omogucen = blokadaDo == null && !provjera

    LaunchedEffect(pin) {
        pin.blokiranDo()?.let { do_ ->
            blokadaDo = do_
            poruka = pin02(do_, clock)
        }
    }

    // Kad blokada istekne, tastatura se vraća i poruka PIN-02 nestaje.
    LaunchedEffect(blokadaDo) {
        val do_ = blokadaDo ?: return@LaunchedEffect
        cekajIstek(do_, clock)
        blokadaDo = null
        poruka = null
    }

    fun dodajCifru(c: Char) {
        if (!omogucen || cifre.length >= DUZINA_PIN) return
        poruka = null
        cifre += c
        if (cifre.length < DUZINA_PIN) return
        provjera = true
        val uneseno = cifre // uhvaćeno prije korutine: brisanje u istom kadru ne smije promijeniti provjeru
        scope.launch {
            when (val r = pin.provjeri(uneseno)) {
                ProvjeraPina.Tacan -> {
                    onOtkljucano()
                    return@launch // tastatura ostaje onemogućena dok se prozor zatvara
                }
                is ProvjeraPina.Pogresan -> poruka = PIN_01
                is ProvjeraPina.Blokiran -> {
                    blokadaDo = r.do_
                    poruka = pin02(r.do_, clock)
                }
            }
            cifre = ""
            provjera = false
        }
    }

    Prozor(
        naslov = "Podešavanja",
        onZatvori = onOdustani,
        sirina = 420.dp,
        ikona = Icons.Outlined.Lock,
        sadrzaj = {
            Text(
                "Samo vlasnik mijenja artikle, mjere i radnike. Otključava se čim upišeš šestu cifru.",
                color = Boje.muted,
                style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 22.sp),
            )
            PinUnos(
                unesene = cifre.length,
                greska = poruka,
                onCifra = ::dodajCifru,
                onBrisi = {
                    if (omogucen) {
                        poruka = null
                        cifre = cifre.dropLast(1)
                    }
                },
                omogucen = omogucen,
            )
        },
        dugmad = { Odustani(onOdustani) },
    )
}

@Composable
private fun RowScope.Odustani(onOdustani: () -> Unit) {
    SporednoDugme("Odustani", onOdustani, Modifier.weight(1f).fillMaxWidth())
}
