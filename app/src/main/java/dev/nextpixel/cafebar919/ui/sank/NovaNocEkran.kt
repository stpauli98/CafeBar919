package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * S0 „Nova noć“ (Paper): traka sa datumom i „Podešavanja“, pa centralni blok (nadnaslov, naslov, podnaslov,
 * kolone 280 sa razmakom 40, dugme 920×64, napomena). Blok je centriran, a skroluje se kad radnika ima previše.
 */
@Composable
fun NovaNocEkran(vm: NovaNocViewModel, onPodesavanja: () -> Unit) {
    val s by vm.stanje.collectAsState()
    Column(Modifier.fillMaxSize().background(Boje.bg)) {
        GornjaTraka(datum = Format.datumNoci(s.danas)) {
            TrakaDugme("Podešavanja", Icons.Outlined.Lock, onPodesavanja)
        }
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val visina = maxHeight
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = visina)
                    .padding(top = 16.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
            ) {
                Zaglavlje()
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp), verticalAlignment = Alignment.Top) {
                    Kolona("Konobari") {
                        s.konobari.forEach { r -> Kartica(r.ime, "konobar", r.id in s.oznaceni) { vm.prebaci(r.id) } }
                    }
                    Kolona("Za šankom") {
                        s.zaSankom.forEach { r -> Kartica(r.ime, "za šankom", r.id in s.oznaceni) { vm.prebaci(r.id) } }
                    }
                    if (s.ispomocDozvoljena) {
                        Kolona("Ispomoć · samo večeras") {
                            s.ispomoc.forEach { (ime, oznacena) -> Kartica(ime, "ispomoć", oznacena) { vm.prebaciIspomoc(ime) } }
                            DodajIspomoc(vm::otvoriIspomoc)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    GlavnoDugme(
                        tekst = "Otvori noć · " + Format.brojac(s.brojRadnika, "radnik", "radnika", "radnika"),
                        onClick = vm::otvoriNoc,
                        modifier = Modifier.width(920.dp).height(64.dp),
                        ikona = Icons.Outlined.NightsStay,
                        upisuje = s.otvara,
                    )
                    Text(
                        "Šank za prosuto je uvijek uključen. Datum noći ostaje ${Format.datumMalim(s.danas)} i poslije ponoći.",
                        color = Boje.muted,
                        style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp),
                    )
                }
            }
        }
    }
    s.prozorIspomoci?.let { IspomocProzor(vm, it, s.greskaIspomoci) }
}

@Composable
private fun Zaglavlje() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Nema otvorene noći".uppercase(),
            color = Boje.primary,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
        )
        Text(
            "Ko radi večeras?",
            color = Boje.ink,
            style = Tekst.body.copy(fontSize = 44.sp, lineHeight = 54.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.03).em),
        )
        Text(
            "Označi ko radi. Samo oni se prikazuju na ekranu za unos.",
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Kolona(naslov: String, sadrzaj: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            naslov.uppercase(),
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em),
        )
        sadrzaj()
    }
}

/** Kartica osobe na S0 (280×80, kvadratić 32): označena = zlatni okvir i tamna zlatna podloga. Nije S1 pločica. */
@Composable
private fun Kartica(ime: String, uloga: String, oznacena: Boolean, onPromjena: () -> Unit) {
    Row(
        Modifier
            .width(280.dp)
            .height(80.dp)
            .clip(Oblici.lg)
            .background(if (oznacena) Boje.primaryTint else Boje.tile)
            .border(2.dp, if (oznacena) Boje.primary else Color.Transparent, Oblici.lg)
            .toggleable(value = oznacena, role = Role.Checkbox, onValueChange = { onPromjena() })
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCorner10)
                .background(if (oznacena) Boje.primary else Color.Transparent)
                .border(2.dp, if (oznacena) Boje.primary else Boje.neutralLine, RoundedCorner10),
            contentAlignment = Alignment.Center,
        ) {
            if (oznacena) Icon(Icons.Rounded.Check, null, Modifier.size(20.dp), tint = Boje.onPrimary)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                ime,
                color = Boje.ink,
                style = Tekst.body.copy(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(uloga, color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp))
        }
    }
}

private val RoundedCorner10 = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)

/** Isprekidana kartica „+ Dodaj ispomoć“ (Paper: okvir 2 `neutralLine`, radius 16, ikona plus 22, tekst 17/600). */
@Composable
private fun DodajIspomoc(onClick: () -> Unit) {
    Row(
        Modifier
            .width(280.dp)
            .height(80.dp)
            .clip(Oblici.lg)
            .drawBehind {
                val d = 2.dp.toPx()
                drawRoundRect(
                    color = Boje.neutralLine,
                    topLeft = Offset(d / 2, d / 2),
                    size = Size(size.width - d, size.height - d),
                    cornerRadius = CornerRadius(16.dp.toPx() - d / 2),
                    style = Stroke(width = d, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))),
                )
            }
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        Icon(Icons.Rounded.Add, null, Modifier.size(22.dp), tint = Boje.ink)
        Text("Dodaj ispomoć", color = Boje.ink, style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold))
    }
}
