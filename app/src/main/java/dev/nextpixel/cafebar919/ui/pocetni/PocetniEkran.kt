package dev.nextpixel.cafebar919.ui.pocetni

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.components.Znak919
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst
import java.time.LocalDate

private val DANI = listOf("Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota", "Nedjelja")

// Privremeno: Task 2 prebacuje formatiranje datuma u domain/Format.
private fun datumUTraci(datum: LocalDate): String =
    "${DANI[datum.dayOfWeek.value - 1]} ${datum.dayOfMonth}. ${datum.monthValue}."

/** Privremeni početni ekran faze 1: gornja traka kao na S0 i poruka u sredini. */
@Composable
fun PocetniEkran(danas: LocalDate, onPodesavanja: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Boje.bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .border(BorderStroke(1.dp, Boje.line))
                .padding(horizontal = Razmaci.s6),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Znak919()
            Box(Modifier.width(1.dp).height(32.dp).background(Boje.line))
            Text(datumUTraci(danas), color = Boje.ink, style = Tekst.section)
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .heightIn(min = Razmaci.dodir)
                    .clip(Oblici.md)
                    .border(BorderStroke(1.5.dp, Boje.neutralLine), Oblici.md)
                    .clickable(role = Role.Button, onClick = onPodesavanja)
                    .padding(horizontal = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = Boje.ink, modifier = Modifier.size(22.dp))
                Text("Podešavanja", color = Boje.ink, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Unos tura dolazi u sljedećoj fazi izrade.", color = Boje.muted, style = Tekst.body)
        }
    }
}
