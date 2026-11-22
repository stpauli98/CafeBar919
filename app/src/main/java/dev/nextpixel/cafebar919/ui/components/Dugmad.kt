package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Zajednička osnova dugmadi iz O2 (Paper): radius 12, visina najmanje 56, okvir 1,5, razmak 10,
 * padding 22, ikona 22, spinner 20. Fokus = 3 px razmak boje pozadine + 2 px zlatni prsten (spolja).
 */
@Composable
private fun DugmeOsnova(
    tekst: String,
    onClick: () -> Unit,
    modifier: Modifier,
    ikona: ImageVector?,
    omoguceno: Boolean,
    upisuje: Boolean,
    podloga: Color,
    podlogaPritisnuto: Color,
    boja: Color,
    okvir: Color,
) {
    val izvor = remember { MutableInteractionSource() }
    val pritisnuto by izvor.collectIsPressedAsState()
    val fokus by izvor.collectIsFocusedAsState()
    val aktivno = omoguceno || upisuje // „u toku“ zadržava boju, ali ne prima klik
    val pozadina = when {
        !aktivno -> Boje.disabledBg
        pritisnuto -> podlogaPritisnuto
        else -> podloga
    }
    val tekstBoja = if (aktivno) boja else Boje.disabledInk
    val bojaOkvira = if (aktivno) okvir else Color.Transparent
    Row(
        modifier = modifier
            .heightIn(min = Razmaci.dodir)
            .then(if (fokus && omoguceno) Modifier.drawBehind { crtajPrsten() } else Modifier)
            .clip(Oblici.md)
            .background(pozadina)
            .border(BorderStroke(1.5.dp, bojaOkvira), Oblici.md)
            .clickable(
                interactionSource = izvor,
                indication = null,
                enabled = omoguceno && !upisuje,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
    ) {
        if (upisuje) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = tekstBoja, trackColor = tekstBoja.copy(alpha = 0.3f), strokeWidth = 2.4.dp)
        } else if (ikona != null) {
            Icon(ikona, contentDescription = null, modifier = Modifier.size(22.dp), tint = tekstBoja)
        }
        Text(
            if (upisuje) "Upisuje se…" else tekst,
            style = Tekst.body.copy(fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
            color = tekstBoja,
        )
    }
}

/** Fokus prsten iz Papera: `0 0 0 3px bg, 0 0 0 5px primary` (iscrtava se izvan dugmeta). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.crtajPrsten() {
    val r = 12.dp.toPx()
    fun prsten(razmak: Float, debljina: Float, boja: Color) {
        val sredina = razmak - debljina / 2f
        drawRoundRect(
            color = boja,
            topLeft = Offset(-sredina, -sredina),
            size = Size(size.width + 2 * sredina, size.height + 2 * sredina),
            cornerRadius = CornerRadius(r + sredina),
            style = Stroke(debljina),
        )
    }
    prsten(3.dp.toPx(), 3.dp.toPx(), Boje.bg)
    prsten(5.dp.toPx(), 2.dp.toPx(), Boje.primary)
}

/** Glavno (zlatno) dugme. `upisuje` = „Upisuje se…“ sa spinnerom, klik se ignoriše. */
@Composable
fun GlavnoDugme(
    tekst: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ikona: ImageVector? = null,
    omoguceno: Boolean = true,
    upisuje: Boolean = false,
) = DugmeOsnova(tekst, onClick, modifier, ikona, omoguceno, upisuje, Boje.primary, Boje.primaryPressed, Boje.onPrimary, Color.Transparent)

/** Sporedno dugme: providno, okvir #444. */
@Composable
fun SporednoDugme(
    tekst: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ikona: ImageVector? = null,
    omoguceno: Boolean = true,
) = DugmeOsnova(tekst, onClick, modifier, ikona, omoguceno, false, Color.Transparent, Boje.pressed, Boje.ink, Boje.neutralLine)

/** Opasno dugme: crveni tekst i okvir (danger-line). */
@Composable
fun OpasnoDugme(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
    DugmeOsnova(tekst, onClick, modifier, null, true, false, Color.Transparent, Boje.dangerTint, Boje.danger, Boje.dangerLine)
