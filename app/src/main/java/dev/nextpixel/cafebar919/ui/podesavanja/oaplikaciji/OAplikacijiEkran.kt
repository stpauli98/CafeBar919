package dev.nextpixel.cafebar919.ui.podesavanja.oaplikaciji

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.BuildConfig
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** V7 · O aplikaciji (Paper): naslov, tabela ključ–vrijednost (redovi 56), kontakt kartica. Samo za čitanje. */
@Composable
fun OAplikacijiEkran(repo: PodesavanjaRepo, verzija: String = BuildConfig.VERSION_NAME) {
    val postavke by repo.postavke.collectAsState(null)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("O aplikaciji", color = Boje.ink, style = Tekst.body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold))
            Text("Evidencija pića za Caffe Bar 919.", color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp))
        }
        Column(Modifier.fillMaxWidth()) {
            Red("Verzija", verzija)
            Red("Podaci", "samo na ovom tabletu, bez interneta")
            Red("Istorija", postavke?.let { "${it.istorijaDana} dana · podešava se u Pravilima rada" } ?: "")
            Red("Izradio", "NextPixel · www.nextpixel.dev")
        }
        Kontakt()
    }
}

@Composable
private fun Red(kljuc: String, vrijednost: String) {
    Row(
        Modifier.fillMaxWidth().height(56.dp).drawBehind {
            drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx())
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(kljuc, Modifier.width(200.dp), color = Boje.muted, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp))
        Text(vrijednost, Modifier.weight(1f), color = Boje.ink, style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold))
    }
}

/** Kartica kontakta: broj je običan tekst, nije klikabilan (tablet nije telefon). */
@Composable
private fun Kontakt() {
    Row(
        Modifier.fillMaxWidth().clip(Oblici.lg).background(Boje.surface).border(1.dp, Boje.line, Oblici.lg).padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Boje.primaryTint), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Phone, null, Modifier.size(26.dp), tint = Boje.primary)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Pomoć oko aplikacije · NextPixel, Nikola", color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp))
            Text(
                "+387 66 603 900", color = Boje.ink,
                style = Tekst.body.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, fontFeatureSettings = "tnum"),
            )
            Text(
                "Za greške u radu aplikacije i kad se evidencija ne otvara (BAZA-01). Zaboravljen PIN se ne može poništiti.",
                color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
            )
        }
    }
}
