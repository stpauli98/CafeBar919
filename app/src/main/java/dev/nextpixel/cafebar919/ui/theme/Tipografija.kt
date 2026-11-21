package dev.nextpixel.cafebar919.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.R

private fun varijabilni(resurs: Int, vararg tezine: FontWeight) = FontFamily(
    tezine.map { Font(resurs, it, variationSettings = FontVariation.Settings(FontVariation.weight(it.weight))) },
)

/** Inter za cijeli UI (variable font, Latin Extended pokriva č, ć, š, ž, đ). */
val InterFamilija = varijabilni(
    R.font.inter,
    FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
)

/** Cinzel samo za znak „919 CAFFE BAR“. */
val CinzelFamilija = varijabilni(R.font.cinzel, FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold)

/** Veličine `--text-*`, razmak slova `--tracking-*` i visine redova iz Papera. */
object Tekst {
    private val osnova = TextStyle(fontFamily = InterFamilija, fontWeight = FontWeight.Normal)

    val caption = osnova.copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.08.em, fontWeight = FontWeight.SemiBold)
    val label = osnova.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.08.em, fontWeight = FontWeight.SemiBold)
    val body = osnova.copy(fontSize = 16.sp, lineHeight = 24.sp)
    val section = osnova.copy(fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
    val title = osnova.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
    val display = osnova.copy(fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
}
