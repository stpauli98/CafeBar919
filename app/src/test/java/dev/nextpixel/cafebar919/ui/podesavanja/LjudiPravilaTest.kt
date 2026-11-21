package dev.nextpixel.cafebar919.ui.podesavanja

import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.ui.components.ToastHost
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.ljudi.LjudiEkran
import dev.nextpixel.cafebar919.ui.podesavanja.ljudi.LjudiViewModel
import dev.nextpixel.cafebar919.ui.podesavanja.pravila.PravilaEkran
import dev.nextpixel.cafebar919.ui.podesavanja.pravila.PravilaViewModel
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

// Tablet 1280×800 kao u Paperu.
@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class LjudiPravilaTest {
    @get:Rule val compose = createComposeRule()
    private val zona = ZoneId.systemDefault()
    private val danas = LocalDate.of(2026, 11, 21)
    private val sat = Clock.fixed(danas.atStartOfDay(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var repo: PodesavanjaRepo
    lateinit var lvm: LjudiViewModel
    lateinit var pvm: PravilaViewModel

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        repo = PodesavanjaRepo(db, sat)
    }

    @After fun tearDown() = db.close()

    private fun <T> repoPoziv(blok: suspend () -> T): T = runBlocking { blok() }

    private fun prikaziLjude() {
        lvm = LjudiViewModel(repo)
        val toast = ToastStanje()
        compose.setContent {
            CafeBarTema { Box(Modifier.fillMaxSize()) { LjudiEkran(lvm, toast); ToastHost(toast, Modifier.align(Alignment.BottomCenter)) } }
        }
    }

    private fun prikaziPravila() {
        pvm = PravilaViewModel(repo)
        val toast = ToastStanje()
        compose.setContent {
            CafeBarTema { Box(Modifier.fillMaxSize()) { PravilaEkran(pvm, toast); ToastHost(toast, Modifier.align(Alignment.BottomCenter)) } }
        }
    }

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst).fetchSemanticsNodes().isNotEmpty()
    private fun cekaj(tekst: String) = compose.waitUntil(5_000) { postoji(tekst) }
    private fun cekajNestanak(tekst: String) = compose.waitUntil(5_000) { !postoji(tekst) }
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(5_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }
    private fun polje(i: Int): SemanticsNodeInteraction = compose.onAllNodes(hasSetTextAction())[i]
    private fun minus(i: Int) = compose.onAllNodesWithContentDescription("Minus")[i]
    private fun plus(i: Int) = compose.onAllNodesWithContentDescription("Plus")[i]

    // ---- V2 Ljudi ----

    @Test fun ljudi_pocetnaListaIUloge() {
        prikaziLjude()
        cekaj("Vuleta")
        compose.onNodeWithText("Stalni radnici").assertExists()
        compose.onNodeWithText("Srđan").assertExists()
        compose.onNodeWithText("Miloš").assertExists()
        compose.onNodeWithText("Smitran").assertExists()
        assertEquals(2, compose.onAllNodesWithText("nosi piće gostima").fetchSemanticsNodes().size)
        assertEquals(2, compose.onAllNodesWithText("ne izlazi iz šanka, prodaje piće").fetchSemanticsNodes().size)
        compose.onNodeWithText("Ispomoć se ne dodaje ovdje").assertExists()
    }

    @Test fun ljudi_promjenaUlogeSeCuva() {
        prikaziLjude()
        cekaj("Smitran")
        // Redoslijed: Vuleta, Srđan, Miloš, Smitran; četvrti red je Smitran.
        compose.onAllNodesWithText("Konobar")[3].performClick()
        cekajUslov { repoPoziv { repo.radnici.first().single { it.ime == "Smitran" }.uloga } == Uloga.KONOBAR }
        cekaj("nosi piće gostima") // opis se izvodi iz uloge
        cekajUslov { compose.onAllNodesWithText("nosi piće gostima").fetchSemanticsNodes().size == 3 }
    }

    @Test fun ljudi_isKljucivanjeTraziPotvrdu() {
        prikaziLjude()
        cekaj("Vuleta")
        compose.onNodeWithContentDescription("Vuleta").performClick()
        cekaj("Isključiti Vuleta?")
        compose.onNodeWithText("Neće se nuditi pri otvaranju noći. Ranije noći ga i dalje prikazuju.").assertExists()
        // Odustani: ništa se ne mijenja.
        compose.onNodeWithText("Odustani").performClick()
        cekajNestanak("Isključiti Vuleta?")
        assertTrue(repoPoziv { repo.radnici.first().single { it.ime == "Vuleta" }.aktivan })

        compose.onNodeWithContentDescription("Vuleta").performClick()
        cekaj("Isključiti Vuleta?")
        compose.onNodeWithText("Isključi").performClick()
        cekajNestanak("Isključiti Vuleta?")
        cekajUslov { !repoPoziv { repo.radnici.first().single { it.ime == "Vuleta" }.aktivan } }
        // Ponovno uključivanje ide bez pitanja.
        cekajUslov { lvm.lista.value?.first { it.ime == "Vuleta" }?.aktivan == false }
        compose.onNodeWithContentDescription("Vuleta").performClick()
        cekajUslov { repoPoziv { repo.radnici.first().single { it.ime == "Vuleta" }.aktivan } }
    }

    @Test fun ljudi_ponovnoUkljucivanjeSaZauzetimImenom() {
        prikaziLjude()
        cekaj("Vuleta")
        compose.onNodeWithContentDescription("Vuleta").performClick()
        cekaj("Isključiti Vuleta?")
        compose.onNodeWithText("Isključi").performClick()
        cekajUslov { !repoPoziv { repo.radnici.first().single { it.ime == "Vuleta" }.aktivan } }
        // Novi aktivni „Vuleta“ je dozvoljen dok je stari isključen.
        repoPoziv { assertEquals(Ishod.Ok, repo.sacuvajRadnika(null, "Vuleta", Uloga.KONOBAR)) }
        cekajUslov { lvm.lista.value?.count { it.ime == "Vuleta" } == 2 }
        cekajUslov { compose.onAllNodesWithContentDescription("Vuleta").fetchSemanticsNodes().size == 2 }
        // Stari je prvi u listi: njegovo uključivanje je odbijeno i javlja se toast.
        compose.onAllNodesWithContentDescription("Vuleta")[0].performClick()
        cekaj("Radnik sa tim imenom već postoji.")
        assertEquals(1, repoPoziv { repo.radnici.first().count { it.ime == "Vuleta" && !it.aktivan } })
    }

    @Test fun ljudi_noviIDuplikat() {
        prikaziLjude()
        cekaj("Novi radnik")
        compose.onNodeWithText("Novi radnik").performClick()
        cekaj("Sačuvaj radnika")
        polje(0).performTextInput("Dejan")
        compose.onNodeWithText("Sačuvaj radnika").performClick()
        cekaj("Sačuvano: Dejan")
        cekajNestanak("Sačuvaj radnika")
        assertEquals(Uloga.KONOBAR, repoPoziv { repo.radnici.first().single { it.ime == "Dejan" }.uloga })

        compose.onNodeWithText("Novi radnik").performClick()
        cekaj("Sačuvaj radnika")
        polje(0).performTextInput("vuleta")
        compose.onNodeWithText("Sačuvaj radnika").performClick()
        cekaj("Radnik sa tim imenom već postoji.")
        // Prozor ostaje otvoren, ništa nije sačuvano.
        compose.onNodeWithText("Sačuvaj radnika").assertIsEnabled()
        assertEquals(5, repoPoziv { repo.radnici.first().size })
    }

    @Test fun ljudi_izmjenaImenaOtvaraSeDodirom() {
        prikaziLjude()
        cekaj("Miloš")
        compose.onNodeWithContentDescription("Izmijeni Miloš").performClick()
        cekaj("Izmjena · Miloš")
        polje(0).performTextReplacement("Miloša")
        compose.onNodeWithText("Sačuvaj radnika").performClick()
        cekaj("Sačuvano: Miloša")
        assertEquals(Uloga.ZA_SANKOM, repoPoziv { repo.radnici.first().single { it.ime == "Miloša" }.uloga })
    }

    // ---- V6 Pravila rada ----

    @Test fun pravila_pocetneVrijednostiIStepperPromjena() {
        prikaziPravila()
        cekaj("Pravila rada")
        cekaj("60 s")
        cekaj("30 dana")
        cekaj("2 min")
        plus(0).performClick()
        cekaj("70 s")
        assertEquals(70, repoPoziv { repo.postavke.first().rokBezPinaS })
        plus(2).performClick()
        cekaj("3 min")
        assertEquals(180, repoPoziv { repo.postavke.first().zakljucavanjeS })
        minus(1).performClick() // istorija: nema noći pa ide bez pitanja
        cekaj("29 dana")
        assertEquals(29, repoPoziv { repo.postavke.first().istorijaDana })
    }

    @Test fun pravila_dvaBrzaDodiraDajuDvaKoraka() {
        prikaziPravila()
        cekaj("60 s")
        // Dva dodira prije nego što tok ponovo javi vrijednost: svaki se računa od najnovije.
        pvm.rokBezPina(1)
        pvm.rokBezPina(1)
        cekaj("80 s")
        cekajUslov { repoPoziv { repo.postavke.first().rokBezPinaS } == 80 }
    }

    @Test fun pravila_stepperGranice() {
        repoPoziv {
            assertEquals(Ishod.Ok, repo.postaviRokBezPina(300))
            assertEquals(Ishod.Ok, repo.postaviZakljucavanje(60))
        }
        prikaziPravila()
        cekaj("300 s")
        cekaj("1 min")
        plus(0).assertIsNotEnabled()
        minus(0).assertIsEnabled()
        minus(2).assertIsNotEnabled()
        plus(2).assertIsEnabled()
    }

    @Test fun pravila_razlogDodajIUkloni() {
        prikaziPravila()
        cekaj("Pogrešan konobar")
        compose.onNodeWithText("+ Razlog").performClick()
        cekaj("Novi razlog")
        polje(0).performTextInput("Vraćeno")
        compose.onNodeWithText("Dodaj razlog").performClick()
        cekajNestanak("Novi razlog")
        cekaj("Vraćeno")

        // Duplikat se javlja u prozoru.
        compose.onNodeWithText("+ Razlog").performClick()
        cekaj("Novi razlog")
        polje(0).performTextInput("vraćeno")
        compose.onNodeWithText("Dodaj razlog").performClick()
        cekaj("Taj razlog već postoji.")
        compose.onNodeWithText("Odustani").performClick()
        cekajNestanak("Novi razlog")

        compose.onNodeWithContentDescription("Ukloni Vraćeno").performClick()
        cekajNestanak("Vraćeno")
        assertFalse(repoPoziv { repo.razlozi.first().any { it.tekst == "Vraćeno" } })
    }

    @Test fun pravila_ispomocPrekidac() {
        prikaziPravila()
        cekaj("Ispomoć")
        compose.onNodeWithContentDescription("Ispomoć").performClick()
        cekajUslov { !repoPoziv { repo.postavke.first().ispomocDozvoljena } }
    }

    @Test fun pravila_smanjenjeIstorijeTraziPotvrduKadBriseNoci() {
        repoPoziv {
            db.nocDao().dodajNoc(NocE(datum = danas.minusDays(20).toEpochDay(), otvorenaU = 1, zatvorenaU = 2))
            assertEquals(Ishod.Ok, repo.postaviIstoriju(20))
        }
        prikaziPravila()
        cekaj("20 dana")
        minus(1).performClick()
        cekaj("Obrisati stare noći?")
        compose.onNodeWithText("1 noć starijih od 19 dana biće obrisano pri sljedećem pokretanju.").assertExists()
        compose.onNodeWithText("Nastavi unos").performClick()
        cekajNestanak("Obrisati stare noći?")
        assertTrue(postoji("20 dana"))
        assertEquals(20, repoPoziv { repo.postavke.first().istorijaDana })

        minus(1).performClick()
        cekaj("Obrisati stare noći?")
        compose.onNodeWithText("Smanji istoriju").performClick()
        cekaj("19 dana")
        assertEquals(19, repoPoziv { repo.postavke.first().istorijaDana })
    }
}
