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
private fun StepperDugme(ikona: ImageVector, opis: String, omoguceno: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(Razmaci.dodir)
            .clip(Oblici.md)
            .background(if (omoguceno) Boje.tile else Boje.disabledBg)
            .clickable(enabled = omoguceno, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = opis },
        contentAlignment = Alignment.Center,
    ) {
        Icon(ikona, null, Modifier.size(24.dp), tint = if (omoguceno) Boje.ink else Boje.disabledInk)
    }
}

/**
 * Količina (O2): [−] broj [+], dugmad 56×56 bez okvira, broj 24/800 u širini 48, razmak 4.
 * `jedinica` ne crta se (Paper je ne prikazuje, mjera je u redu pored); koristi se samo za čitač ekrana.
 */
@Composable
fun Stepper(
    vrijednost: String,
    jedinica: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    minusOmogucen: Boolean,
    plusOmogucen: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        StepperDugme(Icons.Rounded.Remove, "Minus", minusOmogucen, onMinus)
        Text(
            vrijednost,
            modifier = Modifier.width(48.dp).semantics { contentDescription = "$vrijednost $jedinica".trim() },
            style = Tekst.section.copy(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.sp),
            color = Boje.ink,
            textAlign = TextAlign.Center,
        )
        StepperDugme(Icons.Rounded.Add, "Plus", plusOmogucen, onPlus)
    }
}
