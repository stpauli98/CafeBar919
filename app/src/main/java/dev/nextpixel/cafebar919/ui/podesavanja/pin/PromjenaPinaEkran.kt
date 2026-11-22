package dev.nextpixel.cafebar919.ui.podesavanja.pin

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.ui.components.PinUnos
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

/** V3 · Promjena PIN-a (Paper): lijevo naslov, stepper i pravila (340), desno natpis koraka i PIN pad (400). */
@Composable
fun PromjenaPinaEkran(vm: PromjenaPinaViewModel, toast: ToastStanje) {
    LaunchedEffect(vm) { vm.poruke.collect { toast.prikazi(it) } }
    val s by vm.stanje.collectAsState()

    Row(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(56.dp, Alignment.CenterHorizontally),
    ) {
        Column(Modifier.width(340.dp).padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                "Promjena PIN-a", color = Boje.ink,
                style = Tekst.body.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.02).em),
            )
            KorakPina.entries.forEach { Korak(it, s.korak) }
            Text(
                "PIN ima 6 cifara i zna ga samo vlasnik. Ne koristi datum rođenja ni 123456.",
                Modifier.padding(top = 8.dp), color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 21.sp),
            )
            Text(
                "Zaboravljen PIN niko ne može poništiti. Zapiši ga na sigurno mjesto.",
                color = Boje.muted, style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 21.sp),
            )
        }
        Column(Modifier.width(400.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(
                "Korak ${s.korak.broj} od 3 · ${s.korak.naziv}".uppercase(),
                color = Boje.primary,
                style = Tekst.body.copy(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em),
            )
            PinUnos(unesene = s.unesene, greska = s.greska, onCifra = vm::cifra, onBrisi = vm::brisi, omogucen = s.omogucen)
        }
    }
}

/** Red steppera: završen = zlatni krug sa kvačicom, aktivan = zlatni okvir i broj, budući = sivi okvir (Paper). */
@Composable
private fun Korak(korak: KorakPina, aktivan: KorakPina) {
    val zavrsen = korak.broj < aktivan.broj
    val tekuci = korak == aktivan
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val okvir = if (zavrsen || tekuci) Boje.primary else Boje.neutralLine
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(if (zavrsen) Boje.primary else Boje.bg).border(2.dp, okvir, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (zavrsen) {
                Icon(Icons.Rounded.Check, null, Modifier.size(18.dp), tint = Boje.onPrimary)
            } else {
                Text(
                    korak.broj.toString(), color = if (tekuci) Boje.primary else Boje.muted,
                    style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
        Text(
            korak.naziv, color = if (tekuci) Boje.ink else Boje.muted,
            style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = if (tekuci) FontWeight.Bold else FontWeight.Medium),
        )
    }
}
