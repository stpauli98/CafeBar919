package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.data.VrstaPrimaoca
import dev.nextpixel.cafebar919.ui.components.PlocicaOsobe
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Tekst

internal val NatpisGrupe = Tekst.body.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em)

/**
 * Lijeva kolona S1 (Paper: 276, desna linija 1 `line`, padding 20/24, razmak 10): grupe KONOBARI, ZA ŠANKOM i ISPOMOĆ
 * (samo one koje imaju nekoga) se skrolaju, a „VAN ZBIRA“ sa Šankom stoji dole van skrola.
 */
@Composable
internal fun KoKolona(s: UnosStanje, onIzaberi: (Long) -> Unit, modifier: Modifier = Modifier) {
    val grupe = listOf(
        "Konobari" to VrstaPrimaoca.KONOBAR,
        "Za šankom" to VrstaPrimaoca.ZA_SANKOM,
        "Ispomoć" to VrstaPrimaoca.ISPOMOC,
    ).map { (naslov, vrsta) -> naslov to s.primaoci.filter { it.vrsta == vrsta } }.filter { it.second.isNotEmpty() }
    val sank = s.primaoci.filter { it.vrsta == VrstaPrimaoca.SANK }
    val fokus = LocalFocusManager.current
    val izaberi = { id: Long -> fokus.clearFocus(); onIzaberi(id) }
    Column(
        modifier
            .width(276.dp)
            .fillMaxHeight()
            .drawBehind { drawLine(Boje.line, Offset(size.width - 0.5.dp.toPx(), 0f), Offset(size.width - 0.5.dp.toPx(), size.height), 1.dp.toPx()) }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(0.dp)) {
            grupe.forEachIndexed { i, (naslov, lista) ->
                item(key = "n-$naslov") {
                    Text(naslov.uppercase(), color = Boje.muted, style = NatpisGrupe, modifier = Modifier.padding(top = if (i == 0) 0.dp else 10.dp))
                }
                items(lista, key = { it.id }) { p ->
                    PlocicaOsobe(p.ime, s.zbirovi[p.id] ?: "još ništa", izabrana = s.primalacId == p.id, sank = false, onClick = { izaberi(p.id) })
                }
            }
        }
        if (sank.isNotEmpty()) {
            Text("Van zbira".uppercase(), color = Boje.muted, style = NatpisGrupe)
            sank.forEach { p ->
                PlocicaOsobe(p.ime, s.zbirovi[p.id] ?: "prosuto i ostalo", izabrana = s.primalacId == p.id, sank = true, onClick = { izaberi(p.id) })
            }
        }
    }
}
