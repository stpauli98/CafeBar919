package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Pretraga pića (Paper O2): pill, okvir 1,5 `line` (2 `primary` u fokusu), padding 16, razmak 10, lupa 20 `muted`,
 * tekst 16/20, placeholder „Traži piće“. Visina 56 (dodirna zona; Paper crta 48). `ImeAction.Search` skida fokus (i tako sakriva tastaturu).
 */
@Suppress("DEPRECATION")
@Composable
fun PoljePretrage(upit: String, onUpit: (String) -> Unit, modifier: Modifier = Modifier) {
    val izvor = remember { MutableInteractionSource() }
    val fokus by izvor.collectIsFocusedAsState()
    val fokusUpravljac = LocalFocusManager.current
    val stil = Tekst.body.copy(fontSize = 16.sp, lineHeight = 20.sp)
    BasicTextField(
        value = upit,
        onValueChange = onUpit,
        modifier = modifier,
        singleLine = true,
        textStyle = stil.copy(color = Boje.ink),
        cursorBrush = SolidColor(Boje.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { fokusUpravljac.clearFocus() }),
        interactionSource = izvor,
        decorationBox = { unutra ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = Razmaci.dodir)
                    .border(if (fokus) 2.dp else 1.5.dp, if (fokus) Boje.primary else Boje.line, Oblici.pill)
                    .padding(horizontal = Razmaci.s4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Search, null, Modifier.size(20.dp), tint = Boje.muted)
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (upit.isEmpty()) Text("Traži piće", style = stil, color = Boje.placeholder)
                    unutra()
                }
            }
        },
    )
}
