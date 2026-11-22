package dev.nextpixel.cafebar919.ui.greska

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.ui.components.Znak919
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** T6 · BAZA-01. Tekstovi iz inventara, uz spec 7 #20 (pomoć kao običan tekst umjesto linka). */
@Composable
fun BazaGreskaEkran(onPonovo: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Boje.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .border(BorderStroke(1.dp, Boje.line))
                .padding(horizontal = Razmaci.s6),
            verticalAlignment = Alignment.CenterVertically,
        ) { Znak919() }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.width(400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Razmaci.s3),
            ) {
                Box(
                    modifier = Modifier.size(64.dp).background(Boje.dangerTint, Oblici.pill)
                        .border(BorderStroke(2.dp, Boje.danger), Oblici.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("!", color = Boje.danger, style = Tekst.title)
                }
                Text("Evidencija se ne može otvoriti", color = Boje.ink, style = Tekst.title, textAlign = TextAlign.Center)
                Text("Baza na tabletu se nije učitala pri pokretanju.", color = Boje.muted, style = Tekst.body, textAlign = TextAlign.Center)
                Text(
                    "Podaci su i dalje na tabletu. Ne briši aplikaciju i ne čisti njene podatke.",
                    color = Boje.ink,
                    style = Tekst.body.copy(fontWeight = FontWeight.SemiBold),
                    textAlign = TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Razmaci.dodir)
                        .clip(Oblici.md)
                        .background(Boje.primary)
                        .clickable(role = Role.Button, onClick = onPonovo),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Pokušaj ponovo", color = Boje.onPrimary, style = Tekst.body.copy(fontWeight = FontWeight.Bold))
                }
                Text("Pomoć: NextPixel · +387 66 603 900", color = Boje.muted, style = Tekst.body, textAlign = TextAlign.Center)
                Text("Šifra: BAZA-01", color = Boje.muted, style = Tekst.caption, textAlign = TextAlign.Center)
            }
        }
    }
}
