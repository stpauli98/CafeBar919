package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

    NavHost(navController, startDestination = RUTA_POCETNI) {
        composable(RUTA_POCETNI) {
            var pinOtvoren by rememberSaveable { mutableStateOf(false) }
            PocetniEkran(danas = danas, onPodesavanja = { pinOtvoren = true })
            if (pinOtvoren) {
                PinProzor(
                    pin = container.pin(),
                    onOtkljucano = {
                        pinOtvoren = false
                        sesija.otkljucaj()
                        navController.navigate(rutaSekcije(Sekcija.ARTIKLI))
                    },
                    onOdustani = { pinOtvoren = false },
                    clock = container.clock,
                )
            }
        }
        composable(RUTA_PODESAVANJA) { unos ->
            val sekcija = unos.arguments?.getString("sekcija")?.let { n -> Sekcija.entries.firstOrNull { it.name == n } } ?: Sekcija.ARTIKLI
            val brojaci by container.repo().brojaciMenija.collectAsState(BrojaciMenija(0, 0, 0, 0))
            val probni by container.pin().probni.collectAsState(false)
            BackHandler { sesija.zakljucaj() }
            Ljuska(
                aktivna = sekcija,
                onSekcija = { navController.navigate(rutaSekcije(it)) { popUpTo(RUTA_PODESAVANJA) { inclusive = true } } },
                onNazad = { navController.popBackStack(RUTA_POCETNI, inclusive = false) },
                sesija = sesija,
                brojaci = brojaci,
                probniPin = probni,
            ) { RadniDio(sekcija) }
        }
    }
}

/** Jedino mjesto gdje se sekcije mapiraju na ekrane; taskovi 8–11 dopunjuju grane. */
@Composable
private fun RadniDio(sekcija: Sekcija) {
    when (sekcija) {
        Sekcija.ARTIKLI, Sekcija.KATEGORIJE, Sekcija.MJERE, Sekcija.LJUDI,
        Sekcija.PRAVILA, Sekcija.PIN, Sekcija.O_APLIKACIJI -> Unit
    }
}
