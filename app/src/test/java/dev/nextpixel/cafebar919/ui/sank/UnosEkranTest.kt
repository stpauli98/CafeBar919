package dev.nextpixel.cafebar919.ui.sank

import android.os.Looper
import android.view.View
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.repo.TuraRepo
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

// Tablet 1280×800 kao u Paperu.
@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class UnosEkranTest {
    @get:Rule val compose = createComposeRule()
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDate.of(2026, 11, 21).atTime(23, 10).atZone(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var podesavanja: PodesavanjaRepo
    lateinit var nocRepo: NocRepo
    lateinit var ture: TuraRepo
    private var vm: UnosViewModel? = null

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        podesavanja = PodesavanjaRepo(db, sat)
        nocRepo = NocRepo(db, sat)
        ture = TuraRepo(db, sat)
    }

    @After fun tearDown() {
        vm?.viewModelScope?.cancel()
        db.close()
    }

    /** Dva pića (Jelen pivo komad, Jack Daniel’s doza) i noć sa Vuletom i Srđanom. */
    private fun podaci(sPicima: Boolean = true, radnici: List<String> = listOf("Vuleta", "Srđan"), ispomoc: List<String> = emptyList()) = runBlocking {
        if (sPicima) {
            podesavanja.sacuvajKategoriju(null, "Pivo")
            podesavanja.sacuvajKategoriju(null, "Žestina")
            val kat = podesavanja.kategorije.first()
            val mj = podesavanja.mjere.first()
            val komad = mj.first { it.m.naziv == "Komad" }.m.id
            val doza = mj.first { it.m.naziv == "Doza" }.m.id
            podesavanja.sacuvajArtikal(null, "Jelen pivo", kat.first { it.k.naziv == "Pivo" }.k.id, listOf(komad))
            podesavanja.sacuvajArtikal(null, "Jack Daniel’s", kat.first { it.k.naziv == "Žestina" }.k.id, listOf(doza))
        }
        val ids = db.radnikDao().jednom().filter { it.ime in radnici }.map { it.id }
        nocRepo.otvoriNoc(ids, ispomoc)
    }

    private fun prikazi(): View {
        var pogled: View? = null
        val v = UnosViewModel(ture, nocRepo, sat).also { vm = it }
        compose.setContent {
            CafeBarTema {
                pogled = LocalView.current
                UnosEkran(v, sat, onPodesavanja = { podesavanja1++ })
            }
        }
        return pogled!!
    }

    private var podesavanja1 = 0

    private fun postoji(tekst: String, dio: Boolean = false) = compose.onAllNodesWithText(tekst, substring = dio).fetchSemanticsNodes().isNotEmpty()
    private fun cekaj(tekst: String, dio: Boolean = false) = try {
        cekajUslov { postoji(tekst, dio) }
    } catch (e: androidx.compose.ui.test.ComposeTimeoutException) {
        throw AssertionError("Nema teksta „$tekst“", e)
    }
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(10_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }

    private fun dodirni(tekst: String) = compose.onNode(hasText(tekst) and hasClickAction()).performClick()

    @Test fun tokTure_odIzboraDoTrake() {
        podaci()
        prikazi()
        cekaj("Vuleta")
        dodirni("Vuleta")
        cekaj("TURA ZA")
        dodirni("Jack Daniel’s")
        dodirni("Jack Daniel’s")
        cekaj("Potvrdi · Vuleta")
        compose.onNodeWithText("Potvrdi · Vuleta").performClick()
        cekaj("Upisano: Vuleta · 1 stavka")
        cekaj("Jack Daniel’s doza 2")
        cekaj("Poništi · 60 s")
        compose.onNodeWithText("Poništi").assertExists()
        cekaj("TURA")
        cekaj("Nova tura")
    }

    @Test fun pretraga_dodirPicaSkidaFokus() {
        podaci()
        prikazi()
        cekaj("Vuleta")
        dodirni("Vuleta")
        cekaj("TURA ZA")
        val polje = compose.onNode(hasSetTextAction())
        polje.performClick()
        polje.performTextInput("Jack")
        polje.assertIsFocused()
        dodirni("Jack Daniel’s")
        cekaj("Potvrdi · Vuleta")
        polje.assertIsNotFocused()
    }

    @Test fun potvrdiOnemoguceno_porukeIzSpec17() {
        podaci()
        prikazi()
        cekaj("Izaberi konobara i dodaj piće.")
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
        dodirni("Vuleta")
        cekaj("Dodaj piće u turu.")
        dodirni("Vuleta")
        cekaj("Izaberi konobara i dodaj piće.")
        dodirni("Jack Daniel’s")
        cekaj("Izaberi konobara.")
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
        dodirni("Vuleta")
        cekaj("Potvrdi · Vuleta")
        compose.onNodeWithText("Potvrdi · Vuleta").assertIsEnabled()
    }

    @Test fun t2_bezPicaPokazujePodesavanja() {
        podaci(sPicima = false)
        prikazi()
        cekaj("Još nema pića")
        compose.onNodeWithText("Prije prve ture vlasnik unosi pića i mjere u podešavanjima. Treba mu PIN.").assertExists()
        compose.onNodeWithText("Prvo treba dodati pića.").assertExists()
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
        assertTrue(compose.onAllNodesWithText("Traži piće").fetchSemanticsNodes().isEmpty())
        val prije = podesavanja1
        // Dva dugmeta „Podešavanja“: u traci i u sredini.
        compose.onAllNodesWithText("Podešavanja")[1].performClick()
        assertEquals(prije + 1, podesavanja1)
    }

    @Test fun t1_pretragaBezRezultataIOcisti() {
        podaci()
        prikazi()
        cekaj("Jelen pivo")
        compose.onNode(hasSetTextAction()).performTextInput("mojito")
        cekaj("Nema pića „mojito“")
        compose.onNodeWithText("Provjeri naziv ili traži po prvim slovima. Novo piće dodaje vlasnik u podešavanjima.").assertExists()
        compose.onNodeWithText("Očisti pretragu").performClick()
        cekaj("Jelen pivo")
        cekaj("Jack Daniel’s")
        assertTrue(!postoji("Nema pića „mojito“"))
    }

    private fun potvrdiTuru() {
        cekaj("Vuleta")
        dodirni("Vuleta")
        cekaj("TURA ZA")
        dodirni("Jack Daniel’s")
        dodirni("Jack Daniel’s")
        cekaj("Potvrdi · Vuleta")
        compose.onNodeWithText("Potvrdi · Vuleta").performClick()
        cekaj("Poništi · 60 s")
    }

    @Test fun ponistiUTraci_nestajePoslijeRoka() {
        podaci()
        prikazi()
        potvrdiTuru()
        sat.pomjeri(Duration.ofSeconds(30))
        compose.mainClock.advanceTimeBy(1100)
        cekaj("Poništi · 30 s")
        sat.pomjeri(Duration.ofSeconds(31))
        compose.mainClock.advanceTimeBy(1100)
        cekajUslov { !postoji("Poništi · 60 s") }
        assertTrue(compose.onAllNodesWithText("Poništi · ", substring = true).fetchSemanticsNodes().isEmpty())
        // Sama tura ostaje u traci.
        compose.onNodeWithText("Jack Daniel’s doza 2").assertExists()
    }

    @Test fun ponistiUTraci_precrtanoIZnacka() {
        podaci()
        prikazi()
        potvrdiTuru()
        compose.onNodeWithText("Poništi · 60 s").performClick()
        cekaj("Poništeno bez PIN-a")
        cekajUslov { compose.onAllNodesWithText("Poništi · ", substring = true).fetchSemanticsNodes().isEmpty() }
        val rezultati = mutableListOf<TextLayoutResult>()
        val node = compose.onNodeWithText("Jack Daniel’s doza 2").fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action!!.invoke(rezultati)
        assertEquals(TextDecoration.LineThrough, rezultati.single().layoutInput.style.textDecoration)
    }

    @Test fun upis01_banerIPokusajPonovo() {
        podaci()
        prikazi()
        cekaj("Vuleta")
        dodirni("Vuleta")
        dodirni("Jack Daniel’s")
        cekaj("Potvrdi · Vuleta")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER kvar BEFORE INSERT ON tura BEGIN SELECT RAISE(ABORT, 'kvar'); END")
        compose.onNodeWithText("Potvrdi · Vuleta").performClick()
        // Baner spaja naslov i opis u jedan čvor.
        cekaj("Tura nije upisana.", dio = true)
        compose.onNodeWithText("Tablet nije uspio da sačuva podatke. Tura je ostala ovdje, ništa nije izgubljeno.", substring = true).assertExists()
        compose.onNodeWithText("Šifra: UPIS-01").assertExists()
        compose.onNodeWithText("Pokušaj ponovo").assertIsEnabled()
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER kvar")
        compose.onNodeWithText("Pokušaj ponovo").performClick()
        cekaj("Upisano: Vuleta · 1 stavka")
        cekajUslov { !postoji("Tura nije upisana.", dio = true) }
    }

    @Test fun ekranSeNeGasi() {
        podaci()
        val pogled = prikazi()
        cekaj("Vuleta")
        assertTrue(pogled.keepScreenOn)
    }

    @Test fun mnogoLjudiIPica_sveDostupnoIPotvrdiVidljivo() {
        val imena = (1..7).map { "Konobar %02d".format(it) }
        runBlocking { imena.forEach { podesavanja.sacuvajRadnika(null, it, Uloga.KONOBAR) } }
        val ispomoc = listOf("Ispomoć 1", "Ispomoć 2", "Ispomoć 3")
        // 7 novih + Vuleta, Srđan, Miloš = 10 radnika.
        podaci(sPicima = false, radnici = imena + listOf("Vuleta", "Srđan", "Miloš"), ispomoc = ispomoc)
        runBlocking {
            podesavanja.sacuvajKategoriju(null, "Pivo")
            val k = podesavanja.kategorije.first().first { it.k.naziv == "Pivo" }.k.id
            val komad = podesavanja.mjere.first().first { it.m.naziv == "Komad" }.m.id
            (1..60).forEach { podesavanja.sacuvajArtikal(null, "Piće %02d".format(it), k, listOf(komad)) }
        }
        prikazi()
        cekaj("Vuleta")
        // Lijeva kolona je prva vertikalna lista (jedna kolona) u stablu.
        val lijeva = hasScrollToIndexAction() and SemanticsMatcher("jedna kolona") { it.config.getOrNull(SemanticsProperties.CollectionInfo)?.columnCount == 1 }
        compose.onAllNodes(lijeva)[0].performScrollToNode(hasText("Ispomoć 3"))
        compose.onNodeWithText("Ispomoć 3").assertIsDisplayed()
        dodirni("Ispomoć 3")
        cekaj("TURA ZA")
        // Okomite liste u stablu redom: lijeva kolona, mreža pića, stavke ture.
        val okomite = hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        fun mreza() = compose.onAllNodes(okomite)[1]
        for (i in 1..15) {
            val ime = "Piće %02d".format(i)
            mreza().performScrollToNode(hasText(ime) and hasClickAction())
            dodirni(ime)
        }
        mreza().performScrollToNode(hasText("Piće 60") and hasClickAction())
        compose.onNodeWithText("Piće 60").assertIsDisplayed()
        cekaj("Potvrdi · Ispomoć 3")
        compose.onNodeWithText("Potvrdi · Ispomoć 3").assertIsDisplayed()
        compose.onNodeWithText("Potvrdi · Ispomoć 3").assertIsEnabled()
    }
}
