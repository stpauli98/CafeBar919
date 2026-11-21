package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst
import kotlinx.coroutines.delay

private const val TRAJANJE_MS = 4_000L

internal class ToastPodaci(val tekst: String, val akcija: String?, val onAkcija: () -> Unit)

/** Drži trenutni toast; novi `prikazi` zamjenjuje stari. */
class ToastStanje {
    internal var trenutni by mutableStateOf<ToastPodaci?>(null)
        private set

    fun prikazi(tekst: String, akcija: String? = null, onAkcija: () -> Unit = {}) {
        trenutni = ToastPodaci(tekst, akcija, onAkcija)
    }

    internal fun ukloni(podaci: ToastPodaci) {
        if (trenutni === podaci) trenutni = null
    }
}

/** Krem toast sa zelenom kvačicom (O3), nestaje poslije 4 s. */
@Composable
fun ToastHost(stanje: ToastStanje, modifier: Modifier = Modifier) {
    val toast = stanje.trenutni
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(TRAJANJE_MS)
            stanje.ukloni(toast)
        }
    }
    if (toast == null) return
    Row(
        modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.md)
            .background(Boje.ink)
            .padding(start = Razmaci.s4, end = if (toast.akcija != null) 0.dp else Razmaci.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Box(Modifier.size(24.dp).clip(CircleShape).background(Boje.successTint), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Check, null, Modifier.size(16.dp), tint = Boje.success)
        }
        Text(toast.tekst, style = Tekst.body.copy(fontWeight = FontWeight.SemiBold), color = Boje.onPrimary)
        if (toast.akcija != null) {
            Box(
                Modifier
                    .heightIn(min = Razmaci.dodir)
                    .clickable(role = Role.Button) { toast.onAkcija(); stanje.ukloni(toast) }
                    .padding(horizontal = Razmaci.s4),
                contentAlignment = Alignment.Center,
            ) {
                Text(toast.akcija, style = Tekst.body.copy(fontWeight = FontWeight.Bold), color = Boje.toastAction)
            }
        }
    }
}
