package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

enum class VrstaBanera { UPOZORENJE, INFO, GRESKA }

/**
 * Baner (O3/O4, Paper): padding 12/14, radius 12, okvir 1, razmak 12, ikona 20 (popunjena sa tamnim znakom),
 * naslov (bold) i tekst u istom redu, oba 14/20 krem. Dugme je vizuelno 36 dp (radius 8, podloga bg,
 * okvir boje banera), a dodirna zona je najmanje 56 dp.
 */
@Composable
fun Baner(vrsta: VrstaBanera, naslov: String, tekst: String, dugme: String? = null, onDugme: () -> Unit = {}) {
    val podloga: Color
    val okvir: Color
    when (vrsta) {
        VrstaBanera.UPOZORENJE -> { podloga = Boje.warningTint; okvir = Boje.warningLine }
        VrstaBanera.INFO -> { podloga = Boje.primaryTint; okvir = Boje.infoLine }
        VrstaBanera.GRESKA -> { podloga = Boje.dangerTint; okvir = Boje.dangerLine }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Oblici.md)
            .background(podloga)
            .border(1.dp, okvir, Oblici.md)
            .padding(horizontal = 14.dp, vertical = if (dugme != null) 2.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Canvas(Modifier.size(20.dp)) { crtajIkonu(vrsta) }
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(naslov) }
                append(" ")
                append(tekst)
            },
            modifier = Modifier.weight(1f).padding(vertical = if (dugme != null) 8.dp else 0.dp),
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = Boje.ink,
        )
        if (dugme != null) {
            Box(
                Modifier
                    .heightIn(min = Razmaci.dodir)
                    .clickable(role = Role.Button, onClick = onDugme)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .height(36.dp)
                        .clip(Oblici.sm)
                        .background(Boje.bg)
                        .border(1.dp, okvir, Oblici.sm)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(dugme, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold), color = Boje.ink)
                }
            }
        }
    }
}


/** Ikone iz Papera (viewBox 20×20): trougao upozorenja, „i“ u krugu, „×“ u krugu; znak je tamni. */
private fun DrawScope.crtajIkonu(vrsta: VrstaBanera) {
    val k = size.width / 20f
    fun o(x: Float, y: Float) = Offset(x * k, y * k)
    val znak = Boje.bg
    val debljina = 2f * k
    when (vrsta) {
        VrstaBanera.UPOZORENJE -> {
            val trougao = Path().apply { moveTo(10f * k, 2f * k); lineTo(19f * k, 18f * k); lineTo(1f * k, 18f * k); close() }
            drawPath(trougao, Boje.warning)
            drawLine(znak, o(10f, 8f), o(10f, 12.5f), debljina, StrokeCap.Round)
            drawCircle(znak, 1.1f * k, o(10f, 15f))
        }
        VrstaBanera.INFO -> {
            drawCircle(Boje.primary, 9f * k, o(10f, 10f))
            drawLine(znak, o(10f, 9f), o(10f, 14f), debljina, StrokeCap.Round)
            drawCircle(znak, 1.2f * k, o(10f, 6f))
        }
        VrstaBanera.GRESKA -> {
            drawCircle(Boje.danger, 9f * k, o(10f, 10f))
            drawLine(znak, o(7f, 7f), o(13f, 13f), debljina, StrokeCap.Round)
            drawLine(znak, o(13f, 7f), o(7f, 13f), debljina, StrokeCap.Round)
        }
    }
}
