package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Segmentirani izbor (Paper V2 uloga): tamni kontejner (bg) sa okvirom 1 (line), radius 12, padding 3. Aktivan segment je
 * svjetlija podloga (tile) sa krem tekstom, neaktivan bez podloge i sa sivim tekstom (14/600). Segment prianja uz tekst
 * (padding 16, radius 9); jedan je uvijek izabran, svaki segment je najmanje 56 dp visok.
 */
@Composable
fun SegmentiraniIzbor(opcije: List<String>, izabrana: Int, onIzbor: (Int) -> Unit) {
    Row(
        Modifier
            .clip(Oblici.md)
            .background(Boje.bg)
            .border(1.dp, Boje.line, Oblici.md)
            .padding(3.dp),
    ) {
        opcije.forEachIndexed { i, tekst ->
            val aktivna = i == izabrana
            Box(
                Modifier
                    .heightIn(min = Razmaci.dodir)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (aktivna) Boje.tile else Color.Transparent)
                    .selectable(selected = aktivna, role = Role.RadioButton, onClick = { onIzbor(i) })
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    tekst,
                    style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
                    color = if (aktivna) Boje.ink else Boje.muted,
                )
            }
        }
    }
}
