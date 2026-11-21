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
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter

private const val DUZINA_PIN = 6
private const val PIN_01 = "PIN nije tačan. Unesi ponovo. · PIN-01"

private fun pin02(do_: Instant, clock: Clock): String {
    val vrijeme = DateTimeFormatter.ofPattern("HH:mm").withZone(clock.zone).format(do_)
    return "Previše pogrešnih pokušaja. Novi pokušaj je moguć u $vrijeme. · PIN-02"
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
    var blokiran by remember { mutableStateOf(false) }
    var provjera by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val omogucen = !blokiran && !provjera

    LaunchedEffect(pin) {
        pin.blokiranDo()?.let { do_ ->
            blokiran = true
            poruka = pin02(do_, clock)
        }
    }

    fun dodajCifru(c: Char) {
        if (!omogucen || cifre.length >= DUZINA_PIN) return
        poruka = null
        cifre += c
        if (cifre.length < DUZINA_PIN) return
        provjera = true
        scope.launch {
            when (val r = pin.provjeri(cifre)) {
                ProvjeraPina.Tacan -> {
                    onOtkljucano()
                    return@launch // tastatura ostaje onemogućena dok se prozor zatvara
                }
                is ProvjeraPina.Pogresan -> poruka = PIN_01
                is ProvjeraPina.Blokiran -> {
                    blokiran = true
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
