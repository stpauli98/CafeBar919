package dev.nextpixel.cafebar919.ui.sank

import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.AppContainer
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.Prostor
import dev.nextpixel.cafebar919.data.StanjeBaze
import dev.nextpixel.cafebar919.ui.podesavanja.AppNav
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class SankNavTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDate.of(2026, 11, 21).atTime(21, 0).atZone(zona).toInstant(), zona)

    private fun postoji(tekst: String, dio: Boolean = false) = compose.onAllNodesWithText(tekst, substring = dio).fetchSemanticsNodes().isNotEmpty()
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(10_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }
    private fun cekaj(tekst: String, dio: Boolean = false) = try {
        cekajUslov { postoji(tekst, dio) }
    } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
        throw AssertionError("Nema teksta „$tekst“", e)
    }
    private fun dodirni(tekst: String) = compose.onNode(hasText(tekst) and hasClickAction()).performClick()

    private fun pokreni(prostor: Prostor = Prostor { 10L * 1024 * 1024 * 1024 }): AppContainer {
        val kontejner = AppContainer(ApplicationProvider.getApplicationContext(), sat, prostor)
        cekajUslov { kontejner.otvaranjeBaze.stanje.value is StanjeBaze.Spremna }
        return kontejner
    }

    private fun prikazi(kontejner: AppContainer) {
        compose.activity.setContent { CafeBarTema { AppNav(kontejner) } }
    }

    @Test fun bezNoci_S0_otvaranje_S1() {
        prikazi(pokreni())
        cekaj("Ko radi večeras?")
        dodirni("Vuleta")
        cekaj("Otvori noć · 1 radnik")
        dodirni("Otvori noć · 1 radnik")
        cekaj("Noć otvorena od", dio = true)
        compose.onNodeWithText("Ko radi večeras?").assertDoesNotExist()
    }

    @Test fun podesavanjaIzS1_iNazadNaUnos_nacrtOstaje() {
        val kontejner = pokreni()
        runBlocking {
            val p = kontejner.repo()
            p.sacuvajKategoriju(null, "Žestina")
            val kat = p.kategorije.first().first { it.k.naziv == "Žestina" }.k.id
            val doza = p.mjere.first().first { it.m.naziv == "Doza" }.m.id
            p.sacuvajArtikal(null, "Jack Daniel’s", kat, listOf(doza))
            val db = (kontejner.otvaranjeBaze.stanje.value as StanjeBaze.Spremna).db
            val ids = db.radnikDao().jednom().filter { it.ime == "Vuleta" }.map { it.id }
            kontejner.noc().otvoriNoc(ids, emptyList())
        }
        prikazi(kontejner)
        cekaj("Vuleta")
        dodirni("Vuleta")
        cekaj("TURA ZA")
        dodirni("Jack Daniel’s")
        cekaj("Potvrdi · Vuleta")

        dodirni("Podešavanja")
        cekaj("Odustani")
        "123456".forEach { compose.onNode(hasTextExactly(it.toString()) and hasClickAction()).performClick() }
        cekaj("Nazad na unos")
        dodirni("Nazad na unos")

        cekaj("TURA ZA")
        compose.onNodeWithText("Potvrdi · Vuleta").assertExists()
        // Jack je i u koloni pića (sa brojačem 1) i u koloni ture: nacrt je vraćen.
        assertEquals(2, compose.onAllNodesWithText("Jack Daniel’s", substring = true).fetchSemanticsNodes().size)
        compose.onNode(hasText("Jack Daniel’s") and hasText("1") and hasClickAction()).assertExists()
    }

    @Test fun prostor01_banerIRazumijem() {
        prikazi(pokreni(Prostor { 100L * 1024 * 1024 }))
        cekaj("Na tabletu je ostalo malo prostora.", dio = true)
        cekaj("Ko radi večeras?")
        dodirni("Razumijem")
        cekajUslov { !postoji("Na tabletu je ostalo malo prostora.", dio = true) }
    }

    @Test fun dovoljnoProstora_nemaBanera() {
        val pozivi = java.util.concurrent.atomic.AtomicInteger()
        val kontejner = pokreni(Prostor { pozivi.incrementAndGet(); 500L * 1024 * 1024 })
        prikazi(kontejner)
        cekaj("Ko radi večeras?")
        // Provjera pri spremnoj bazi je morala da se izvrši prije tvrdnje o odsustvu banera.
        cekajUslov { pozivi.get() >= 1 }
        kontejner.prostor.provjeri()
        cekajUslov { true }
        assertEquals(false, kontejner.prostor.nizak.value)
        compose.onNodeWithText("Razumijem").assertDoesNotExist()
        assertEquals(false, postoji("Na tabletu je ostalo malo prostora.", dio = true))
    }

    @Test fun prostor01_provjeraPoslijeOtvaranjaNoci() {
        val slobodno = java.util.concurrent.atomic.AtomicLong(500L * 1024 * 1024)
        val kontejner = pokreni(Prostor { slobodno.get() })
        prikazi(kontejner)
        cekaj("Ko radi večeras?")
        kontejner.prostor.provjeri()
        cekajUslov { true }
        compose.onNodeWithText("Razumijem").assertDoesNotExist()

        slobodno.set(100L * 1024 * 1024)
        dodirni("Vuleta")
        cekaj("Otvori noć · 1 radnik")
        dodirni("Otvori noć · 1 radnik")
        cekaj("Noć otvorena od", dio = true)
        cekaj("Na tabletu je ostalo malo prostora.", dio = true)
        compose.onNodeWithText("Razumijem").assertExists()
    }
}
