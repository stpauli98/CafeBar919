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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
 * Količina (Paper V6 „Pravila rada“): [−] „60 s“ [+], razmak 8, dugmad 56×56 bez okvira, vrijednost
 * (krem, 18/700, tabularni brojevi) i jedinica (muted, 500) u jednom tekstu širine 110, centrirano.
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
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StepperDugme(Icons.Rounded.Remove, "Minus", minusOmogucen, onMinus)
        Text(
            buildAnnotatedString {
                append(vrijednost)
                if (jedinica.isNotEmpty()) {
                    append(" ")
                    withStyle(SpanStyle(color = Boje.muted, fontWeight = FontWeight.Medium)) { append(jedinica) }
                }
            },
            modifier = Modifier.width(110.dp).semantics { contentDescription = "$vrijednost $jedinica".trim() },
            style = Tekst.body.copy(
                fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum", textAlign = TextAlign.Center,
            ),
            color = Boje.ink,
        )
        StepperDugme(Icons.Rounded.Add, "Plus", plusOmogucen, onPlus)
    }
}
