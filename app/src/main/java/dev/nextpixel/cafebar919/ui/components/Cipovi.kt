package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

private val TekstCipa = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold)

/**
 * Čip za izbor (Paper V1.1): radius 12, neizabran = tile podloga bez okvira, izabran = zlatni tint i okvir 1,5
 * sa kvačicom 18, padding 16, razmak 6. Visina najmanje 56 (dodirna zona; Paper crta 48).
 */
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
        else -> Boje.tile
    }
    val okvir = if (izabran && omogucen) Boje.primary else Color.Transparent
    val boja = when {
        !omogucen -> Boje.disabledInk
        izabran -> Boje.primary
        else -> Boje.ink
    }
    Row(
        modifier = modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.md)
            .background(pozadina)
            .border(1.5.dp, okvir, Oblici.md)
            .selectable(selected = izabran, enabled = omogucen, role = Role.Button, onClick = onClick)
            .padding(horizontal = Razmaci.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        if (izabran && kvacica) Icon(Icons.Rounded.Check, null, Modifier.size(18.dp), tint = boja)
        Text(tekst, style = TekstCipa, color = boja)
    }
}

/** Filter-pilula (V1, „Sve · 15“): izabrana je krem podloga i tamni tekst (Paper O2: 15/600, padding 18, okvir 1,5). */
@Composable
fun Pilula(tekst: String, izabrana: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.pill)
            .background(if (izabrana) Boje.ink else Color.Transparent)
            .border(1.5.dp, if (izabrana) Color.Transparent else Boje.line, Oblici.pill)
            .selectable(selected = izabrana, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(tekst, style = TekstCipa, color = if (izabrana) Boje.bg else Boje.ink)
    }
}
