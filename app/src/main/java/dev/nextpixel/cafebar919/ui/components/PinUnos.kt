package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

private const val DUZINA_PIN = 6

/**
 * 6 tačaka + tastatura 3×4 (1–9 / prazno 0 ⌫). Kad je `unesene == 6`, dalje cifre ignoriše pozivalac.
 * Greška: tačke prazne sa crvenim okvirom, ispod crveni tekst sa trouglom.
 */
@Composable
fun PinUnos(
    unesene: Int,
    greska: String?,
    onCifra: (Char) -> Unit,
    onBrisi: () -> Unit,
    omogucen: Boolean = true,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Razmaci.s4)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
            repeat(DUZINA_PIN) { i ->
                val ispunjena = greska == null && i < unesene
                Box(
                    Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (ispunjena) Boje.ink else Color.Transparent)
                        .border(1.5.dp, if (greska != null) Boje.danger else if (ispunjena) Boje.ink else Boje.neutralLine, CircleShape),
                )
            }
        }
        if (greska != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Razmaci.s2)) {
                Icon(Icons.Rounded.Warning, null, Modifier.size(18.dp), tint = Boje.danger)
                Text(greska, style = Tekst.body, color = Boje.danger)
            }
        }
        val redovi = listOf("123", "456", "789")
        Column(verticalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
            redovi.forEach { red ->
                Row(horizontalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
                    red.forEach { c -> Taster(omogucen, onClick = { onCifra(c) }) { TasterCifra(c, omogucen) } }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
                Spacer(Modifier.size(TASTER_SIRINA, Razmaci.dodir)) // prazna ćelija, nije dodirljiva
                Taster(omogucen, onClick = { onCifra('0') }) { TasterCifra('0', omogucen) }
                Taster(omogucen, onClick = onBrisi, opis = "Obriši cifru") {
                    Icon(Icons.AutoMirrored.Rounded.Backspace, null, Modifier.size(24.dp), tint = if (omogucen) Boje.ink else Boje.disabledInk)
                }
            }
        }
    }
}

private val TASTER_SIRINA = 88.dp

@Composable
private fun TasterCifra(c: Char, omogucen: Boolean) {
    Text(c.toString(), style = Tekst.section.copy(fontWeight = FontWeight.SemiBold), color = if (omogucen) Boje.ink else Boje.disabledInk)
}

@Composable
private fun Taster(omogucen: Boolean, onClick: () -> Unit, opis: String? = null, sadrzaj: @Composable () -> Unit) {
    Box(
        Modifier
            .size(TASTER_SIRINA, Razmaci.dodir)
            .clip(Oblici.md)
            .background(if (omogucen) Boje.tile else Boje.disabledBg)
            .border(1.dp, Boje.line, Oblici.md)
            .clickable(enabled = omogucen, role = Role.Button, onClick = onClick)
            .then(if (opis != null) Modifier.semantics { contentDescription = opis } else Modifier),
        contentAlignment = Alignment.Center,
    ) { sadrzaj() }
}
