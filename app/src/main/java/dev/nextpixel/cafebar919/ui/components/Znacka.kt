package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

enum class VrstaZnacke { NOC, UPISANO, PONISTENO, PONISTENO_BEZ_PINA, ZATVORENA }

/** Značka (Paper O2): visina 28, radius 8, padding 10, razmak 6, ikona 16, tekst 13/600. Uvijek ima riječ. */
@Composable
fun Znacka(tekst: String, vrsta: VrstaZnacke, ikona: ImageVector? = null) {
    val (pozadina, boja) = when (vrsta) {
        VrstaZnacke.NOC -> Boje.primaryTint to Boje.primary
        VrstaZnacke.UPISANO -> Boje.successTint to Boje.success
        VrstaZnacke.PONISTENO -> Boje.dangerTint to Boje.danger
        VrstaZnacke.PONISTENO_BEZ_PINA -> Boje.warningTint to Boje.warning
        VrstaZnacke.ZATVORENA -> Boje.tile to Boje.muted
    }
    Row(
        Modifier.height(28.dp).clip(Oblici.sm).background(pozadina).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (ikona != null) Icon(ikona, null, Modifier.size(16.dp), tint = boja)
        Text(tekst, style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold), color = boja, maxLines = 1)
    }
}
