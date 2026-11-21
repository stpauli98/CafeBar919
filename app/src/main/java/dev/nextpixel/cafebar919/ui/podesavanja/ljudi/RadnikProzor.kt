package dev.nextpixel.cafebar919.ui.podesavanja.ljudi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.OpasnoDugme
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SegmentiraniIzbor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

internal val ULOGE = listOf(Uloga.KONOBAR to "Konobar", Uloga.ZA_SANKOM to "Za šankom")

/** Prozor „Novi radnik“ / „Izmjena · {ime}“ istog oblika kao ostali prozori podešavanja (spec 7 #26). */
@Composable
fun RadnikProzor(forma: RadnikForma, vm: LjudiViewModel) {
    val novi = forma.id == null
    if (forma.potvrdaOdbacivanja) {
        Prozor(
            naslov = if (novi) "Odbaciti novog radnika?" else "Odbaciti izmjene?",
            onZatvori = vm::nastaviUnos,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    if (novi) "Uneseno ime se ne čuva." else "Izmjene se ne čuvaju.",
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                )
            },
            dugmad = {
                SporednoDugme("Nastavi unos", vm::nastaviUnos)
                OpasnoDugme("Odbaci", vm::odbaci)
            },
        )
        return
    }
    Prozor(
        naslov = if (novi) "Novi radnik" else "Izmjena · ${forma.pocetnoIme}",
        onZatvori = vm::odustani,
        sirina = 520.dp,
        sadrzaj = {
            var imaoFokus by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Polje(
                    labela = "Ime",
                    vrijednost = forma.ime,
                    onPromjena = vm::promijeniIme,
                    greska = forma.greska,
                    placeholder = "npr. Dejan",
                    max = Validacija.IME_MAX,
                    brojacOd = Validacija.IME_MAX - 2,
                    modifier = Modifier.onFocusChanged {
                        if (it.hasFocus) imaoFokus = true else if (imaoFokus) { imaoFokus = false; vm.imeNapusteno() }
                    },
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Uloga", color = Boje.ink, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                    SegmentiraniIzbor(ULOGE.map { it.second }, ULOGE.indexOfFirst { it.first == forma.uloga }) { vm.promijeniUlogu(ULOGE[it].first) }
                }
            }
        },
        dugmad = {
            SporednoDugme("Odustani", vm::odustani)
            GlavnoDugme("Sačuvaj radnika", vm::sacuvaj, ikona = Icons.Rounded.Check, omoguceno = !forma.upis)
        },
    )
}
