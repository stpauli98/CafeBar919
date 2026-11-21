package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nextpixel.cafebar919.ui.components.ToastHost
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.artikli.ArtikliEkran
import dev.nextpixel.cafebar919.ui.podesavanja.artikli.ArtikliViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.repo.BrojaciMenija
import dev.nextpixel.cafebar919.ui.pocetni.PocetniEkran
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val RUTA_POCETNI = "pocetni"
private const val RUTA_PODESAVANJA = "podesavanja/{sekcija}"

private fun rutaSekcije(s: Sekcija) = "podesavanja/${s.name}"

/** Navigacija aplikacije: početni ekran i podešavanja (ljuska + sekcija). Poziva se kad je baza spremna. */
@Composable
fun AppNav(container: AppContainer, danas: LocalDate, navController: NavHostController = rememberNavController()) {
    val sesija = container.sesija
    val otkljucano by sesija.otkljucano.collectAsState()
    val ruta by navController.currentBackStackEntryAsState()
    val naPodesavanjima = ruta?.destination?.route == RUTA_PODESAVANJA

    // Zaključavanje (istek, „Zaključaj sada“, odlazak u pozadinu) uvijek vraća na početni ekran.
    LaunchedEffect(otkljucano, naPodesavanjima) {
        if (!otkljucano && naPodesavanjima) navController.popBackStack(RUTA_POCETNI, inclusive = false)
    }

    // Brojači i probni PIN žive iznad NavHosta da prežive promjenu rute (nema treptanja nula i banera).
    val brojaci by container.repo().brojaciMenija.collectAsState(BrojaciMenija(0, 0, 0, 0))
    val probni by container.pin().probni.collectAsState(false)

    // Bez prelaza: promjena sekcije ne smije utamniti cijelu ljusku.
    NavHost(
        navController,
        startDestination = RUTA_POCETNI,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        composable(RUTA_POCETNI) {
            var pinOtvoren by rememberSaveable { mutableStateOf(false) }
            val glavna = rememberCoroutineScope()
            PocetniEkran(danas = danas, onPodesavanja = { pinOtvoren = true })
            if (pinOtvoren) {
                PinProzor(
                    pin = container.pin(),
                    onOtkljucano = {
                        // Ishod provjere stiže iz korutine; navigacija traži glavnu nit.
                        glavna.launch(Dispatchers.Main.immediate) {
                            pinOtvoren = false
                            sesija.otkljucaj()
                            navController.navigate(rutaSekcije(Sekcija.ARTIKLI))
                        }
                    },
                    onOdustani = { pinOtvoren = false },
                    clock = container.clock,
                )
            }
        }
        composable(RUTA_PODESAVANJA) { unos ->
            val sekcija = unos.arguments?.getString("sekcija")?.let { n -> Sekcija.entries.firstOrNull { it.name == n } } ?: Sekcija.ARTIKLI
            BackHandler { sesija.zakljucaj() }
            // Jedan toast za cijelo područje podešavanja, iznad radnog dijela.
            val toast = remember { ToastStanje() }
            Box(Modifier.fillMaxSize()) {
                Ljuska(
                    aktivna = sekcija,
                    onSekcija = { navController.navigate(rutaSekcije(it)) { popUpTo(RUTA_PODESAVANJA) { inclusive = true } } },
                    onNazad = { navController.popBackStack(RUTA_POCETNI, inclusive = false) },
                    sesija = sesija,
                    brojaci = brojaci,
                    probniPin = probni,
                ) { RadniDio(sekcija, container, toast) }
                ToastHost(toast, Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
            }
        }
    }
}

/** Jedino mjesto gdje se sekcije mapiraju na ekrane; taskovi 8–11 dopunjuju grane. */
@Composable
private fun RadniDio(sekcija: Sekcija, container: AppContainer, toast: ToastStanje) {
    when (sekcija) {
        Sekcija.ARTIKLI -> ArtikliEkran(viewModel(factory = ArtikliViewModel.Fabrika(container)), toast)
        Sekcija.KATEGORIJE, Sekcija.MJERE, Sekcija.LJUDI,
        Sekcija.PRAVILA, Sekcija.PIN, Sekcija.O_APLIKACIJI -> Unit
    }
}
