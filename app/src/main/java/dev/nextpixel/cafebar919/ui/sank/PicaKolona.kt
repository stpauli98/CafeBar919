package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsBar
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Nacrt
import dev.nextpixel.cafebar919.ui.components.MjeraNaPlocici
import dev.nextpixel.cafebar919.ui.components.PlocicaPica
import dev.nextpixel.cafebar919.ui.components.PoljePretrage
import dev.nextpixel.cafebar919.ui.components.Pilula
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Tekst

/**
 * Srednja kolona S1 (Paper: padding 20, razmak 16): red „Sve“ + kategorije i pretraga, ispod mreža 3 kolone
 * (pločice 192×120, razmak 12). Bez pića u katalogu (T2) nema ni čipova ni pretrage; pretraga bez rezultata je T1.
 */
@Composable
internal fun PicaKolona(
    s: UnosStanje,
    vm: UnosViewModel,
    onPodesavanja: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!s.imaPica) {
        Box(modifier.padding(20.dp), contentAlignment = Alignment.Center) { NemaPica(onPodesavanja) }
        return
    }
    val fokus = LocalFocusManager.current
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LazyRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item(key = "sve") { Pilula("Sve", s.izabranaKategorija == null) { vm.izaberiKategoriju(null) } }
                items(s.kategorije, key = { it.id }) { k ->
                    Pilula(k.naziv, s.izabranaKategorija == k.id) { vm.izaberiKategoriju(k.id) }
                }
            }
            PoljePretrage(s.upit, vm::upit, Modifier.width(if (s.upit.isEmpty()) 150.dp else 200.dp))
        }
        if (s.pica.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { NemaRezultata(s.upit) { vm.upit("") } }
        } else {
            LazyVerticalGrid(
                GridCells.Fixed(3),
                Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(s.pica, key = { it.a.id }) { p ->
                    PlocicaPica(
                        naziv = p.a.naziv,
                        mjere = p.mjere.map { m ->
                            MjeraNaPlocici(m.id, m.natpis, Format.mjeraNaPlocici(m.naziv, m.litara), Nacrt.kolicina(s.stavke, p.a.id, m.id))
                        },
                        onDodaj = { mjeraId -> fokus.clearFocus(); vm.dodaj(p.a.id, mjeraId) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private val NaslovStanja = Tekst.body.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)

/** Prazno stanje (Paper T1/T2): krug 72 `tile` sa ikonom 30, naslov 22/800, opis 16 širine najviše 380, dugme. */
@Composable
private fun PraznoStanje(ikona: androidx.compose.ui.graphics.vector.ImageVector, naslov: String, opis: String, dugme: @Composable () -> Unit) {
    Column(
        Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
    ) {
        Box(Modifier.size(72.dp).clip(Oblici.pill).background(Boje.tile), contentAlignment = Alignment.Center) {
            Icon(ikona, null, Modifier.size(30.dp), tint = Boje.muted)
        }
        Text(naslov, color = Boje.ink, style = NaslovStanja)
        Text(
            opis,
            Modifier.widthIn(max = 380.dp),
            color = Boje.muted,
            style = Tekst.body.copy(textAlign = TextAlign.Center),
        )
        dugme()
    }
}

@Composable
private fun NemaPica(onPodesavanja: () -> Unit) = PraznoStanje(
    Icons.Outlined.SportsBar,
    "Još nema pića",
    "Prije prve ture vlasnik unosi pića i mjere u podešavanjima. Treba mu PIN.",
) { GlavnoDugme("Podešavanja", onPodesavanja) }

@Composable
private fun NemaRezultata(upit: String, onOcisti: () -> Unit) = PraznoStanje(
    Icons.Rounded.Search,
    "Nema pića „$upit“",
    "Provjeri naziv ili traži po prvim slovima. Novo piće dodaje vlasnik u podešavanjima.",
) { SporednoDugme("Očisti pretragu", onOcisti) }
