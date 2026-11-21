package dev.nextpixel.cafebar919.ui.podesavanja.kategorije

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragIndicator
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.repo.KategorijaPrikaz
import dev.nextpixel.cafebar919.ui.components.Prekidac
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.DugmeIzmjene
import dev.nextpixel.cafebar919.ui.podesavanja.ZaglavljeSekcije
import dev.nextpixel.cafebar919.ui.theme.Boje
import dev.nextpixel.cafebar919.ui.theme.Razmaci
import dev.nextpixel.cafebar919.ui.theme.Tekst
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/** „N pića“, a za 0 „nema pića“ (spec 7 #14). */
internal fun brojPica(n: Int): String = if (n == 0) "nema pića" else Format.brojac(n, "piće", "pića", "pića")

/** V5 · Kategorije (Paper): zaglavlje, redovi 64 sa ručicom, nazivom, brojem pića, prekidačem i „›“, pomoćni tekst. */
@Composable
fun KategorijeEkran(vm: KategorijeViewModel, toast: ToastStanje) {
    LaunchedEffect(vm) { vm.poruke.collect { toast.prikazi(it) } }
    val lista by vm.lista.collectAsState()
    val prozor by vm.prozor.collectAsState()

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ZaglavljeSekcije(
            "Kategorije",
            "Kategorije su dugmad iznad pića na šanku. Redoslijed ovdje je redoslijed na šanku.",
            "Nova kategorija",
            vm::otvoriNovu,
        )
        lista?.let { ListaKategorija(it, vm::prebaci, vm::otvoriIzmjenu, vm::pomjeri, Modifier.weight(1f, fill = false)) }
        Text(
            "Isključena kategorija se ne vidi na šanku, a njena pića ostaju u istoriji. Kategorija sa aktivnim pićima ne može se isključiti dok se pića ne premjeste ili isključe.",
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 14.sp, lineHeight = 20.sp),
        )
    }
    prozor?.let { KategorijaProzor(it, vm) }
}

@Composable
private fun ListaKategorija(
    kategorije: List<KategorijaPrikaz>,
    onPrebaci: (Long, Boolean) -> Unit,
    onIzmjena: (Long) -> Unit,
    onPomjeri: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Redoslijed se mijenja samo lokalno dok traje prevlačenje; upis ide na spuštanje.
    val lokalna = remember(kategorije) { kategorije.toMutableStateList() }
    var odIndeks by remember { mutableIntStateOf(-1) }
    val stanjeListe = rememberLazyListState()
    val reorder = rememberReorderableLazyListState(stanjeListe) { od, na -> lokalna.add(na.index, lokalna.removeAt(od.index)) }

    LazyColumn(modifier, state = stanjeListe) {
        items(lokalna, key = { it.k.id }) { p ->
            ReorderableItem(reorder, key = p.k.id) { prevlaci ->
                RedKategorije(
                    p = p,
                    prevlaci = prevlaci,
                    rucica = Modifier.draggableHandle(
                        onDragStarted = { odIndeks = lokalna.indexOfFirst { it.k.id == p.k.id } },
                        onDragStopped = { onPomjeri(odIndeks, lokalna.indexOfFirst { it.k.id == p.k.id }) },
                    ),
                    onPrebaci = { onPrebaci(p.k.id, it) },
                    onIzmjena = { onIzmjena(p.k.id) },
                )
            }
        }
    }
}

/** Red 64: ručica 32, naziv (fleksibilno), broj pića 160, prekidač, „›“; razmak 16 (Paper V5). */
@Composable
private fun RedKategorije(p: KategorijaPrikaz, prevlaci: Boolean, rucica: Modifier, onPrebaci: (Boolean) -> Unit, onIzmjena: () -> Unit) {
    val aktivna = p.k.aktivna
    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(if (prevlaci) Boje.surface else Boje.bg)
            .drawBehind { drawLine(Boje.line, Offset(0f, size.height - 0.5.dp.toPx()), Offset(size.width, size.height - 0.5.dp.toPx()), 1.dp.toPx()) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            rucica.size(32.dp, Razmaci.dodir).semantics { contentDescription = "Redoslijed: ${p.k.naziv}" },
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.DragIndicator, null, Modifier.size(22.dp), tint = Boje.disabledInk) }
        Text(
            p.k.naziv,
            Modifier.weight(1f),
            color = if (aktivna) Boje.ink else Boje.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = Tekst.body.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        )
        Text(
            brojPica(p.brojAktivnihPica),
            Modifier.width(160.dp),
            color = Boje.muted,
            style = Tekst.body.copy(fontSize = 15.sp, lineHeight = 18.sp),
        )
        // Kategorija sa aktivnim pićem ostaje uključena dok se pića ne premjeste ili isključe.
        Prekidac(aktivna, onPrebaci, omogucen = !(aktivna && p.brojAktivnihPica > 0), opis = p.k.naziv)
        DugmeIzmjene(p.k.naziv, onIzmjena)
    }
}
