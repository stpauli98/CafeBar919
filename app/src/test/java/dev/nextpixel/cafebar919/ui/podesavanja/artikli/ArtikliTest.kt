package dev.nextpixel.cafebar919.ui.podesavanja.artikli

import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.NocPrimalacE
import dev.nextpixel.cafebar919.data.StavkaTureE
import dev.nextpixel.cafebar919.data.TuraE
import dev.nextpixel.cafebar919.data.VrstaPrimaoca
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.ui.components.ToastHost
import dev.nextpixel.cafebar919.ui.components.ToastStanje
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
class ArtikliTest {
    @get:Rule val compose = createComposeRule()
    private val zona = ZoneId.systemDefault()
    private val danas = LocalDate.of(2026, 11, 21)
    private val sat = Clock.fixed(danas.atStartOfDay(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var repo: PodesavanjaRepo
    lateinit var vm: ArtikliViewModel

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

    private fun redoslijedNaziva(): List<String> = repoPoziv { repo.artikli.first().map { it.a.naziv } }

    private fun prikazi() {
        vm = ArtikliViewModel(repo)
        val toast = ToastStanje()
        compose.setContent {
            CafeBarTema {
                Box(Modifier.fillMaxSize()) {
                    ArtikliEkran(vm, toast)
                    ToastHost(toast, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst).fetchSemanticsNodes().isNotEmpty()
    private fun cekaj(tekst: String) = compose.waitUntil(5_000) { postoji(tekst) }
    private fun cekajNestanak(tekst: String) = compose.waitUntil(5_000) { !postoji(tekst) }

    /** Čeka uslov i pri tome ispumpava glavnu petlju (tokovi ViewModela rade na glavnoj niti). */
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(5_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }

    private fun unesiNaziv(naziv: String) = compose.onNode(hasSetTextAction()).performTextInput(naziv)

    @Test fun noviArtikal_sacuvanIVidiSeUListi() {
        val zestina = kategorija("Žestina")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        unesiNaziv("Viljamovka")
        compose.onNodeWithTag("kategorija:Žestina").performClick()
        compose.onNodeWithTag("mjera:Doza").performClick()
        compose.onNodeWithTag("mjera:Bokal").performClick()
        compose.onNodeWithText("Sačuvaj artikal").assertIsEnabled().performClick()
        cekaj("Sačuvano: Viljamovka")
        cekajNestanak("Sačuvaj artikal")
        compose.onNodeWithText("Viljamovka").assertExists()
        compose.onNodeWithText("Doza 0,03").assertExists()
        compose.onNodeWithText("Bokal 0,25").assertExists()
        val a = repoPoziv { repo.artikli.first().single() }
        assertEquals(zestina, a.a.kategorijaId)
        assertEquals(listOf("Doza", "Bokal"), a.mjere.map { it.naziv })
    }

    @Test fun sacuvajOnemogucenoBezMjere() {
        kategorija("Žestina")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        compose.onNodeWithText("Sačuvaj artikal").assertIsNotEnabled()
        unesiNaziv("Viljamovka")
        compose.onNodeWithTag("kategorija:Žestina").performClick()
        compose.onNodeWithText("Sačuvaj artikal").assertIsNotEnabled()
        compose.onNodeWithTag("mjera:Doza").performClick()
        compose.onNodeWithText("Sačuvaj artikal").assertIsEnabled()
    }

    @Test fun cetvrtaMjeraSeNeMozeIzabrati() {
        kategorija("Žestina")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        compose.onNodeWithTag("mjera:Doza").performClick()
        compose.onNodeWithTag("mjera:Bokal").performClick()
        compose.onNodeWithTag("mjera:Boca 0,7").performClick()
        compose.onNodeWithTag("mjera:Boca 1 l").assertIsNotEnabled()
        compose.onNodeWithTag("mjera:Komad").assertIsNotEnabled()
        compose.onNodeWithTag("mjera:Doza").assertIsEnabled()
        assertTrue(postoji("Boca 1,00 l"))
    }

    @Test fun duplikatPrikazujeArtikal01() {
        val zestina = kategorija("Žestina")
        artikal("Smirnoff", zestina, "Doza")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        unesiNaziv("smirnoff")
        compose.onNodeWithTag("kategorija:Žestina").performClick()
        compose.onNodeWithTag("mjera:Doza").performClick()
        compose.onNodeWithText("Sačuvaj artikal").performClick()
        cekaj("Taj naziv već postoji · ARTIKAL-01")
        // Prozor ostaje otvoren sa unesenim nazivom.
        compose.onNodeWithText("Sačuvaj artikal").assertExists()
        compose.onNode(hasSetTextAction() and hasText("smirnoff")).assertExists()
        assertEquals(1, redoslijedNaziva().size)
    }

    @Test fun duplikatSeJavljaPoslijeIzlaskaIzPoljaBezCuvanja() {
        val zestina = kategorija("Žestina")
        artikal("Smirnoff", zestina, "Doza")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        compose.onNode(hasSetTextAction()).performClick() // fokus u polje
        unesiNaziv("smirnoff")
        assertFalse(postoji("Taj naziv već postoji · ARTIKAL-01")) // dok se kuca, nema greške
        compose.onNode(hasSetTextAction()).performKeyInput { pressKey(Key.Tab) } // fokus napušta polje
        cekaj("Taj naziv već postoji · ARTIKAL-01")
    }

    @Test fun odbijenoCuvanjePrikazujeSeUProzoru() {
        val zestina = kategorija("Žestina")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        unesiNaziv("Viljamovka")
        compose.onNodeWithTag("kategorija:Žestina").performClick()
        compose.onNodeWithTag("mjera:Doza").performClick()
        repoPoziv { assertEquals(Ishod.Ok, repo.postaviAktivnaKategorija(zestina, false)) }
        compose.onNodeWithText("Sačuvaj artikal").performClick()
        cekaj("Prvo uključi kategoriju Žestina.")
        compose.onNodeWithText("Sačuvaj artikal").assertIsEnabled() // prozor ostaje otvoren sa unosom
        assertTrue(redoslijedNaziva().isEmpty())
    }

    @Test fun iskljucivanjeTraziPotvrdu() {
        val zestina = kategorija("Žestina")
        val id = artikal("Smirnoff", zestina, "Doza")
        prikazi()
        cekaj("Smirnoff")
        compose.onNodeWithContentDescription("Smirnoff").performClick()
        cekaj("Isključiti Smirnoff?")
        compose.onNodeWithText("Neće se nuditi na šanku. Stare noći ga i dalje prikazuju. Možeš ga ponovo uključiti ovdje.").assertExists()
        // Odustani ništa ne mijenja.
        compose.onNodeWithText("Odustani").performClick()
        cekajNestanak("Isključiti Smirnoff?")
        assertTrue(repoPoziv { repo.artikli.first().single { it.a.id == id }.a.aktivan })
        compose.onNodeWithContentDescription("Smirnoff").performClick()
        cekaj("Isključi")
        compose.onNodeWithText("Isključi").performClick()
        cekajUslov { !repoPoziv { repo.artikli.first().single { it.a.id == id }.a.aktivan } }
        // Ponovno uključivanje ide bez prozora.
        compose.onNodeWithContentDescription("Smirnoff").performClick()
        cekajUslov { repoPoziv { repo.artikli.first().single { it.a.id == id }.a.aktivan } }
        assertFalse(postoji("Isključiti Smirnoff?"))
    }

    @Test fun odustaniSaIzmjenamaPita() {
        kategorija("Žestina")
        prikazi()
        cekaj("Novi artikal")
        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        // Bez izmjena Odustani zatvara bez pitanja.
        compose.onNodeWithText("Odustani").performClick()
        cekajNestanak("Sačuvaj artikal")
        assertFalse(postoji("Odbaciti novi artikal?"))

        compose.onNodeWithText("Novi artikal").performClick()
        cekaj("Sačuvaj artikal")
        unesiNaziv("Viljamovka")
        compose.onNodeWithText("Odustani").performClick()
        cekaj("Odbaciti novi artikal?")
        compose.onNodeWithText("Uneseni naziv i mjera se ne čuvaju.").assertExists()
        // Potvrda zamjenjuje sadržaj prozora, ne otvara prozor preko prozora (O5).
        assertFalse(postoji("Sačuvaj artikal"))
        compose.onNodeWithText("Nastavi unos").performClick()
        cekaj("Sačuvaj artikal")
        compose.onNode(hasSetTextAction() and hasText("Viljamovka")).assertExists()
        compose.onNodeWithText("Odustani").performClick()
        cekaj("Odbaciti novi artikal?")
        compose.onNodeWithText("Odbaci").performClick()
        cekajNestanak("Odbaciti novi artikal?")
        assertFalse(postoji("Sačuvaj artikal"))
        assertTrue(redoslijedNaziva().isEmpty())
    }

    @Test fun listaPrikazujeMjereBezJediniceIFilterSuzavaListu() {
        val zestina = kategorija("Žestina"); val pivo = kategorija("Pivo")
        artikal("Rakija šljiva", zestina, "Doza", "Bokal")
        artikal("Jelen", pivo, "Komad")
        artikal("Jack Daniel’s 0,7", zestina, "Boca 0,7")
        prikazi()
        cekaj("Rakija šljiva")
        compose.onNodeWithText("Sve · 3").assertExists()
        compose.onNodeWithText("Doza 0,03").assertExists()
        compose.onNodeWithText("Bokal 0,25").assertExists()
        compose.onNodeWithText("Boca 0,70").assertExists()
        compose.onNodeWithText("Komad").assertExists()
        compose.onNode(hasText("Pivo") and isSelectable()).performClick()
        cekajNestanak("Rakija šljiva")
        compose.onNodeWithText("Jelen").assertExists()
        compose.onNode(hasText("Sve · 3") and isSelectable()).performClick()
        cekaj("Rakija šljiva")
    }

    @Test fun pomjeranjeSaFilteromMijesaSamoPrikazaneIUpisujeSve() {
        val zestina = kategorija("Žestina"); val pivo = kategorija("Pivo")
        artikal("A", zestina, "Doza"); artikal("B", pivo, "Komad"); artikal("C", zestina, "Doza")
        artikal("D", pivo, "Komad"); artikal("E", zestina, "Doza")
        prikazi()
        cekaj("A")
        vm.postaviFilter(zestina)
        cekajUslov { vm.lista.value?.map { it.a.naziv } == listOf("A", "C", "E") }
        vm.pomjeri(0, 2) // A prelazi na kraj prikazane liste
        cekajUslov { redoslijedNaziva() == listOf("C", "B", "E", "D", "A") }
        // Bez filtera pomjeranje važi za cijelu listu.
        vm.postaviFilter(null)
        cekajUslov { vm.lista.value?.size == 5 }
        vm.pomjeri(4, 0)
        cekajUslov { redoslijedNaziva() == listOf("A", "C", "B", "E", "D") }
    }

    @Test fun izmjena_naslovINapomenaSamoSaUnosimaUAktivnojNoci() {
        val zestina = kategorija("Žestina")
        val bokal = mjera("Bokal")
        val sa = artikal("Rakija šljiva", zestina, "Doza", "Bokal")
        artikal("Rakija dunja", zestina, "Doza", "Bokal")
        unesiUAktivnojNoci(sa, "Rakija šljiva", zestina, "Žestina", bokal, 3)
        prikazi()
        cekaj("Rakija šljiva")
        compose.onNodeWithContentDescription("Izmijeni Rakija šljiva").performClick()
        cekaj("Izmjena · Rakija šljiva")
        cekaj("Promjena važi od sljedeće ture. Ove noći je već uneseno bok. 3, to ostaje kako je uneseno.")
        compose.onNodeWithText("Sačuvaj izmjenu").assertExists()
        compose.onNodeWithText("Odustani").performClick()
        cekajNestanak("Izmjena · Rakija šljiva")

        compose.onNodeWithContentDescription("Izmijeni Rakija dunja").performClick()
        cekaj("Izmjena · Rakija dunja")
        assertTrue(compose.onAllNodesWithText("Promjena važi", substring = true).fetchSemanticsNodes().isEmpty())
    }

    @Test fun izmjenaSeCuvaIJavljaToast() {
        val zestina = kategorija("Žestina")
        val id = artikal("Smirnoff", zestina, "Doza")
        prikazi()
        cekaj("Smirnoff")
        compose.onNodeWithContentDescription("Izmijeni Smirnoff").performClick()
        cekaj("Izmjena · Smirnoff")
        compose.onNodeWithTag("mjera:Bokal").performClick()
        compose.onNodeWithText("Sačuvaj izmjenu").performClick()
        cekaj("Sačuvano: Smirnoff")
        cekajNestanak("Sačuvaj izmjenu")
        assertEquals(listOf("Doza", "Bokal"), repoPoziv { repo.artikli.first().single { it.a.id == id }.mjere.map { it.naziv } })
    }

    @Test fun ukljucivanjeOdbijenoKadJeKategorijaIskljucena() {
        val zestina = kategorija("Žestina")
        val id = artikal("Smirnoff", zestina, "Doza")
        repoPoziv {
            assertEquals(Ishod.Ok, repo.postaviAktivanArtikal(id, false))
            assertEquals(Ishod.Ok, repo.postaviAktivnaKategorija(zestina, false))
        }
        prikazi()
        cekaj("Smirnoff")
        compose.onNodeWithContentDescription("Smirnoff").performClick()
        cekaj("Prvo uključi kategoriju Žestina.")
        assertFalse(repoPoziv { repo.artikli.first().single().a.aktivan })
    }

    @Test fun ukljucivanjeOdbijenoKadJeMjeraIskljucena() {
        val zestina = kategorija("Žestina")
        val id = artikal("Smirnoff", zestina, "Doza")
        repoPoziv {
            assertEquals(Ishod.Ok, repo.postaviAktivanArtikal(id, false))
            assertEquals(Ishod.Ok, repo.postaviAktivnaMjera(mjera("Doza"), false))
        }
        prikazi()
        cekaj("Smirnoff")
        compose.onNodeWithContentDescription("Smirnoff").performClick()
        cekaj("Prvo uključi mjeru Doza.")
    }

    @Test fun ukljucivanjeOdbijenoKadPostojiAktivanIstiNaziv() {
        val zestina = kategorija("Žestina")
        val prvi = artikal("Smirnoff", zestina, "Doza")
        repoPoziv { assertEquals(Ishod.Ok, repo.postaviAktivanArtikal(prvi, false)) }
        artikal("Smirnoff", zestina, "Doza")
        prikazi()
        cekaj("Sve · 1")
        // Prvi red je isključeni Smirnoff (stariji); uključivanje se odbija zbog aktivnog istog naziva.
        compose.onAllNodesWithContentDescription("Smirnoff")[0].performClick()
        cekaj("Taj naziv već postoji · ARTIKAL-01")
        assertEquals(1, repoPoziv { repo.artikli.first().count { it.a.aktivan } })
    }

    private fun unesiUAktivnojNoci(artikalId: Long, naziv: String, kategorijaId: Long, kategorijaNaziv: String, mjeraId: Long, kolicina: Int) = repoPoziv {
        val d = db.nocDao()
        val m = repo.mjere.first().first { it.m.id == mjeraId }.m
        val noc = d.dodajNoc(NocE(datum = danas.toEpochDay(), otvorenaU = 1))
        val p = d.dodajPrimaoce(listOf(NocPrimalacE(nocId = noc, vrsta = VrstaPrimaoca.KONOBAR, radnikId = null, ime = "Vuleta", redoslijed = 0))).single()
        val t = d.dodajTuru(TuraE(nocId = noc, primalacId = p, potvrdjenaU = 2, kljuc = "t1"))
        d.dodajStavke(
            listOf(
                StavkaTureE(
                    turaId = t, artikalId = artikalId, artikalNaziv = naziv, artikalRedoslijed = 0, kategorijaId = kategorijaId,
                    kategorijaNaziv = kategorijaNaziv, kategorijaRedoslijed = 0, mjeraId = m.id, mjeraNaziv = m.naziv,
                    mjeraNatpis = m.natpis, mjeraUZbiru = m.uZbiru, mjeraRedoslijed = m.redoslijed, litara = m.litara, kolicina = kolicina,
                ),
            ),
        )
    }
}
