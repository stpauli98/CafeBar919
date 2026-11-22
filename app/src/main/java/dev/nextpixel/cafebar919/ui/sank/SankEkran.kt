package dev.nextpixel.cafebar919.ui.sank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.ui.components.Baner
import dev.nextpixel.cafebar919.ui.components.VrstaBanera
import dev.nextpixel.cafebar919.ui.theme.Boje
import kotlinx.coroutines.flow.flow

private sealed interface StanjeNoci {
    data object Ucitava : StanjeNoci
    data class Ima(val noc: NocE?) : StanjeNoci
}

/**
 * Početni ekran šanka: S0 „Nova noć“ dok nema otvorene noći, S1 „Unos ture“ dok je jedna otvorena.
 * Baner PROSTOR-01 stoji na samom vrhu, iznad ekrana. Dok se noć prvi put čita, crta se samo pozadina.
 */
@Composable
fun SankEkran(container: AppContainer, onPodesavanja: () -> Unit) {
    val noc by remember(container) {
        flow<StanjeNoci> {
            emit(StanjeNoci.Ucitava)
            container.noc().aktivna.collect { emit(StanjeNoci.Ima(it)) }
        }
    }.collectAsState(StanjeNoci.Ucitava)
    val nizak by container.prostor.nizak.collectAsState()

    Column(Modifier.fillMaxSize().background(Boje.bg)) {
        if (nizak) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Baner(
                    VrstaBanera.UPOZORENJE,
                    "Na tabletu je ostalo malo prostora.",
                    "Memorija je skoro puna (ispod 200 MB). Sve je sačuvano. Novi unos još radi.",
                    "Razumijem",
                    container.prostor::razumijem,
                )
            }
        }
        Box(Modifier.weight(1f).fillMaxSize()) {
            when (val n = noc) {
                StanjeNoci.Ucitava -> Box(Modifier.fillMaxSize().background(Boje.bg))
                is StanjeNoci.Ima ->
                    if (n.noc == null) NovaNocEkran(viewModel(factory = NovaNocViewModel.Fabrika(container)), onPodesavanja)
                    else UnosEkran(viewModel(factory = UnosViewModel.Fabrika(container)), container.clock, onPodesavanja)
            }
        }
    }
}
