package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Prozor sa scrimom: surface, radius 16. Dugmad se poravnavaju desno. */
@Composable
fun Prozor(
    naslov: String,
    onZatvori: () -> Unit,
    sirina: Dp,
    ikona: ImageVector? = null,
    sadrzaj: @Composable ColumnScope.() -> Unit,
    dugmad: @Composable RowScope.() -> Unit,
) {
    Dialog(onDismissRequest = onZatvori, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .width(sirina)
                .background(Boje.surface, Oblici.lg)
                .border(1.dp, Boje.line, Oblici.lg)
                .padding(Razmaci.s6),
            verticalArrangement = Arrangement.spacedBy(Razmaci.s4),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
                if (ikona != null) Icon(ikona, null, Modifier.size(28.dp), tint = Boje.primary)
                Text(naslov, style = Tekst.section, color = Boje.ink)
            }
            sadrzaj()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Razmaci.s3, Alignment.End)) { dugmad() }
        }
    }
}
