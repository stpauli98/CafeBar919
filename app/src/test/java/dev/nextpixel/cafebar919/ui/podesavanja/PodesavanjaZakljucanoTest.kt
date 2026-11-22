package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.CafeBarApp
import dev.nextpixel.cafebar919.data.StanjeBaze
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class PodesavanjaZakljucanoTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst).fetchSemanticsNodes().isNotEmpty()

    // Poslije onStop / smrti procesa ruta podešavanja se vraća zaključana: ni jedan kadar (ni snimak u
    // Recents) ne smije pokazati sadržaj podešavanja prije nego što efekat vrati na početni ekran.
    @Test fun zakljucanaSesijaNaRutiPodesavanja_nePokazujeSadrzaj() {
        val kontejner = ApplicationProvider.getApplicationContext<CafeBarApp>().container
        compose.waitUntil(10_000) { kontejner.otvaranjeBaze.stanje.value is StanjeBaze.Spremna }
        assertFalse(kontejner.sesija.otkljucano.value)
        lateinit var nav: NavHostController
        compose.mainClock.autoAdvance = false
        compose.activity.setContent {
            nav = rememberNavController()
            CafeBarTema { AppNav(kontejner, nav) }
        }
        compose.mainClock.advanceTimeBy(500)
        compose.runOnUiThread { nav.navigate("podesavanja/ARTIKLI") }
        repeat(3) {
            compose.mainClock.advanceTimeByFrame()
            compose.onNodeWithText("Nazad na unos").assertDoesNotExist()
            compose.onNodeWithText("Otključano za vlasnika.").assertDoesNotExist()
        }
        compose.mainClock.autoAdvance = true
        compose.waitUntil(5_000) { postoji("Ko radi večeras?") }
    }
}
