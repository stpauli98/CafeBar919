package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Razmaci

/** Prekidač 52×32 u dodirnoj zoni 56×56. `opis` je opis za čitač ekrana (inače „Prekidač“). */
@Composable
fun Prekidac(ukljucen: Boolean, onPromjena: (Boolean) -> Unit, omogucen: Boolean = true, opis: String? = null) {
    val traka = when {
        !omogucen -> Boje.disabledBg
        ukljucen -> Boje.primary
        else -> Boje.neutralLine
    }
    Box(
        modifier = Modifier
            .size(Razmaci.dodir)
            .semantics { contentDescription = opis ?: "Prekidač" }
            .toggleable(value = ukljucen, enabled = omogucen, role = Role.Switch, onValueChange = onPromjena),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(52.dp, 32.dp).clip(CircleShape).background(traka)) {
            Box(
                Modifier
                    .align(if (ukljucen) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (!omogucen) Boje.disabledInk else if (ukljucen) Boje.onPrimary else Boje.ink),
            )
        }
    }
}
