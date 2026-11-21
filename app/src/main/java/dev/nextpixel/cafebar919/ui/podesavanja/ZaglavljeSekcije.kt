package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** Zaglavlje radnog dijela V4/V5 (Paper): naslov 22/800, podnaslov 14/20 sivo, zlatno dugme desno. */
@Composable
internal fun ZaglavljeSekcije(naslov: String, podnaslov: String, dugme: String, onDugme: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(naslov, color = Boje.ink, style = Tekst.body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold))
            Text(podnaslov, color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp))
        }
        GlavnoDugme(dugme, onDugme, ikona = Icons.Rounded.Add)
    }
}

/** „›“ na kraju reda: otvara izmjenu. Dodirna zona 56×56 (glif ostaje 24 dp). */
@Composable
internal fun DugmeIzmjene(naziv: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(Razmaci.dodir, Razmaci.dodir)
            .semantics { contentDescription = "Izmijeni $naziv" }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Rounded.ChevronRight, null, Modifier.size(24.dp), tint = Boje.muted) }
}
