package dev.nextpixel.cafebar919.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.CinzelFamilija
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci

/** Znak iz Papera: zlatni krug sa „919“ i natpis „CAFFE BAR“ (Cinzel). */
@Composable
fun Znak919(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Razmaci.s3),
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(Boje.primary, Oblici.pill),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "919",
                color = Boje.onPrimary,
                style = TextStyle(fontFamily = CinzelFamilija, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
            )
        }
        Text(
            text = "CAFFE BAR",
            color = Boje.ink,
            style = TextStyle(fontFamily = CinzelFamilija, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.08.em),
        )
    }
}
