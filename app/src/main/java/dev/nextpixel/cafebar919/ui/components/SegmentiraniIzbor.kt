package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Segmentirani izbor (V2 uloga): jedan segment je uvijek izabran, svaki segment je najmanje 56 dp visok. */
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
                    .heightIn(min = Razmaci.dodir)
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
