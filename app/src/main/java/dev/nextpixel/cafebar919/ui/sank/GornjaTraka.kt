package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.components.Znak919
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Gornja traka šanka (Paper S1 „Vrh“): visina 72, donja linija 1 `line`, padding 24, razmak 20;
 * znak 919, razdjelnik 1×32, datum (`Tekst.section`), opciona značka, prazan prostor, pa `desno` (razmak 12).
 */
@Composable
fun GornjaTraka(
    datum: String,
    znacka: (@Composable () -> Unit)? = null,
    desno: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(BorderStroke(1.dp, Boje.line))
            .padding(horizontal = Razmaci.s6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Znak919()
        Box(Modifier.width(1.dp).height(32.dp).background(Boje.line))
        Text(datum, color = Boje.ink, style = Tekst.section)
        znacka?.invoke()
        Box(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, content = desno)
    }
}

/** Dugme u traci: okvir 1,5 `neutralLine`, radius 12, padding 22, ikona 22, tekst 15/600; visina najmanje 56 (Paper crta 48). */
@Composable
fun TrakaDugme(tekst: String, ikona: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.md)
            .border(BorderStroke(1.5.dp, Boje.neutralLine), Oblici.md)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(ikona, contentDescription = null, tint = Boje.ink, modifier = Modifier.size(22.dp))
        Text(tekst, color = Boje.ink, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
    }
}
