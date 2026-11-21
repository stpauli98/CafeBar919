package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Radius 20 iz Papera (prozori); Paper ima prednost nad radijusom 16 iz plana. */
private val OblikProzora = RoundedCornerShape(20.dp)

/**
 * Prozor sa scrimom (Paper O3/V0): scrim crni 66 %, surface, radius 20, padding 28, razmak 14,
 * naslov 22/800, sjena 0 24 64 crna 50 %, red dugmadi desno sa razmakom 12 i 8 dp razmaka iznad.
 */
@Composable
fun Prozor(
    naslov: String,
    onZatvori: () -> Unit,
    sirina: Dp,
    ikona: ImageVector? = null,
    sadrzaj: @Composable ColumnScope.() -> Unit,
    dugmad: @Composable RowScope.() -> Unit,
) {
    Dialog(onDismissRequest = onZatvori, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        // Scrim crtamo sami (Paper: #000000A8), pa prozorska zatamnjenost sistema ide na nulu.
        val prozor = (LocalView.current.parent as? DialogWindowProvider)?.window
        val pogled = LocalView.current
        SideEffect {
            prozor?.setDimAmount(0f)
            // Aplikacija je imerzivna: ni prozor ne smije vratiti sistemske trake.
            prozor?.let {
                val kontroler = WindowCompat.getInsetsController(it, pogled)
                kontroler.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                kontroler.hide(WindowInsetsCompat.Type.systemBars())
            }
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.66f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onZatvori)
                // Tastatura podiže prozor; sadržaj se klizi, a dugmad ostaju vidljiva.
                .imePadding()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                Modifier
                    .width(sirina)
                    .shadow(32.dp, OblikProzora, ambientColor = Color.Black.copy(alpha = 0.5f), spotColor = Color.Black.copy(alpha = 0.5f))
                    .background(Boje.surface, OblikProzora)
                    .border(1.dp, Boje.line, OblikProzora)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (ikona != null) Icon(ikona, null, Modifier.size(24.dp), tint = Boje.primary)
                    Text(
                        naslov,
                        style = Tekst.body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.01).em),
                        color = Boje.ink,
                    )
                }
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) { sadrzaj() }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) { dugmad() }
            }
        }
    }
}
