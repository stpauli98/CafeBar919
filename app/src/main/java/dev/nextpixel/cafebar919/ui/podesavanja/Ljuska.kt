package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SportsBar
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.repo.BrojaciMenija
import dev.nextpixel.cafebar919.ui.components.Baner
import dev.nextpixel.cafebar919.ui.components.VrstaBanera
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

enum class Sekcija { ARTIKLI, KATEGORIJE, MJERE, LJUDI, PRAVILA, PIN, O_APLIKACIJI }

private val Sekcija.naslov: String
    get() = when (this) {
        Sekcija.ARTIKLI -> "Artikli"
        Sekcija.KATEGORIJE -> "Kategorije"
        Sekcija.MJERE -> "Mjere"
        Sekcija.LJUDI -> "Ljudi"
        Sekcija.PRAVILA -> "Pravila rada"
        Sekcija.PIN -> "PIN vlasnika"
        Sekcija.O_APLIKACIJI -> "O aplikaciji"
    }

private val Sekcija.ikona: ImageVector
    get() = when (this) {
        Sekcija.ARTIKLI -> Icons.Outlined.SportsBar
        Sekcija.KATEGORIJE -> Icons.Outlined.GridView
        Sekcija.MJERE -> Icons.Outlined.Straighten
        Sekcija.LJUDI -> Icons.Outlined.Groups
        Sekcija.PRAVILA -> Icons.Outlined.Tune
        Sekcija.PIN -> Icons.Outlined.Lock
        Sekcija.O_APLIKACIJI -> Icons.Outlined.Info
    }

private fun Sekcija.podnaslov(b: BrojaciMenija): String = when (this) {
    Sekcija.ARTIKLI -> "${if (b.pica == 0) "nema pića" else Format.brojac(b.pica, "piće", "pića", "pića")} · redoslijed"
    Sekcija.KATEGORIJE -> Format.brojac(b.kategorija, "kategorija", "kategorije", "kategorija")
    Sekcija.MJERE -> "${Format.brojac(b.mjera, "mjera", "mjere", "mjera")} u litrima"
    Sekcija.LJUDI -> Format.brojac(b.stalnihRadnika, "stalni radnik", "stalna radnika", "stalnih radnika")
    Sekcija.PRAVILA -> "poništavanje, istorija"
    Sekcija.PIN -> "promjena"
    Sekcija.O_APLIKACIJI -> "verzija, kontakt"
}

/**
 * Zajednička ljuska podešavanja (Paper V1): gornja traka 72, baner „otključano“, lijevi meni 276 i radni dio.
 * Svaki dodir (pass Initial, bez trošenja) vraća brojač zaključavanja na početak.
 */
@Composable
fun Ljuska(
    aktivna: Sekcija,
    onSekcija: (Sekcija) -> Unit,
    onNazad: () -> Unit,
    sesija: PodesavanjaSesija,
    brojaci: BrojaciMenija,
    probniPin: Boolean,
    sadrzaj: @Composable () -> Unit,
) {
    val preostaloS by sesija.preostaloS.collectAsState()
    Column(
        Modifier
            .fillMaxSize()
            .background(Boje.bg)
            .pointerInput(sesija) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        sesija.dodir()
                    }
                }
            },
    ) {
        GornjaTraka(onNazad = { sesija.zakljucaj(); onNazad() })
        BanerOtkljucano(preostaloS, onZakljucaj = sesija::zakljucaj)
        if (probniPin) {
            Box(Modifier.padding(horizontal = Razmaci.s6, vertical = Razmaci.s3)) {
                Baner(VrstaBanera.UPOZORENJE, "Još se koristi probni PIN 123456.", "Promijeni ga u meniju PIN vlasnika.")
            }
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            Meni(aktivna, onSekcija, brojaci)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 28.dp, vertical = 20.dp),
            ) { sadrzaj() }
        }
    }
}

@Composable
private fun GornjaTraka(onNazad: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(BorderStroke(1.dp, Boje.line))
            .padding(horizontal = Razmaci.s6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            Modifier
                .heightIn(min = Razmaci.dodir)
                .clip(Oblici.md)
                .border(BorderStroke(1.5.dp, Boje.neutralLine), Oblici.md)
                .clickable(role = Role.Button, onClick = onNazad)
                .padding(start = 12.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Razmaci.s2),
        ) {
            Icon(Icons.Outlined.ChevronLeft, null, Modifier.size(22.dp), tint = Boje.ink)
            Text("Nazad na unos", color = Boje.ink, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
            Text(
                "Podešavanja",
                color = Boje.ink,
                style = Tekst.body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.01).em),
            )
            Text("samo vlasnik", color = Boje.muted, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp), modifier = Modifier.padding(bottom = 5.dp))
        }
    }
}

/** Baner preko cijele širine (Paper: podloga warning-tint, linija ispod); visina 56 zbog dodirne zone dugmeta. */
@Composable
private fun BanerOtkljucano(preostaloS: Int, onZakljucaj: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Razmaci.dodir)
            .background(Boje.warningTint)
            .drawBottomLine()
            .padding(horizontal = Razmaci.s6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Icon(Icons.Outlined.Lock, null, Modifier.size(20.dp), tint = Boje.warning)
        val stil = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp)
        Text("Otključano za vlasnika.", color = Boje.ink, style = stil.copy(fontWeight = FontWeight.Bold))
        Text("Zaključava se samo za ${Format.odbrojavanje(preostaloS)} bez dodira.", color = Boje.ink, style = stil)
        Spacer(Modifier.weight(1f))
        Row(
            Modifier
                .heightIn(min = Razmaci.dodir)
                .clickable(role = Role.Button, onClick = onZakljucaj)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .height(36.dp)
                    .border(1.dp, Boje.warningLine, Oblici.sm)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Razmaci.s2),
            ) {
                Icon(Icons.Outlined.Lock, null, Modifier.size(16.dp), tint = Boje.ink)
                Text("Zaključaj sada", color = Boje.ink, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

private fun Modifier.drawBottomLine(): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawLine(Boje.warningLine, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx())
    },
)

@Composable
private fun Meni(aktivna: Sekcija, onSekcija: (Sekcija) -> Unit, brojaci: BrojaciMenija) {
    Column(
        Modifier
            .width(276.dp)
            .fillMaxHeight()
            .drawWithContent {
                drawContent()
                drawLine(Boje.line, Offset(size.width - 0.5.dp.toPx(), 0f), Offset(size.width - 0.5.dp.toPx(), size.height), 1.dp.toPx())
            }
            .padding(horizontal = Razmaci.s4, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(Razmaci.s1),
    ) {
        Text("PODEŠAVANJA", color = Boje.muted, style = Tekst.caption.copy(letterSpacing = 0.06.em))
        Sekcija.entries.forEach { s -> StavkaMenija(s, s == aktivna, brojaci, onClick = { onSekcija(s) }) }
        Spacer(Modifier.weight(1f))
        Text(
            "Sve promjene važe odmah, bez nove verzije aplikacije.",
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 18.sp),
            modifier = Modifier.padding(horizontal = Razmaci.s1),
        )
    }
}

@Composable
private fun StavkaMenija(sekcija: Sekcija, aktivna: Boolean, brojaci: BrojaciMenija, onClick: () -> Unit) {
    val oblik = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(Razmaci.dodir)
            .clip(oblik)
            .background(if (aktivna) Boje.primaryTint else Color.Transparent)
            .border(1.5.dp, if (aktivna) Boje.primary else Color.Transparent, oblik)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = Razmaci.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Icon(sekcija.ikona, null, Modifier.size(22.dp), tint = if (aktivna) Boje.primary else Boje.muted)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(sekcija.naslov, color = Boje.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold))
            Text(sekcija.podnaslov(brojaci), color = Boje.muted, maxLines = 1, overflow = TextOverflow.Ellipsis, style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 16.sp))
        }
    }
}
