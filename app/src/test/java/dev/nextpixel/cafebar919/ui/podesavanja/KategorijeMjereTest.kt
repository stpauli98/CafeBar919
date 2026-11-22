package dev.nextpixel.cafebar919.ui.podesavanja

import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.ui.components.ToastHost
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.kategorije.KategorijeEkran
import dev.nextpixel.cafebar919.ui.podesavanja.kategorije.KategorijeViewModel
import dev.nextpixel.cafebar919.ui.podesavanja.mjere.MjereEkran
import dev.nextpixel.cafebar919.ui.podesavanja.mjere.MjereViewModel
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
class KategorijeMjereTest {
    @get:Rule val compose = createComposeRule()
    private val zona = ZoneId.systemDefault()
    private val sat = Clock.fixed(LocalDate.of(2026, 11, 21).atStartOfDay(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var repo: PodesavanjaRepo
    lateinit var kvm: KategorijeViewModel
    lateinit var mvm: MjereViewModel

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        repo = PodesavanjaRepo(db, sat)
    }

    @After fun tearDown() = db.close()

    private fun <T> repoPoziv(blok: suspend () -> T): T = runBlocking { blok() }

    private fun kategorija(naziv: String): Long = repoPoziv {
        assertEquals(Ishod.Ok, repo.sacuvajKategoriju(null, naziv))
        repo.kategorije.first().first { it.k.naziv == naziv }.k.id
    }

    private fun mjera(naziv: String): Long = repoPoziv { repo.mjere.first().first { it.m.naziv == naziv }.m.id }

    private fun artikal(naziv: String, kategorijaId: Long, vararg mjere: String): Long = repoPoziv {
        assertEquals(Ishod.Ok, repo.sacuvajArtikal(null, naziv, kategorijaId, mjere.map { mjera(it) }))
        repo.artikli.first().first { it.a.naziv == naziv }.a.id
    }

    private fun naziviKategorija() = repoPoziv { repo.kategorije.first().map { it.k.naziv } }

    private fun prikaziKategorije() {
        kvm = KategorijeViewModel(repo)
        val toast = ToastStanje()
        compose.setContent {
            CafeBarTema { Box(Modifier.fillMaxSize()) { KategorijeEkran(kvm, toast); ToastHost(toast, Modifier.align(Alignment.BottomCenter)) } }
        }
    }

    private fun prikaziMjere() {
        mvm = MjereViewModel(repo)
        val toast = ToastStanje()
        compose.setContent {
            CafeBarTema { Box(Modifier.fillMaxSize()) { MjereEkran(mvm, toast); ToastHost(toast, Modifier.align(Alignment.BottomCenter)) } }
        }
    }

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst).fetchSemanticsNodes().isNotEmpty()
    private fun cekaj(tekst: String) = compose.waitUntil(5_000) { postoji(tekst) }
    private fun cekajNestanak(tekst: String) = compose.waitUntil(5_000) { !postoji(tekst) }
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(5_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }
    private fun brojTekstova(tekst: String): Int = compose.onAllNodes(isRoot()).fetchSemanticsNodes()
        .sumOf { r -> brojUStablu(r, tekst) }

    private fun brojUStablu(n: androidx.compose.ui.semantics.SemanticsNode, tekst: String): Int =
        (n.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.count { it.text == tekst } ?: 0) +
            n.children.sumOf { brojUStablu(it, tekst) }

    private fun polje(i: Int): SemanticsNodeInteraction = compose.onAllNodes(hasSetTextAction())[i]

    // ---- V5 Kategorije ----

    @Test fun kategorija_novaIDuplikat() {
        prikaziKategorije()
        cekaj("Nova kategorija")
        compose.onNodeWithText("Nova kategorija").performClick()
        cekaj("Sačuvaj kategoriju")
        polje(0).performTextInput("Pivo")
        compose.onNodeWithText("Sačuvaj kategoriju").performClick()
        cekaj("Sačuvano: Pivo")
        cekajNestanak("Sačuvaj kategoriju")
        assertTrue(postoji("nema pića"))
        compose.onNodeWithText("Pivo").assertExists()

        compose.onNodeWithText("Nova kategorija").performClick()
        cekaj("Sačuvaj kategoriju")
        polje(0).performTextInput("pivo")
        compose.onNodeWithText("Sačuvaj kategoriju").performClick()
        cekaj("Ta kategorija već postoji.")
        // Prozor ostaje otvoren sa unosom, ništa nije sačuvano.
        compose.onNodeWithText("Sačuvaj kategoriju").assertIsEnabled()
        assertEquals(listOf("Pivo"), naziviKategorija())
    }

    @Test fun kategorija_prekidacOnemogucenKadImaAktivnoPice() {
        val zestina = kategorija("Žestina")
        kategorija("Pivo")
        artikal("Smirnoff", zestina, "Doza")
        prikaziKategorije()
        cekaj("1 piće")
        compose.onNodeWithContentDescription("Žestina").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Pivo").assertIsEnabled()
        assertTrue(postoji("nema pića"))
    }

    @Test fun kategorija_slobodnaSeIskljucujeBezPotvrdeIPonovoUkljucuje() {
        val pivo = kategorija("Pivo")
        prikaziKategorije()
        cekaj("Pivo")
        compose.onNodeWithContentDescription("Pivo").performClick()
        cekajUslov { !repoPoziv { repo.kategorije.first().single { it.k.id == pivo }.k.aktivna } }
        cekajUslov { kvm.lista.value?.single()?.k?.aktivna == false } // ekran mora dobiti novo stanje prije sljedećeg dodira
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Pivo").performClick()
        cekajUslov { repoPoziv { repo.kategorije.first().single { it.k.id == pivo }.k.aktivna } }
    }

    @Test fun kategorija_ukljucivanjeSaZauzetimNazivomJavljaUToastu() {
        val prva = kategorija("Pivo")
        repoPoziv { assertEquals(Ishod.Ok, repo.postaviAktivnaKategorija(prva, false)) }
        kategorija("Pivo")
        prikaziKategorije()
        cekaj("Pivo")
        // Prvi red je isključena (starija) kategorija; uključivanje se odbija zbog aktivne istog naziva.
        compose.onAllNodesWithContentDescription("Pivo")[0].performClick()
        cekaj("Ta kategorija već postoji.")
    }

    @Test fun kategorija_prazanNazivJavljaObavezno() {
        prikaziKategorije()
        cekaj("Nova kategorija")
        compose.onNodeWithText("Nova kategorija").performClick()
        cekaj("Sačuvaj kategoriju")
        compose.onNodeWithText("Sačuvaj kategoriju").performClick()
        cekaj("Obavezno polje.")
        assertTrue(naziviKategorija().isEmpty())
    }

    @Test fun kategorija_duplikatSeJavljaPoslijeIzlaskaIzPolja() {
        kategorija("Pivo")
        prikaziKategorije()
        cekaj("Nova kategorija")
        compose.onNodeWithText("Nova kategorija").performClick()
        cekaj("Sačuvaj kategoriju")
        polje(0).performClick()
        polje(0).performTextInput("pivo")
        compose.waitForIdle()
        assertFalse(postoji("Ta kategorija već postoji.")) // dok se kuca, nema greške
        polje(0).performKeyInput { pressKey(Key.Tab) }
        compose.waitForIdle()
        cekaj("Ta kategorija već postoji.")
    }

    @Test fun kategorija_izmjenaPreimenujeIOdustaniPita() {
        val id = kategorija("Pivo")
        prikaziKategorije()
        cekaj("Pivo")
        compose.onNodeWithContentDescription("Izmijeni Pivo").performClick()
        cekaj("Izmjena · Pivo")
        polje(0).performTextReplacement("Pivo točeno")
        compose.onNodeWithText("Odustani").performClick()
        cekaj("Odbaciti izmjene?")
        assertFalse(postoji("Sačuvaj kategoriju")) // potvrda zamjenjuje sadržaj prozora (O5)
        compose.onNodeWithText("Nastavi unos").performClick()
        cekaj("Sačuvaj kategoriju")
        compose.onNodeWithText("Sačuvaj kategoriju").performClick()
        cekaj("Sačuvano: Pivo točeno")
        assertEquals("Pivo točeno", repoPoziv { repo.kategorije.first().single { it.k.id == id }.k.naziv })
    }

    @Test fun kategorija_pomjeranjeUpisujeRedoslijedSvihIUkljucenihIIskljucenih() {
        val a = kategorija("A"); val b = kategorija("B"); val c = kategorija("C")
        repoPoziv { assertEquals(Ishod.Ok, repo.postaviAktivnaKategorija(b, false)) }
        prikaziKategorije()
        cekajUslov { kvm.lista.value?.size == 3 }
        kvm.pomjeri(0, 2) // A na kraj
        cekajUslov { naziviKategorija() == listOf("B", "C", "A") }
        assertEquals(listOf(b, c, a), repoPoziv { repo.kategorije.first().map { it.k.id } })
    }

    // ---- V4 Mjere ----

    @Test fun mjere_pocetnaTabela() {
        prikaziMjere()
        cekaj("Boca 1 l")
        listOf("NAZIV U PREGLEDU", "LITARA", "DUGME", "U ZBIRU", "KORISTE JE").forEach { compose.onNodeWithText(it).assertExists() }
        listOf("Komad", "Doza", "Bokal", "Boca 0,7").forEach { assertTrue(it, postoji(it)) }
        assertTrue(postoji("—")) // Komad nema litre
        assertTrue(postoji("kom"))
        assertTrue(postoji("0,03"))
        assertTrue(postoji("1,00"))
        assertEquals(5, compose.onAllNodesWithText("nema pića").fetchSemanticsNodes().size)
    }

    @Test fun mjere_koristeJePrikazujeImenaIBroj() {
        val zestina = kategorija("Žestina")
        artikal("Rakija šljiva", zestina, "Doza", "Bokal")
        artikal("Rakija dunja", zestina, "Doza", "Bokal")
        artikal("Viljamovka", zestina, "Doza")
        artikal("Jelen", zestina, "Komad")
        prikaziMjere()
        cekaj("Rakija šljiva, Rakija dunja")
        assertTrue(postoji("3 pića")) // Doza
        assertTrue(postoji("Jelen"))
        compose.onNodeWithContentDescription("Doza").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Bokal").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Boca 0,7").assertIsEnabled()
    }

    @Test fun mjera_novaSaLitrama() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performTextInput("Čaša vina")
        polje(1).performTextInput("Čaša")
        polje(2).performTextInput("čaša")
        polje(3).performTextInput("0,2")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Sačuvano: Čaša vina")
        cekajNestanak("Sačuvaj mjeru")
        compose.onNodeWithText("Čaša vina").assertExists()
        compose.onNodeWithText("0,20").assertExists()
        val m = repoPoziv { repo.mjere.first().single { it.m.naziv == "Čaša vina" }.m }
        assertEquals(0.20, m.litara!!, 0.0)
        assertEquals("čaša", m.uZbiru)
    }

    @Test fun mjera_litreSaTackomIPrazneLitre() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performTextInput("Čaša")
        polje(1).performTextInput("Čaša")
        polje(2).performTextInput("čaša")
        polje(3).performTextInput("0.15")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Sačuvano: Čaša")
        assertEquals(0.15, repoPoziv { repo.mjere.first().single { it.m.naziv == "Čaša" }.m.litara!! }, 0.0)

        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performTextInput("Flaša")
        polje(1).performTextInput("Flaša")
        polje(2).performTextInput("flaša")
        compose.onNodeWithText("Sačuvaj mjeru").performClick() // prazne litre = komad
        cekaj("Sačuvano: Flaša")
        assertNull(repoPoziv { repo.mjere.first().single { it.m.naziv == "Flaša" }.m.litara })
    }

    @Test fun mjera_litreVanOpsega() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performTextInput("Bure")
        polje(1).performTextInput("Bure")
        polje(2).performTextInput("bure")
        polje(3).performTextInput("6")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Litre moraju biti od 0,01 do 5,00.")
        assertTrue(repoPoziv { repo.mjere.first().none { it.m.naziv == "Bure" } })
        // Prozor ostaje otvoren sa unosom; nečitljiv tekst daje istu poruku.
        polje(3).performTextInput("x")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Litre moraju biti od 0,01 do 5,00.")
    }

    @Test fun mjera_obaveznaPoljaIDuplikat() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Obavezno polje.")
        // Prozor je jedan spojeni čvor semantike; brojimo stavke teksta „Obavezno polje.“ u njemu.
        assertEquals(3, brojTekstova("Obavezno polje."))

        polje(0).performTextInput("doza")
        polje(1).performTextInput("D")
        polje(2).performTextInput("d")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Ta mjera već postoji.")
        assertEquals(1, repoPoziv { repo.mjere.first().count { it.m.naziv.equals("doza", ignoreCase = true) } })
    }

    @Test fun mjera_duplikatSeJavljaPoslijeIzlaskaIzPolja() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performClick()
        polje(0).performTextInput("Bokal")
        compose.waitForIdle()
        assertFalse(postoji("Ta mjera već postoji.")) // dok se kuca, nema greške
        polje(1).performClick() // fokus se eksplicitno prebacuje u drugo polje
        cekaj("Ta mjera već postoji.")
    }

    @Test fun mjera_izmjenaPrikazujeNapomenuICuvaLitre() {
        val id = mjera("Bokal")
        prikaziMjere()
        cekaj("Bokal")
        compose.onNodeWithContentDescription("Izmijeni Bokal").performClick()
        cekaj("Izmjena · Bokal")
        cekaj("Promjena litara važi od sljedeće ture. Stare ture čuvaju litre iz trenutka unosa.")
        compose.onNode(hasSetTextAction() and hasText("0,25")).assertExists()
        compose.onNode(hasSetTextAction() and hasText("0,25")).performTextReplacement("0,255")
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Sačuvano: Bokal")
        assertEquals(0.26, repoPoziv { repo.mjere.first().single { it.m.id == id }.m.litara!! }, 0.0)
    }

    @Test fun mjera_odbijenoCuvanjePrikazujeSeUProzoru() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performTextInput("Čaša")
        polje(1).performTextInput("Čaša")
        polje(2).performTextInput("čaša")
        // Dok je prozor otvoren, ista mjera nastaje drugdje; repo odbija, prozor to prikazuje.
        repoPoziv { assertEquals(Ishod.Ok, repo.sacuvajMjeru(null, "čaša", "Č", "č", null)) }
        compose.onNodeWithText("Sačuvaj mjeru").performClick()
        cekaj("Ta mjera već postoji.")
        compose.onNodeWithText("Sačuvaj mjeru").assertIsEnabled()
    }

    @Test fun mjera_odustaniSaIzmjenamaPita() {
        prikaziMjere()
        cekaj("Nova mjera")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        compose.onNodeWithText("Odustani").performClick() // bez izmjena zatvara
        cekajNestanak("Sačuvaj mjeru")
        compose.onNodeWithText("Nova mjera").performClick()
        cekaj("Sačuvaj mjeru")
        polje(0).performTextInput("Bure")
        compose.onNodeWithText("Odustani").performClick()
        cekaj("Odbaciti novu mjeru?")
        compose.onNodeWithText("Odbaci").performClick()
        cekajNestanak("Odbaciti novu mjeru?")
        assertFalse(postoji("Sačuvaj mjeru"))
    }

    @Test fun mjera_iskljucivanjeSlobodneIUkljucivanje() {
        val id = mjera("Boca 1 l")
        prikaziMjere()
        cekaj("Boca 1 l")
        compose.onNodeWithContentDescription("Boca 1 l").performClick()
        cekajUslov { !repoPoziv { repo.mjere.first().single { it.m.id == id }.m.aktivna } }
        cekajUslov { mvm.lista.value?.single { it.m.id == id }?.m?.aktivna == false } // ekran mora dobiti novo stanje prije sljedećeg dodira
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Boca 1 l").performClick()
        cekajUslov { repoPoziv { repo.mjere.first().single { it.m.id == id }.m.aktivna } }
    }
}
