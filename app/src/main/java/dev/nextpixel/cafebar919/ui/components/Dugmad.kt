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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Zajednička osnova dugmadi iz O2: radius 12, najmanje 56 dp, ikona 22 dp lijevo od teksta. */
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
    okvir: Color?,
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
    val ivica = when {
        fokus && omoguceno -> BorderStroke(2.dp, Boje.primary)
        okvir != null -> BorderStroke(1.dp, if (aktivno) okvir else Boje.line)
        else -> null
    }
    Row(
        modifier = modifier
            .heightIn(min = Razmaci.dodir)
            .clip(Oblici.md)
            .background(pozadina)
            .then(if (ivica != null) Modifier.border(ivica, Oblici.md) else Modifier)
            .clickable(
                interactionSource = izvor,
                indication = null,
                enabled = omoguceno && !upisuje,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = Razmaci.s6),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s2, Alignment.CenterHorizontally),
    ) {
        if (upisuje) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = tekstBoja, strokeWidth = 2.dp)
        } else if (ikona != null) {
            Icon(ikona, contentDescription = null, modifier = Modifier.size(22.dp), tint = tekstBoja)
        }
        Text(if (upisuje) "Upisuje se…" else tekst, style = Tekst.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = tekstBoja)
    }
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
) = DugmeOsnova(tekst, onClick, modifier, ikona, omoguceno, upisuje, Boje.primary, Boje.primaryPressed, Boje.onPrimary, null)

/** Sporedno dugme: providno, okvir #444. */
@Composable
fun SporednoDugme(
    tekst: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ikona: ImageVector? = null,
    omoguceno: Boolean = true,
) = DugmeOsnova(tekst, onClick, modifier, ikona, omoguceno, false, Color.Transparent, Boje.pressed, Boje.ink, Boje.neutralLine)

/** Opasno dugme: crveni tekst i okvir. */
@Composable
fun OpasnoDugme(tekst: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
    DugmeOsnova(tekst, onClick, modifier, null, true, false, Color.Transparent, Boje.dangerTint, Boje.danger, Boje.dangerLine)
