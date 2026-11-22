package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Jedna mjera na pločici: `natpis` je tekst dugmeta („0,03“, „Bokal“), `opis` red ispod naziva („doza 0,03 l“). */
data class MjeraNaPlocici(val mjeraId: Long, val natpis: String, val opis: String, val kolicina: Int)

private val NazivStil = Tekst.body.copy(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)

/**
 * Pločica pića (Paper O2): visina najmanje 120, radius 16, okvir 2, naziv 18/600 (najviše dva reda), opis 14/500.
 * Jedna mjera: cijela pločica je dugme, u turi zlatni okvir i značka-broj. 2–3 mjere: red dugmadi visine 56
 * (u turi tint + zlatni okvir 1,5, natpis „natpis · n“).
 */
@Composable
fun PlocicaPica(
    naziv: String,
    mjere: List<MjeraNaPlocici>,
    onDodaj: (mjeraId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uTuri = mjere.any { it.kolicina > 0 }
    val okvir = if (uTuri) Boje.primary else Color.Transparent
    if (mjere.size == 1) {
        val mjera = mjere.single()
        val izvor = remember { MutableInteractionSource() }
        val pritisnuta by izvor.collectIsPressedAsState()
        Column(
            modifier = modifier
                .heightIn(min = 120.dp)
                .clip(Oblici.lg)
                .background(if (pritisnuta) Boje.pressed else Boje.tile)
                .border(2.dp, okvir, Oblici.lg)
                .clickable(interactionSource = izvor, indication = null, role = Role.Button) { onDodaj(mjera.mjeraId) }
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                Text(naziv, Modifier.weight(1f), style = NazivStil, color = Boje.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (mjera.kolicina > 0) {
                    Box(
                        Modifier.height(28.dp).widthIn(min = 28.dp).clip(Oblici.pill).background(Boje.primary).padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            mjera.kolicina.toString(),
                            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.ExtraBold),
                            color = Boje.onPrimary,
                        )
                    }
                }
            }
            Text(mjera.opis, style = OpisStil, color = Boje.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    } else {
        Column(
            modifier = modifier
                .heightIn(min = 120.dp)
                .clip(Oblici.lg)
                .background(Boje.tile)
                .border(2.dp, okvir, Oblici.lg)
                .padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(naziv, Modifier.padding(horizontal = 10.dp), style = NazivStil, color = Boje.ink, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                mjere.forEach { m ->
                    val izabrano = m.kolicina > 0
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = Razmaci.dodir)
                            .clip(Oblici.md)
                            .background(if (izabrano) Boje.primaryTint else Boje.bg)
                            .border(1.5.dp, if (izabrano) Boje.primary else Color.Transparent, Oblici.md)
                            .clickable(role = Role.Button) { onDodaj(m.mjeraId) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (izabrano) Format.natpisUTuri(m.natpis, m.kolicina) else m.natpis,
                            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
                            color = if (izabrano) Boje.primary else Boje.ink,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

private val OpisStil = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
