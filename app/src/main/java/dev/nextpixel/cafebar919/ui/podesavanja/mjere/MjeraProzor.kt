package dev.nextpixel.cafebar919.ui.podesavanja.mjere

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.OpasnoDugme
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

private const val NAJVISE_ZNAKOVA_LITARA = 8
private val POMOCNI = Tekst.body.copy(fontSize = 13.sp, lineHeight = 18.sp)

/** Prozor „Nova mjera“ / „Izmjena · {naziv}“ istog oblika kao ArtikalProzor (spec 7 #26). */
@Composable
fun MjeraProzor(forma: MjeraForma, vm: MjereViewModel) {
    val nova = forma.id == null
    if (forma.potvrdaOdbacivanja) {
        Prozor(
            naslov = if (nova) "Odbaciti novu mjeru?" else "Odbaciti izmjene?",
            onZatvori = vm::nastaviUnos,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    if (nova) "Uneseni podaci se ne čuvaju." else "Izmjene se ne čuvaju.",
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
        naslov = if (nova) "Nova mjera" else "Izmjena · ${forma.pocetna.naziv}",
        onZatvori = vm::odustani,
        sirina = 760.dp,
        sadrzaj = {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                // Dva polja u redu: sa četiri poredana polja i greškama prozor ne stane na 800 dp visine.
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    PoljeMjereUnos(
                        "Naziv u pregledu", forma.naziv, vm::promijeniNaziv, forma, vm, PoljeMjere.NAZIV,
                        placeholder = "npr. Boca 0,7", max = Validacija.NAZIV_MJERE_MAX, modifier = Modifier.weight(1f),
                    )
                    PoljeMjereUnos(
                        "Natpis na dugmetu", forma.natpis, vm::promijeniNatpis, forma, vm, PoljeMjere.NATPIS,
                        placeholder = "npr. Boca", max = Validacija.NATPIS_MAX, modifier = Modifier.weight(1f),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    PoljeMjereUnos(
                        "U zbiru", forma.uZbiru, vm::promijeniUZbiru, forma, vm, PoljeMjere.U_ZBIRU,
                        placeholder = "npr. kom", max = Validacija.U_ZBIRU_MAX, modifier = Modifier.weight(1f),
                        pomocni = "Kratko ime u zbiru, npr. kom, doza, bok.",
                    )
                    PoljeMjereUnos(
                        "Litara", forma.litara, vm::promijeniLitre, forma, vm, PoljeMjere.LITARA,
                        placeholder = "npr. 0,70", max = NAJVISE_ZNAKOVA_LITARA, tastatura = KeyboardType.Decimal, modifier = Modifier.weight(1f),
                        pomocni = "Prazno za komad. Kuca se sa zarezom: 0,70.",
                    )
                }
                if (!nova) {
                    Text(
                        "Promjena litara važi od sljedeće ture. Stare ture čuvaju litre iz trenutka unosa.",
                        color = Boje.muted,
                        style = POMOCNI,
                    )
                }
            }
        },
        dugmad = {
            if (forma.greska != null) {
                Text(forma.greska, Modifier.weight(1f), color = Boje.danger, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp))
            }
            SporednoDugme("Odustani", vm::odustani)
            GlavnoDugme("Sačuvaj mjeru", vm::sacuvaj, ikona = Icons.Rounded.Check, omoguceno = !forma.upis)
        },
    )
}

@Composable
private fun PoljeMjereUnos(
    labela: String,
    vrijednost: String,
    onPromjena: (String) -> Unit,
    forma: MjeraForma,
    vm: MjereViewModel,
    polje: PoljeMjere,
    placeholder: String,
    max: Int,
    tastatura: KeyboardType = KeyboardType.Text,
    pomocni: String? = null,
    modifier: Modifier = Modifier,
) {
    var imaoFokus by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Polje(
            labela = labela,
            vrijednost = vrijednost,
            onPromjena = onPromjena,
            greska = forma.greske[polje],
            placeholder = placeholder,
            max = max,
            brojacOd = if (polje == PoljeMjere.LITARA) null else max - 2,
            tastatura = tastatura,
            modifier = Modifier.onFocusChanged {
                if (it.hasFocus) imaoFokus = true else if (imaoFokus) { imaoFokus = false; vm.poljeNapusteno(polje) }
            },
        )
        if (pomocni != null) Text(pomocni, color = Boje.muted, style = POMOCNI)
    }
}
