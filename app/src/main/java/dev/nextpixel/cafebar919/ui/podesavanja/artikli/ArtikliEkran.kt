package dev.nextpixel.cafebar919.ui.podesavanja.artikli

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.repo.ArtikalPrikaz
import dev.nextpixel.cafebar919.ui.components.GlavnoDugme
import dev.nextpixel.cafebar919.ui.components.Pilula
import dev.nextpixel.cafebar919.ui.components.Prekidac
import dev.nextpixel.cafebar919.ui.components.Prozor
import dev.nextpixel.cafebar919.ui.components.OpasnoDugme
import dev.nextpixel.cafebar919.ui.components.SporednoDugme
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Oblici
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/** V1 · Artikli (Paper): filter pilule, „Novi artikal“, lista sa ručicom, prekidačem i „›“, pomoćni tekst. */
@Composable
fun ArtikliEkran(vm: ArtikliViewModel, toast: ToastStanje) {
    LaunchedEffect(vm) { vm.poruke.collect { toast.prikazi(it) } }

    val lista by vm.lista.collectAsState()
    val filter by vm.filter.collectAsState()
    val kategorije by vm.kategorije.collectAsState()
    val brojAktivnih by vm.brojAktivnih.collectAsState()
    val prozor by vm.prozor.collectAsState()
    val iskljucivanje by vm.potvrdaIskljucivanja.collectAsState()

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(Razmaci.s3)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Razmaci.s2)) {
            Row(
                Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Razmaci.s2),
            ) {
                Pilula("Sve · $brojAktivnih", filter == null, { vm.postaviFilter(null) })
                kategorije.filter { it.k.aktivna }.forEach { kp ->
                    Pilula(kp.k.naziv, filter == kp.k.id, { vm.postaviFilter(kp.k.id) })
                }
            }
            GlavnoDugme("Novi artikal", vm::otvoriNovi, ikona = Icons.Rounded.Add)
        }
        val artikli = lista
        if (artikli != null && artikli.isEmpty()) {
            Text("Još nema pića.", color = Boje.muted, style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 20.sp), modifier = Modifier.padding(top = Razmaci.s4))
        } else if (artikli != null) {
            ListaArtikala(artikli, onPrebaci = vm::prebaci, onIzmjena = vm::otvoriIzmjenu, onPomjeri = vm::pomjeri, modifier = Modifier.weight(1f, fill = false))
        }
        Text(
            "Drži ručicu i povuci da promijeniš redoslijed pločica na šanku. Isključeno piće se ne nudi na šanku, a stare noći ga i dalje prikazuju.",
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
        )
    }

    prozor?.let { forma ->
        ArtikalProzor(
            forma = forma,
            kategorije = kategorije.map { it.k },
            mjere = vm.mjere.collectAsState().value,
            vm = vm,
        )
    }
    iskljucivanje?.let { a ->
        Prozor(
            naslov = "Isključiti ${a.a.naziv}?",
            onZatvori = vm::odustaniOdIskljucivanja,
            sirina = 460.dp,
            sadrzaj = {
                Text(
                    "Neće se nuditi na šanku. Stare noći ga i dalje prikazuju. Možeš ga ponovo uključiti ovdje.",
                    color = Boje.muted,
                    style = Tekst.body.copy(fontSize = 16.sp, lineHeight = 24.sp),
                )
            },
            dugmad = {
                SporednoDugme("Odustani", vm::odustaniOdIskljucivanja)
                OpasnoDugme("Isključi", vm::potvrdiIskljucivanje)
            },
        )
    }
}

@Composable
private fun ListaArtikala(
    artikli: List<ArtikalPrikaz>,
    onPrebaci: (Long, Boolean) -> Unit,
    onIzmjena: (Long) -> Unit,
    onPomjeri: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Redoslijed se mijenja samo lokalno dok traje prevlačenje; upis ide na spuštanje.
    val lokalna = remember(artikli) { artikli.toMutableStateList() }
    var odIndeks by remember { mutableIntStateOf(-1) }
    val stanjeListe = rememberLazyListState()
    val reorder = rememberReorderableLazyListState(stanjeListe) { od, na -> lokalna.add(na.index, lokalna.removeAt(od.index)) }

    LazyColumn(modifier, state = stanjeListe) {
        items(lokalna, key = { it.a.id }) { p ->
            ReorderableItem(reorder, key = p.a.id) { prevlaci ->
                RedArtikla(
                    p = p,
                    prevlaci = prevlaci,
                    rucica = Modifier.draggableHandle(
                        onDragStarted = { odIndeks = lokalna.indexOfFirst { it.a.id == p.a.id } },
                        onDragStopped = { onPomjeri(odIndeks, lokalna.indexOfFirst { it.a.id == p.a.id }) },
                    ),
                    onPrebaci = { onPrebaci(p.a.id, it) },
                    onIzmjena = { onIzmjena(p.a.id) },
                )
            }
        }
    }
}

/** Red 60: rucica 32, naziv 200, kategorija 120, čipovi mjera, prekidač, „›“; razmak 14 (Paper V1). */
@Composable
private fun RedArtikla(
    p: ArtikalPrikaz,
    prevlaci: Boolean,
    rucica: Modifier,
    onPrebaci: (Boolean) -> Unit,
    onIzmjena: () -> Unit,
) {
    val aktivan = p.a.aktivan
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(if (prevlaci) Boje.surface else Boje.bg)
            .drawBehind { drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx()) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            rucica.size(32.dp, Razmaci.dodir).semantics { contentDescription = "Redoslijed: ${p.a.naziv}" },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.DragIndicator, null, Modifier.size(22.dp), tint = Boje.disabledInk) }
        Text(
            p.a.naziv,
            Modifier.width(200.dp),
            color = if (aktivan) Boje.ink else Boje.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        )
        Text(
            p.kategorija.naziv,
            Modifier.width(120.dp),
            color = Boje.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp),
        )
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            p.mjere.forEach { m -> CipMjere(Format.oznakaMjere(m.naziv, m.litara, saJedinicom = false), aktivan) }
        }
        Prekidac(aktivan, onPrebaci, opis = p.a.naziv)
        Box(
            Modifier
                .size(40.dp, Razmaci.dodir)
                .semantics { contentDescription = "Izmijeni ${p.a.naziv}" }
                .clickable(role = Role.Button, onClick = onIzmjena),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.ChevronRight, null, Modifier.size(24.dp), tint = Boje.muted) }
    }
}

@Composable
private fun CipMjere(tekst: String, aktivan: Boolean) {
    Box(
        Modifier
            .height(30.dp)
            .background(Boje.tile, Oblici.sm)
            .border(BorderStroke(1.dp, Boje.line), Oblici.sm)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            tekst,
            color = if (aktivan) Boje.ink else Boje.muted,
            maxLines = 1,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum"),
        )
    }
}
