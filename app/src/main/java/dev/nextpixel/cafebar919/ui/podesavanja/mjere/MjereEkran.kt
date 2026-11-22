package dev.nextpixel.cafebar919.ui.podesavanja.mjere

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.repo.MjeraPrikaz
import dev.nextpixel.cafebar919.ui.components.Prekidac
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.DugmeIzmjene
import dev.nextpixel.cafebar919.ui.podesavanja.ZaglavljeSekcije
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

private val ZAGLAVLJE_KOLONE = Tekst.body.copy(
    fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.06.sp,
)

// Širine kolona iz Papera V4; kolona „U ZBIRU“ je dodata po spec 7 #26.
private val NAZIV = 220.dp
private val LITARA = 120.dp
private val DUGME = 150.dp
private val U_ZBIRU = 90.dp
private val SIRINA_PREKIDACA_I_STRELICE = 108.dp

/** V4 · Mjere (Paper): zaglavlje, tabela sa kolonama NAZIV U PREGLEDU | LITARA | DUGME | U ZBIRU | KORISTE JE, pomoćni tekst. */
@Composable
fun MjereEkran(vm: MjereViewModel, toast: ToastStanje) {
    LaunchedEffect(vm) { vm.poruke.collect { toast.prikazi(it) } }
    val lista by vm.lista.collectAsState()
    val prozor by vm.prozor.collectAsState()

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ZaglavljeSekcije(
            "Mjere",
            "Svaka mjera je jedno dugme na pločici. Litre se sabiraju u pregledu. Komad nema litre.",
            "Nova mjera",
            vm::otvoriNovu,
        )
        Column(Modifier.weight(1f, fill = false)) {
            Zaglavlje()
            lista?.let { mjere ->
                LazyColumn {
                    items(mjere, key = { it.m.id }) { p ->
                        RedMjere(p, onPrebaci = { vm.prebaci(p.m.id, it) }, onIzmjena = { vm.otvoriIzmjenu(p.m.id) })
                    }
                }
            }
        }
        Text(
            "Promjena litara važi od sljedeće ture. Stare ture čuvaju litre iz trenutka unosa. Mjera koju koristi neko piće ne može se isključiti dok se ne skine sa tog pića.",
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
        )
    }
    prozor?.let { MjeraProzor(it, vm) }
}

@Composable
private fun Zaglavlje() {
    Row(
        Modifier.fillMaxWidth().height(36.dp).drawBehind { linija() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("NAZIV U PREGLEDU", Modifier.width(NAZIV), color = Boje.muted, maxLines = 1, style = ZAGLAVLJE_KOLONE)
        Text("LITARA", Modifier.width(LITARA), color = Boje.muted, maxLines = 1, textAlign = TextAlign.End, style = ZAGLAVLJE_KOLONE)
        Text("DUGME", Modifier.width(DUGME).padding(start = 32.dp), color = Boje.muted, maxLines = 1, style = ZAGLAVLJE_KOLONE)
        Text("U ZBIRU", Modifier.width(U_ZBIRU), color = Boje.muted, maxLines = 1, style = ZAGLAVLJE_KOLONE)
        Text("KORISTE JE", Modifier.weight(1f), color = Boje.muted, maxLines = 1, style = ZAGLAVLJE_KOLONE)
        Box(Modifier.width(SIRINA_PREKIDACA_I_STRELICE))
    }
}

/** Red 64: naziv 220, litara 120 desno, dugme 150 (čip), u zbiru 90, koriste je, prekidač, „›“; razmak 16 (Paper V4). */
@Composable
private fun RedMjere(p: MjeraPrikaz, onPrebaci: (Boolean) -> Unit, onIzmjena: () -> Unit) {
    val m = p.m
    val boja = if (m.aktivna) Boje.ink else Boje.muted
    Row(
        Modifier.fillMaxWidth().height(64.dp).drawBehind { linija() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            m.naziv, Modifier.width(NAZIV), color = boja, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        )
        Text(
            m.litara?.let { Format.litre(it) } ?: "—", Modifier.width(LITARA), color = boja, textAlign = TextAlign.End,
            style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 20.sp, fontFeatureSettings = "tnum"),
        )
        Box(Modifier.width(DUGME).padding(start = 32.dp)) {
            Box(
                Modifier
                    .height(30.dp)
                    .background(Boje.tile, Oblici.sm)
                    .border(BorderStroke(1.dp, Boje.line), Oblici.sm)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    m.natpis, color = boja, maxLines = 1,
                    style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
                )
            }
        }
        Text(m.uZbiru, Modifier.width(U_ZBIRU), color = Boje.muted, maxLines = 1, overflow = TextOverflow.Ellipsis, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp))
        Text(
            koristeJe(p.aktivnaPica), Modifier.weight(1f), color = Boje.muted, maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp),
        )
        // Mjera koju koristi aktivno piće ostaje uključena dok se ne skine sa tog pića.
        Prekidac(m.aktivna, onPrebaci, omogucen = !(m.aktivna && p.aktivnaPica.isNotEmpty()), opis = m.naziv)
        DugmeIzmjene(m.naziv, onIzmjena)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.linija() {
    drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx())
}
