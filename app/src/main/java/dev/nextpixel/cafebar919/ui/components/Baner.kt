package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

enum class VrstaBanera { UPOZORENJE, INFO, GRESKA }

/** Baner (O3/O4): stoji dok stanje traje. Podloga, okvir i ikona zavise od vrste. */
@Composable
fun Baner(vrsta: VrstaBanera, naslov: String, tekst: String, dugme: String? = null, onDugme: () -> Unit = {}) {
    val (podloga, okvir, boja, ikona) = when (vrsta) {
        VrstaBanera.UPOZORENJE -> Stil(Boje.warningTint, Boje.warningLine, Boje.warning, Icons.Rounded.Warning)
        VrstaBanera.INFO -> Stil(Boje.primaryTint, Boje.infoLine, Boje.primary, Icons.Rounded.Info)
        VrstaBanera.GRESKA -> Stil(Boje.dangerTint, Boje.dangerLine, Boje.danger, Icons.Rounded.Close)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Oblici.md)
            .background(podloga)
            .border(1.dp, okvir, Oblici.md)
            .padding(Razmaci.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Icon(ikona, null, Modifier.size(24.dp), tint = boja)
        Column(Modifier.weight(1f)) {
            Text(naslov, style = Tekst.body.copy(fontWeight = FontWeight.Bold), color = Boje.ink)
            Text(tekst, style = Tekst.body, color = Boje.muted)
        }
        if (dugme != null) SporednoDugme(dugme, onDugme)
    }
}

private data class Stil(
    val podloga: androidx.compose.ui.graphics.Color,
    val okvir: androidx.compose.ui.graphics.Color,
    val boja: androidx.compose.ui.graphics.Color,
    val ikona: androidx.compose.ui.graphics.vector.ImageVector,
)
