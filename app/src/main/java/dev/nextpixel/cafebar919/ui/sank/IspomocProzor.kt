package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Validacija
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.Polje
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** S0.1 (Paper): prozor širine 520 sa poljem „Ime“ i pomoćnim tekstom; greške se vide ispod polja. */
@Composable
fun IspomocProzor(vm: NovaNocViewModel, ime: String, greska: String?) {
    Prozor(
        naslov = "Ispomoć večeras",
        onZatvori = vm::zatvoriIspomoc,
        sirina = 520.dp,
        sadrzaj = {
            Text(
                "Ime se pamti samo za ovu noć. Ispomoć ima svoj zbir u pregledu, a sutra se ne nudi.",
                color = Boje.muted,
                style = Tekst.body,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Polje(
                    labela = "Ime",
                    vrijednost = ime,
                    onPromjena = vm::imeIspomoci,
                    greska = greska,
                    max = Validacija.IME_MAX,
                    modifier = Modifier,
                )
                Text(
                    "Kako ga zovu na šanku, npr. „Dejan“ ili „Dejan M.“",
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp),
                )
            }
        },
        dugmad = {
            SporednoDugme("Odustani", vm::zatvoriIspomoc)
            GlavnoDugme("Dodaj", vm::dodajIspomoc)
        },
    )
}
