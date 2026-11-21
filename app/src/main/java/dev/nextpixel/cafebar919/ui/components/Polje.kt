package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Tekstualno polje (O6, Paper): labela 14/600 iznad (razmak 8), polje visine 56 bez ispune, okvir 1,5
 * (2 pri fokusu/grešci), tekst i placeholder 18/22, greška 14 sa trouglom 16 (razmak 6).
 * `max` ograničava unos; brojač „n/max“ se vidi tek kad je `brojacOd != null && n >= brojacOd`.
 */
@Suppress("DEPRECATION") // vrijednost/onPromjena potpis je zadat planom (value-based overload)
@Composable
fun Polje(
    labela: String,
    vrijednost: String,
    onPromjena: (String) -> Unit,
    greska: String? = null,
    placeholder: String = "",
    max: Int? = null,
    brojacOd: Int? = null,
    tastatura: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
) {
    val izvor = remember { MutableInteractionSource() }
    val dodir = LocalDodir.current
    val fokus by izvor.collectIsFocusedAsState()
    val okvir = when {
        greska != null -> Boje.danger
        fokus -> Boje.primary
        else -> Boje.line
    }
    val stilTeksta = Tekst.body.copy(fontSize = 18.sp, lineHeight = 22.sp)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Razmaci.s2)) {
        Text(
            labela,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
            color = if (greska != null) Boje.danger else Boje.ink,
        )
        BasicTextField(
            value = vrijednost,
            onValueChange = {
                dodir()
                if (max == null || it.length <= max) onPromjena(it)
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = stilTeksta.copy(color = Boje.ink),
            cursorBrush = SolidColor(Boje.primary),
            keyboardOptions = KeyboardOptions(keyboardType = tastatura),
            interactionSource = izvor,
            decorationBox = { unutra ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = Razmaci.dodir)
                        .border(if (fokus || greska != null) 2.dp else 1.5.dp, okvir, Oblici.md)
                        .padding(horizontal = Razmaci.s4),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (vrijednost.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, style = stilTeksta, color = Boje.placeholder)
                    unutra()
                }
            },
        )
        val brojac = max != null && brojacOd != null && vrijednost.length >= brojacOd
        if (greska != null || brojac) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (greska != null) {
                    Icon(Icons.Outlined.Warning, null, Modifier.size(16.dp), tint = Boje.danger)
                    Text(greska, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp), color = Boje.danger, modifier = Modifier.weight(1f))
                } else {
                    Box(Modifier.weight(1f))
                }
                if (brojac) Text("${vrijednost.length}/$max", style = Tekst.label, color = Boje.muted)
            }
        }
    }
}
