package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.ui.components.ToastHost
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.components.VrstaZnacke
import dev.nextpixel.cafebar919.ui.components.Znacka
import dev.nextpixel.cafebar919.ui.theme.Boje
import kotlinx.coroutines.delay
import java.time.Clock
import java.time.LocalDate

/**
 * S1 „Unos ture“: traka, tri kolone (konobari, pića, tura) i traka „Posljednje“. Dok se stanje učitava
 * (`null`) vidi se samo zlatni indikator. „Sada“ i odbrojavanje se osvježavaju svake sekunde iz `clock`.
 */
@Composable
fun UnosEkran(vm: UnosViewModel, clock: Clock, onPodesavanja: () -> Unit) {
    val s by vm.stanje.collectAsState()
    val toast = remember { ToastStanje() }
    var sadaMs by remember { mutableLongStateOf(clock.millis()) }

    // Ekran šanka ostaje upaljen dok je prikazan.
    val pogled = LocalView.current
    DisposableEffect(pogled) {
        pogled.keepScreenOn = true
        onDispose { pogled.keepScreenOn = false }
    }
    LaunchedEffect(clock) {
        while (true) {
            delay(1000)
            sadaMs = clock.millis()
        }
    }
    // Nova tura u traci: odbrojavanje ne smije kasniti iza sata.
    val posljednjaTura = s?.posljednje?.firstOrNull()?.tura?.id
    LaunchedEffect(posljednjaTura) { sadaMs = clock.millis() }
    LaunchedEffect(vm) {
        vm.dogadjaji.collect { d ->
            when (d) {
                is UnosDogadjaj.Upisana -> toast.prikazi(
                    "Upisano: ${d.ime} · ${Format.brojac(d.brojStavki, "stavka", "stavke", "stavki")}",
                    akcija = if (d.rokS > 0) "Poništi" else null,
                ) { vm.ponisti(d.turaId) }
            }
        }
    }

    val st = s
    if (st == null) {
        Box(Modifier.fillMaxSize().background(Boje.bg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Boje.primary)
        }
        return
    }
    Box(Modifier.fillMaxSize().background(Boje.bg)) {
        Column(Modifier.fillMaxSize()) {
            GornjaTraka(
                datum = Format.datumNoci(LocalDate.ofEpochDay(st.noc.datum)),
                znacka = {
                    Znacka("Noć otvorena od ${Format.vrijeme(st.noc.otvorenaU, clock.zone)}", VrstaZnacke.NOC, Icons.Outlined.NightsStay)
                },
            ) {
                TrakaDugme("Podešavanja", Icons.Outlined.Lock, onPodesavanja)
            }
            Row(Modifier.weight(1f)) {
                KoKolona(st, vm::izaberiPrimaoca)
                PicaKolona(st, vm, onPodesavanja, Modifier.weight(1f).fillMaxSize())
                TuraKolona(st, vm)
            }
            PosljednjeTraka(st.posljednje, st.rokS, sadaMs, clock.zone, vm::ponisti)
        }
        ToastHost(toast, Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp + 16.dp))
    }
}
