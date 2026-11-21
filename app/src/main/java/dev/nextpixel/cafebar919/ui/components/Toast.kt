package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Krem toast (Paper O3): širina 400, padding 14/16, radius 12, razmak 12, sjena, zelena kružna kvačica 20
 * sa tamnim znakom, tekst 15 regular, akcija 14 bold (toast-action). Nestaje poslije 4 s.
 * Dodirna zona akcije je 56 dp visoka.
 */
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
    val oblik = Oblici.md
    Row(
        modifier
            .width(400.dp)
            .shadow(24.dp, oblik, ambientColor = Color.Black.copy(alpha = 0.25f), spotColor = Color.Black.copy(alpha = 0.25f))
            .background(Boje.ink, oblik)
            .padding(start = Razmaci.s4, end = if (toast.akcija != null) Razmaci.s2 else Razmaci.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Canvas(Modifier.size(20.dp)) {
            val k = size.width / 20f
            drawCircle(Boje.success, 9f * k, Offset(10f * k, 10f * k))
            val kvacica = Path().apply { moveTo(6f * k, 10.5f * k); lineTo(8.6f * k, 13.1f * k); lineTo(14f * k, 7.6f * k) }
            drawPath(kvacica, Boje.bg, style = Stroke(2f * k, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        Text(
            toast.tekst,
            modifier = Modifier.weight(1f).padding(vertical = 14.dp),
            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 20.sp),
            color = Boje.onPrimary,
        )
        if (toast.akcija != null) {
            Box(
                Modifier
                    .heightIn(min = Razmaci.dodir)
                    .clickable(role = Role.Button) { toast.onAkcija(); stanje.ukloni(toast) }
                    .padding(horizontal = Razmaci.s2),
                contentAlignment = Alignment.Center,
            ) {
                Text(toast.akcija, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold), color = Boje.toastAction)
            }
        }
    }
}
