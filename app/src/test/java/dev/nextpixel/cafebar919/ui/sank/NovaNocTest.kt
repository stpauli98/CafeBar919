package dev.nextpixel.cafebar919.ui.sank

import android.os.Looper
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.VrstaPrimaoca
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
class NovaNocTest {
    @get:Rule val compose = createComposeRule()
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDate.of(2026, 11, 21).atTime(21, 0).atZone(zona).toInstant())
    lateinit var db: AppDatabase
    lateinit var podesavanja: PodesavanjaRepo
    lateinit var noc: NocRepo
    lateinit var vm: NovaNocViewModel

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        podesavanja = PodesavanjaRepo(db, sat)
        noc = NocRepo(db, sat)
    }

    @After fun tearDown() = db.close()

    private fun prikazi() {
        vm = NovaNocViewModel(podesavanja, noc, sat, poslijeOtvaranja = {})
        compose.setContent { CafeBarTema { NovaNocEkran(vm, onPodesavanja = {}) } }
    }

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst).fetchSemanticsNodes().isNotEmpty()
    private fun cekaj(tekst: String) = cekajUslov { postoji(tekst) }
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(5_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }

    private fun dodajIspomoc(ime: String) {
        compose.onNodeWithText("Dodaj ispomoć").performClick()
        cekaj("Ispomoć večeras")
        compose.onNode(hasSetTextAction()).performTextInput(ime)
        compose.onNodeWithText("Dodaj").performClick()
    }

    @Test fun s0_prikazujeAktivneRadnikeNeoznacene() {
        prikazi()
        cekaj("Vuleta")
        compose.onNodeWithText("KONOBARI").assertExists()
        compose.onNodeWithText("ZA ŠANKOM").assertExists()
        listOf("Vuleta", "Srđan", "Miloš", "Smitran").forEach { compose.onNodeWithText(it).assertExists() }
        compose.onNodeWithText("Vuleta").assertIsOff()
        compose.onNodeWithText("Smitran").assertIsOff()
        compose.onNodeWithText("Otvori noć · 0 radnika").assertExists()
        compose.onNodeWithText("Ko radi večeras?").assertExists()
        compose.onNodeWithText("ISPOMOĆ · SAMO VEČERAS").assertExists()
    }

    @Test fun s0_brojRadnikaSeSlaze() {
        prikazi()
        cekaj("Vuleta")
        compose.onNodeWithText("Vuleta").performClick()
        cekaj("Otvori noć · 1 radnik")
        compose.onNodeWithText("Vuleta").assertIsOn()
        compose.onNodeWithText("Srđan").performClick()
        cekaj("Otvori noć · 2 radnika")
        compose.onNodeWithText("Vuleta").performClick()
        cekaj("Otvori noć · 1 radnik")
    }

    @Test fun s0_ispomocSeDodajeOznacenaIBroji() {
        prikazi()
        cekaj("Vuleta")
        dodajIspomoc("Dejan")
        cekaj("Otvori noć · 1 radnik")
        compose.onNodeWithText("Dejan").assertIsOn()
        compose.onNodeWithText("ispomoć").assertExists()
        // Odznačavanje dodirom kao i ostali.
        compose.onNodeWithText("Dejan").performClick()
        cekaj("Otvori noć · 0 radnika")
    }

    @Test fun ispomoc_dupliNazivOstajeUProzoru() {
        prikazi()
        cekaj("Vuleta")
        dodajIspomoc("vuleta")
        cekaj("To ime već postoji večeras.")
        compose.onNodeWithText("Ispomoć večeras").assertExists()
        compose.onNodeWithText("Odustani").assertExists()
        assertEquals(0, compose.onAllNodesWithText("ispomoć").fetchSemanticsNodes().size)
        assertEquals(emptyList<Pair<String, Boolean>>(), vm.stanje.value.ispomoc)
    }

    @Test fun ispomoc_praznoIme() {
        prikazi()
        cekaj("Vuleta")
        dodajIspomoc("  ")
        cekaj("Upiši ime.")
        compose.onNodeWithText("Ispomoć večeras").assertExists()
    }

    @Test fun s0_ispomocIskljucenaUPravilima() {
        runBlocking { podesavanja.postaviIspomoc(false) }
        prikazi()
        cekaj("Vuleta")
        assertEquals(0, compose.onAllNodesWithText("ISPOMOĆ · SAMO VEČERAS").fetchSemanticsNodes().size)
        assertEquals(0, compose.onAllNodesWithText("Dodaj ispomoć").fetchSemanticsNodes().size)
    }

    @Test fun s0_otvoriNocUpisujeOznacene() {
        prikazi()
        cekaj("Vuleta")
        compose.onNodeWithText("Vuleta").performClick()
        dodajIspomoc("Dejan")
        cekaj("Otvori noć · 2 radnika")
        compose.onNodeWithText("Otvori noć · 2 radnika").performClick()
        val aktivna = runBlocking {
            var n = noc.aktivna.first()
            repeat(50) { if (n == null) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(50); n = noc.aktivna.first() } }
            n
        }
        assertNotNull(aktivna)
        val primaoci = runBlocking { noc.primaoci(aktivna!!.id).first() }
        assertEquals(listOf("Vuleta", "Dejan", "Šank"), primaoci.map { it.ime })
        assertEquals(listOf(VrstaPrimaoca.KONOBAR, VrstaPrimaoca.ISPOMOC, VrstaPrimaoca.SANK), primaoci.map { it.vrsta })
    }

    @Test fun s0_napomenaSaDatumom() {
        prikazi()
        cekaj("Vuleta")
        compose.onNodeWithText(
            "Šank za prosuto je uvijek uključen. Datum noći ostaje subota 21. 11. i poslije ponoći.",
        ).assertExists()
        compose.onNodeWithText("Subota 21. 11.").assertExists()
    }

    private fun brojNoci(): Int = db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM noc").use { it.moveToFirst(); it.getInt(0) }

    private fun cekajAktivnu() = runBlocking {
        var n = noc.aktivna.first()
        repeat(50) { if (n == null) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(50); n = noc.aktivna.first() } }
        n
    }

    @Test fun s0_dvaDodiraNaOtvoriJednaNoc() {
        prikazi()
        cekaj("Vuleta")
        compose.onNodeWithText("Otvori noć · 0 radnika").performClick()
        cekaj("Upisuje se…")
        compose.onNodeWithText("Upisuje se…").performClick()
        vm.otvoriNoc()
        assertNotNull(cekajAktivnu())
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(1, brojNoci())
        assertTrue(vm.stanje.value.otvara)
        assertTrue(postoji("Upisuje se…"))
    }

    @Test fun s0_otvoriNocBezIkogaSamoSank() {
        prikazi()
        cekaj("Vuleta")
        compose.onNodeWithText("Otvori noć · 0 radnika").assertIsEnabled().performClick()
        val aktivna = cekajAktivnu()
        assertNotNull(aktivna)
        val primaoci = runBlocking { noc.primaoci(aktivna!!.id).first() }
        assertEquals(listOf("Šank"), primaoci.map { it.ime })
    }

    @Test fun s0_ispomocIskljucena_neUlaziUNoc() {
        prikazi()
        cekaj("Vuleta")
        dodajIspomoc("Dejan")
        cekaj("Otvori noć · 1 radnik")
        runBlocking { podesavanja.postaviIspomoc(false) }
        cekajUslov { !postoji("Dejan") }
        compose.onNodeWithText("Otvori noć · 0 radnika").performClick()
        val aktivna = cekajAktivnu()
        assertNotNull(aktivna)
        val primaoci = runBlocking { noc.primaoci(aktivna!!.id).first() }
        assertEquals(emptyList<Any>(), primaoci.filter { it.vrsta == VrstaPrimaoca.ISPOMOC })
    }

    @Test fun s0_greskaPriOtvaranju_baner() {
        prikazi()
        cekaj("Vuleta")
        db.openHelper.writableDatabase.execSQL("CREATE TRIGGER kvar BEFORE INSERT ON noc BEGIN SELECT RAISE(ABORT, 'kvar'); END")
        compose.onNodeWithText("Otvori noć · 0 radnika").performClick()
        cekajUslov { compose.onAllNodes(hasText("Noć nije otvorena.", substring = true)).fetchSemanticsNodes().isNotEmpty() }
        assertEquals(0, brojNoci())
        db.openHelper.writableDatabase.execSQL("DROP TRIGGER kvar")
        compose.onNodeWithText("Otvori noć · 0 radnika").performClick()
        assertNotNull(cekajAktivnu())
        assertEquals(1, brojNoci())
    }

    @Test fun s0_datumSeOsvjezava() {
        prikazi()
        cekaj("Subota 21. 11.")
        sat.pomjeri(Duration.ofDays(1))
        vm.osvjeziDan()
        cekaj("Nedjelja 22. 11.")
        compose.onNodeWithText(
            "Šank za prosuto je uvijek uključen. Datum noći ostaje nedjelja 22. 11. i poslije ponoći.",
        ).assertExists()
    }
}
