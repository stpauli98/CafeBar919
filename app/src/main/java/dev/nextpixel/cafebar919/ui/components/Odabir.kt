package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Čip za izbor (V1.1): izabran = zlatni tint i okvir, sa kvačicom. */
@Composable
fun Cip(
    tekst: String,
    izabran: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    omogucen: Boolean = true,
    kvacica: Boolean = true,
) {
    val pozadina = when {
        !omogucen -> Boje.disabledBg
        izabran -> Boje.primaryTint
        else -> Color.Transparent
    }
    val okvir = if (izabran && omogucen) Boje.primary else Boje.line
    val boja = when {
        !omogucen -> Boje.disabledInk
        izabran -> Boje.primary
        else -> Boje.ink
    }
    Row(
        modifier = modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.pill)
            .background(pozadina)
            .border(1.dp, okvir, Oblici.pill)
            .selectable(selected = izabran, enabled = omogucen, role = Role.Button, onClick = onClick)
            .padding(horizontal = Razmaci.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s2, Alignment.CenterHorizontally),
    ) {
        if (izabran && kvacica) Icon(Icons.Rounded.Check, null, Modifier.size(20.dp), tint = boja)
        Text(tekst, style = Tekst.body.copy(fontWeight = FontWeight.SemiBold), color = boja)
    }
}

/** Filter-pilula (V1, „Sve · 15“): izabrana je krem podloga i tamni tekst. */
@Composable
fun Pilula(tekst: String, izabrana: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.pill)
            .background(if (izabrana) Boje.ink else Color.Transparent)
            .border(1.dp, if (izabrana) Boje.ink else Boje.line, Oblici.pill)
            .selectable(selected = izabrana, role = Role.Button, onClick = onClick)
            .padding(horizontal = Razmaci.s4),
        contentAlignment = Alignment.Center,
    ) {
        Text(tekst, style = Tekst.body.copy(fontWeight = FontWeight.SemiBold), color = if (izabrana) Boje.onPrimary else Boje.ink)
    }
}

/** Prekidač 52×32 u dodirnoj zoni 56×56. */
@Composable
fun Prekidac(ukljucen: Boolean, onPromjena: (Boolean) -> Unit, omogucen: Boolean = true) {
    val traka = when {
        !omogucen -> Boje.disabledBg
        ukljucen -> Boje.primary
        else -> Boje.neutralLine
    }
    Box(
        modifier = Modifier
            .size(Razmaci.dodir)
            .semantics { contentDescription = "Prekidač" }
            .toggleable(value = ukljucen, enabled = omogucen, role = Role.Switch, onValueChange = onPromjena),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(52.dp, 32.dp).clip(CircleShape).background(traka)) {
            Box(
                Modifier
                    .align(if (ukljucen) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (ukljucen && omogucen) Boje.onPrimary else if (omogucen) Boje.ink else Boje.disabledInk),
            )
        }
    }
}

@Composable
private fun StepperDugme(ikona: androidx.compose.ui.graphics.vector.ImageVector, opis: String, omoguceno: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(Razmaci.dodir)
            .clip(Oblici.md)
            .background(if (omoguceno) Boje.tile else Boje.disabledBg)
            .border(1.dp, Boje.neutralLine, Oblici.md)
            .clickable(enabled = omoguceno, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = opis },
        contentAlignment = Alignment.Center,
    ) {
        Icon(ikona, null, Modifier.size(24.dp), tint = if (omoguceno) Boje.ink else Boje.disabledInk)
    }
}

/** Količina: [−] broj [+], dugmad 56×56, jedinica sitno ispod broja. */
@Composable
fun Stepper(
    vrijednost: String,
    jedinica: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    minusOmogucen: Boolean,
    plusOmogucen: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperDugme(Icons.Rounded.Remove, "Minus", minusOmogucen, onMinus)
        Column(Modifier.width(64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(vrijednost, style = Tekst.section.copy(fontWeight = FontWeight.ExtraBold), color = Boje.ink, textAlign = TextAlign.Center)
            if (jedinica.isNotEmpty()) Text(jedinica, style = Tekst.caption, color = Boje.muted, textAlign = TextAlign.Center)
        }
        StepperDugme(Icons.Rounded.Add, "Plus", plusOmogucen, onPlus)
    }
}

/** Segmentirani izbor (V2 uloga): jedan segment je uvijek izabran. */
@Composable
fun SegmentiraniIzbor(opcije: List<String>, izabrana: Int, onIzbor: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Oblici.md)
            .background(Boje.tile)
            .border(1.dp, Boje.neutralLine, Oblici.md)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        opcije.forEachIndexed { i, tekst ->
            val aktivna = i == izabrana
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(Oblici.sm)
                    .background(if (aktivna) Boje.primary else Color.Transparent)
                    .selectable(selected = aktivna, role = Role.RadioButton, onClick = { onIzbor(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(tekst, style = Tekst.body.copy(fontWeight = FontWeight.SemiBold), color = if (aktivna) Boje.onPrimary else Boje.ink)
            }
        }
    }
}
