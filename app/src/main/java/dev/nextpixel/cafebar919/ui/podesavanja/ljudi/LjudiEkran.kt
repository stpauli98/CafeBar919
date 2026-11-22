package dev.nextpixel.cafebar919.ui.podesavanja.ljudi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.data.RadnikE
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.Prekidac
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.SegmentiraniIzbor
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.ZaglavljeSekcije
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/** V2 · Ljudi (Paper): zaglavlje, redovi 76 (ime + opis | uloga | „radi“ + prekidač), info kartica, pomoćni tekst. */
@Composable
fun LjudiEkran(vm: LjudiViewModel, toast: ToastStanje) {
    LaunchedEffect(vm) { vm.poruke.collect { toast.prikazi(it) } }
    val lista by vm.lista.collectAsState()
    val prozor by vm.prozor.collectAsState()
    val iskljucivanje by vm.iskljucivanje.collectAsState()

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ZaglavljeSekcije(
            "Stalni radnici",
            "",
            "Novi radnik",
            vm::otvoriNovi,
        )
        lista?.let { radnici ->
            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(radnici, key = { it.id }) { r ->
                    RedRadnika(
                        r,
                        onUloga = { vm.postaviUlogu(r.id, ULOGE[it].first) },
                        onAktivan = { vm.prebaci(r.id, it) },
                        onIzmjena = { vm.otvoriIzmjenu(r.id) },
                    )
                }
            }
        }
        InfoKartica()
        Text(
            "Dodir na ime otvara izmjenu imena. Isključen radnik se ne nudi pri otvaranju noći, a ranije noći ga i dalje prikazuju. Promjena uloge važi od sljedeće noći.",
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp),
        )
    }
    prozor?.let { RadnikProzor(it, vm) }
    iskljucivanje?.let { r ->
        Prozor(
            naslov = "Isključiti ${r.ime}?",
            onZatvori = vm::odustaniOdIskljucivanja,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    "Neće se nuditi pri otvaranju noći. Ranije noći ga i dalje prikazuju.",
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                )
            },
            dugmad = {
                SporednoDugme("Odustani", vm::odustaniOdIskljucivanja)
                GlavnoDugme("Isključi", vm::potvrdiIskljucivanje)
            },
        )
    }
}

@Composable
private fun RedRadnika(r: RadnikE, onUloga: (Int) -> Unit, onAktivan: (Boolean) -> Unit, onIzmjena: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(76.dp).drawBehind {
            drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx())
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Dodir na ime i opis otvara izmjenu; cijela zona je visoka kao red (76 ≥ 56).
        Column(
            Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .semantics(mergeDescendants = true) { contentDescription = "Izmijeni ${r.ime}" }
                .clickable(role = Role.Button, onClick = onIzmjena),
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        ) {
            Text(r.ime, color = if (r.aktivan) Boje.ink else Boje.muted, maxLines = 1, style = Tekst.body.copy(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold))
            Text(opisUloge(r.uloga), color = Boje.muted, maxLines = 1, style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 16.sp))
        }
        SegmentiraniIzbor(ULOGE.map { it.second }, ULOGE.indexOfFirst { it.first == r.uloga }, onUloga)
        Text("radi", Modifier.width(80.dp), color = Boje.muted, textAlign = TextAlign.End, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp))
        Prekidac(r.aktivan, onAktivan, opis = r.ime)
    }
}

@Composable
private fun InfoKartica() {
    Row(
        Modifier.fillMaxWidth().background(Boje.surface, Oblici.md).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.People, null, Modifier.size(22.dp), tint = Boje.muted)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Ispomoć se ne dodaje ovdje", color = Boje.ink, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold))
            Text(
                "Radnik upiše ime ispomoći kad otvara noć, bez PIN-a. Ime važi samo te noći i vidi se u istoriji sa oznakom „ispomoć“.",
                color = Boje.muted,
                style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
            )
        }
    }
}
