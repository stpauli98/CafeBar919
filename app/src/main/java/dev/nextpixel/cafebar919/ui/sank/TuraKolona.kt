package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Nacrt
import dev.nextpixel.cafebar919.domain.Obracun
import dev.nextpixel.cafebar919.domain.StavkaNacrta
import dev.nextpixel.cafebar919.repo.ArtikalPrikaz
import dev.nextpixel.cafebar919.ui.components.Baner
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.StepperTure
import dev.nextpixel.cafebar919.ui.components.VrstaBanera
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

private class RedStavke(val artikal: ArtikalPrikaz, val mjera: dev.nextpixel.cafebar919.data.MjeraE, val kolicina: Int)

private fun redovi(s: UnosStanje): List<RedStavke> = s.stavke.mapNotNull { st: StavkaNacrta ->
    val p = s.katalog.firstOrNull { it.a.id == st.artikalId } ?: return@mapNotNull null
    val m = p.mjere.firstOrNull { it.id == st.mjeraId } ?: return@mapNotNull null
    RedStavke(p, m, st.kolicina)
}

private fun uObracun(r: List<RedStavke>) = r.map {
    Obracun.StavkaObracuna(
        it.artikal.kategorija.id, it.artikal.kategorija.naziv, it.artikal.kategorija.redoslijed,
        it.mjera.uZbiru, it.mjera.redoslijed, it.mjera.litara, it.kolicina,
    )
}

private val Tnum = Tekst.body.copy(fontFeatureSettings = "tnum")

/**
 * Desna kolona S1 (Paper: 364, `surface`, lijeva linija 1, padding 24/20/24): zaglavlje „TURA“ ili „TURA ZA“ + ime,
 * stavke (skrolaju se), zbirni red, pa dugme visine 64 uvijek dole, van skrola.
 */
@Composable
internal fun TuraKolona(s: UnosStanje, vm: UnosViewModel, modifier: Modifier = Modifier) {
    val ime = s.primaoci.firstOrNull { it.id == s.primalacId }?.ime
    val fokus = LocalFocusManager.current
    val red = redovi(s)
    val poruka = Nacrt.porukaPotvrde(s.imaPica, ime != null, red.isNotEmpty())
    Column(
        modifier
            .width(364.dp)
            .fillMaxHeight()
            .background(Boje.surface)
            .drawBehind { drawLine(Boje.line, Offset(0.5.dp.toPx(), 0f), Offset(0.5.dp.toPx(), size.height), 1.dp.toPx()) }
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (s.greskaUpisa) {
            Baner(VrstaBanera.GRESKA, "Tura nije upisana.", "Tablet nije uspio da sačuva podatke. Tura je ostala ovdje, ništa nije izgubljeno.")
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text((if (ime == null) "Tura" else "Tura za").uppercase(), color = Boje.muted, style = NatpisGrupe)
            if (ime != null) {
                Text(
                    ime,
                    color = Boje.ink,
                    style = Tekst.title.copy(fontWeight = FontWeight.ExtraBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (red.isEmpty()) NovaTura() else Stavke(red, vm)
        }
        if (red.isNotEmpty()) {
            val o = uObracun(red)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(Obracun.zbirMjera(o), Modifier.weight(1f, fill = false), color = Boje.muted, style = Tnum.copy(fontSize = 14.sp, lineHeight = 18.sp))
                Text(
                    Format.litreKategorija(Obracun.litrePoKategoriji(o)),
                    Modifier.padding(start = 8.dp),
                    color = Boje.ink,
                    style = Tnum.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
                )
            }
        }
        GlavnoDugme(
            tekst = when {
                s.greskaUpisa -> "Pokušaj ponovo"
                ime != null && poruka == null -> "Potvrdi · $ime"
                else -> "Potvrdi"
            },
            onClick = { fokus.clearFocus(); vm.potvrdi() },
            modifier = Modifier.fillMaxWidth().height(64.dp),
            ikona = if (s.greskaUpisa) Icons.Rounded.Refresh else Icons.Rounded.Check,
            omoguceno = poruka == null && !s.upisuje,
            upisuje = s.upisuje,
        )
        val ispod = when {
            s.greskaUpisa -> "Šifra: UPIS-01"
            poruka != null -> poruka
            else -> null
        }
        if (ispod != null) {
            Text(ispod, Modifier.fillMaxWidth(), color = Boje.muted, style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 16.sp, textAlign = TextAlign.Center))
        }
    }
}

@Composable
private fun NovaTura() {
    Column(
        Modifier.fillMaxWidth().fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(64.dp).clip(Oblici.pill).background(Boje.tile), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Add, null, Modifier.size(28.dp), tint = Boje.muted)
        }
        Text("Nova tura", color = Boje.ink, style = Tekst.body.copy(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
        Text(
            "Dodirni konobara lijevo, pa pića u sredini. Redoslijed nije bitan.",
            Modifier.widthIn(max = 260.dp),
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center),
        )
    }
}

@Composable
private fun Stavke(red: List<RedStavke>, vm: UnosViewModel) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(red, key = { it.artikal.a.id to it.mjera.id }) { r ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx()) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(r.artikal.a.naziv, color = Boje.ink, style = Tekst.body.copy(fontWeight = FontWeight.SemiBold, lineHeight = 20.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        Format.mjeraUStavci(r.mjera.naziv, r.mjera.litara, r.kolicina),
                        color = Boje.muted,
                        style = Tnum.copy(fontSize = 13.sp, lineHeight = 16.sp),
                    )
                }
                StepperTure(
                    kolicina = r.kolicina,
                    onMinus = { vm.promijeni(r.artikal.a.id, r.mjera.id, -1) },
                    onPlus = { vm.promijeni(r.artikal.a.id, r.mjera.id, 1) },
                    plusOmogucen = r.kolicina < Nacrt.MAX_KOLICINA,
                )
            }
        }
    }
}
