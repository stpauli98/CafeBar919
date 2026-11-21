package dev.nextpixel.cafebar919.ui.podesavanja.kategorije

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
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.OpasnoDugme
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Prozor „Nova kategorija“ / „Izmjena · {naziv}“ istog oblika kao ArtikalProzor (spec 7 #26). */
@Composable
fun KategorijaProzor(forma: KategorijaForma, vm: KategorijeViewModel) {
    val nova = forma.id == null
    if (forma.potvrdaOdbacivanja) {
        Prozor(
            naslov = if (nova) "Odbaciti novu kategoriju?" else "Odbaciti izmjene?",
            onZatvori = vm::nastaviUnos,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    if (nova) "Uneseni naziv se ne čuva." else "Izmjene se ne čuvaju.",
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
        naslov = if (nova) "Nova kategorija" else "Izmjena · ${forma.pocetniNaziv}",
        onZatvori = vm::odustani,
        sirina = 520.dp,
        sadrzaj = {
            var imaoFokus by remember { mutableStateOf(false) }
            Polje(
                labela = "Naziv",
                vrijednost = forma.naziv,
                onPromjena = vm::promijeniNaziv,
                greska = forma.greskaNaziv,
                placeholder = "npr. Pivo",
                max = Validacija.NAZIV_KATEGORIJE_MAX,
                brojacOd = Validacija.NAZIV_KATEGORIJE_MAX - 2,
                modifier = Modifier.onFocusChanged {
                    if (it.hasFocus) imaoFokus = true else if (imaoFokus) { imaoFokus = false; vm.nazivNapusten() }
                },
            )
        },
        dugmad = {
            SporednoDugme("Odustani", vm::odustani)
            GlavnoDugme("Sačuvaj kategoriju", vm::sacuvaj, ikona = Icons.Rounded.Check, omoguceno = !forma.upis)
        },
    )
}
