package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

@Composable
private fun TureDugme(ikona: ImageVector, opis: String, omoguceno: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(Razmaci.dodir)
            .clip(Oblici.md)
            .background(if (omoguceno) Boje.tile else Boje.disabledBg)
            .semantics { contentDescription = opis }
            .clickable(enabled = omoguceno, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(ikona, null, Modifier.size(24.dp), tint = if (omoguceno) Boje.ink else Boje.disabledInk) }
}

/** Količina u stavci ture (Paper O2): [−] 56×56, broj 24/800 širine 48, [+] 56×56, razmak 4. Minus je uvijek omogućen. */
@Composable
fun StepperTure(kolicina: Int, onMinus: () -> Unit, onPlus: () -> Unit, plusOmogucen: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TureDugme(Icons.Rounded.Remove, "Manje", true, onMinus)
        Text(
            kolicina.toString(),
            Modifier.width(48.dp),
            style = Tekst.body.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold),
            color = Boje.ink,
            textAlign = TextAlign.Center,
        )
        TureDugme(Icons.Rounded.Add, "Više", plusOmogucen, onPlus)
    }
}
