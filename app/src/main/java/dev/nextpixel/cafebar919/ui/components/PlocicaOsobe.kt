package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Pločica osobe (Paper O2 „Konobar i Šank“): 228×80, radius 16, padding 16, ime 20/700, zbir 14/500.
 * Izabrana = zlatna podloga, tamni tekst i crni krug 28 sa zlatnom kvačicom 18. Pritisnuta = `pressed`.
 * `sank` = providno, isprekidan okvir 1,5 `neutralLine`.
 */
@Composable
fun PlocicaOsobe(
    ime: String,
    zbir: String,
    izabrana: Boolean,
    sank: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val izvor = remember { MutableInteractionSource() }
    val pritisnuta by izvor.collectIsPressedAsState()
    // Izabran Šank izgleda isto kao izabran konobar (spec 7 #29); isprekidan okvir samo kad nije izabran.
    val isprekidan = sank && !izabrana
    val pozadina = when {
        izabrana -> Boje.primary
        sank -> Color.Transparent
        pritisnuta -> Boje.pressed
        else -> Boje.tile
    }
    val imeBoja = if (izabrana) Boje.onPrimary else Boje.ink
    val zbirBoja = if (izabrana) Boje.onPrimary else Boje.muted
    Row(
        modifier = modifier
            .width(228.dp)
            .height(80.dp)
            .clip(Oblici.lg)
            .background(pozadina)
            .then(
                if (isprekidan) Modifier.drawBehind {
                    val s = 1.5.dp.toPx()
                    drawRoundRect(
                        color = Boje.neutralLine,
                        topLeft = Offset(s / 2, s / 2),
                        size = Size(size.width - s, size.height - s),
                        cornerRadius = CornerRadius(16.dp.toPx() - s / 2),
                        style = Stroke(width = s, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))),
                    )
                } else Modifier.border(1.5.dp, Color.Transparent, Oblici.lg),
            )
            .selectable(selected = izabrana, interactionSource = izvor, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(ime, style = Tekst.body.copy(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold), color = imeBoja, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(zbir, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium), color = zbirBoja, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (izabrana) {
            Box(Modifier.size(28.dp).clip(Oblici.pill).background(Color(0xFF191919)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, "Izabrano", Modifier.size(18.dp), tint = Boje.primary)
            }
        }
    }
}
