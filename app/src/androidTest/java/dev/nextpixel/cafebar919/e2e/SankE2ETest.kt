package dev.nextpixel.cafebar919.e2e

import android.view.ViewGroup
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** S0 Nova noć, S0.1 Ispomoć, S1 Unos ture, traka „Posljednje“ i poništavanje bez PIN-a, na pravom uređaju. */
@RunWith(AndroidJUnit4::class)
class SankE2ETest : E2ETest() {

    private fun dodirniDio(tekst: String) {
        cekajUslov("Nema dugmeta sa „$tekst“") { compose.onAllNodes(hasText(tekst, substring = true) and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }
        compose.onAllNodes(hasText(tekst, substring = true) and hasClickAction())[0].performClick()
        compose.waitForIdle()
    }

    private fun potvrdi(ime: String) {
        cekaj("Potvrdi · $ime")
        compose.onNodeWithText("Potvrdi · $ime").assertIsEnabled().performClick()
        compose.waitForIdle()
    }

    private fun tureUBazi() = broj("SELECT COUNT(*) FROM tura")

    // ---- S0 / S0.1 ----

    @Test fun s0_otvaranjeNoci_samoIzabraniISankNaS1() {
        otvoriNoc("Vuleta", "Miloš")
        listOf("Vuleta", "Miloš", "Šank").forEach { cekaj(it) }
        assertFalse("Srđan nije izabran, a vidi se na S1", postoji("Srđan"))
        assertFalse("Smitran nije izabran, a vidi se na S1", postoji("Smitran"))
        assertEquals(1L, broj("SELECT COUNT(*) FROM noc WHERE zatvorena_u IS NULL"))
        assertEquals(3L, broj("SELECT COUNT(*) FROM noc_primalac"))
    }

    @Test fun s0_1_ispomoc_validacijaPaDodavanjeIPrikazNaS1() {
        cekaj("Ko radi večeras?")
        dodirni("Dodaj ispomoć")
        cekaj("Ispomoć večeras")
        dodirni("Dodaj")
        cekaj("Upiši ime.")
        upisi("Vuleta")
        dodirni("Dodaj")
        cekaj("To ime već postoji večeras.")
        compose.onNode(hasSetTextAction()).performTextClearance()
        upisi("Šank")
        dodirni("Dodaj")
        cekaj("To ime već postoji večeras.")
        compose.onNode(hasSetTextAction()).performTextClearance()
        upisi("Dejan Đurić")
        dodirni("Dodaj")
        cekajNestanak("Ispomoć večeras")
        cekaj("Dejan Đurić")
        dodirni("Vuleta")
        dodirni("Otvori noć · 2 radnika")
        cekaj("Noć otvorena od", dio = true)
        cekaj("ISPOMOĆ")
        cekaj("Dejan Đurić")
        assertEquals(1L, broj("SELECT COUNT(*) FROM noc_primalac WHERE vrsta = 'ISPOMOC' AND ime = 'Dejan Đurić'"))
    }

    @Test fun s0_1_odustaniNeDodajeIspomoc() {
        cekaj("Ko radi večeras?")
        dodirni("Dodaj ispomoć")
        cekaj("Ispomoć večeras")
        upisi("Marko")
        dodirni("Odustani")
        cekajNestanak("Ispomoć večeras")
        assertFalse(postoji("Marko"))
        cekaj("Otvori noć · 0 radnika")
    }

    // ---- S1: T2, tura, zbir ----

    @Test fun t2_bezPica_porukaIPrazanTraka() {
        otvoriNoc("Vuleta")
        cekaj("Još nema pića")
        cekaj("Još nema tura ove noći.")
        dodirni("Vuleta")
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
    }

    @Test fun tura_odIzboraDoZbiraNaPlocici_iUBazi() {
        standardniMeni()
        otvoriNoc("Vuleta", "Miloš")
        dodirni("Vuleta")
        cekaj("TURA ZA")
        dodirni("Jelen 0,5")
        dodirni("Jelen 0,5")
        dodirni("0,03")
        dodirni("Bokal")
        cekaj("0,03 · 1")
        cekaj("Bokal · 1")
        potvrdi("Vuleta")
        cekaj("Upisano: Vuleta · 3 stavke")
        cekaj("kom 2 · doza 1 · bok. 1")
        cekaj("Jelen 0,5 2", dio = true)
        cekaj("Nova tura")
        assertEquals(1L, tureUBazi())
        assertEquals(4L, broj("SELECT SUM(kolicina) FROM stavka_ture"))
        // Litre se čuvaju kao snimak: doza 0,03 + bokal 0,25.
        assertEquals(28L, broj("SELECT CAST(ROUND(SUM(litara * kolicina) * 100) AS INTEGER) FROM stavka_ture WHERE litara IS NOT NULL"))
    }

    @Test fun triTure_jednaNaSank_svakoSvojZbir() {
        standardniMeni()
        otvoriNoc("Vuleta", "Miloš")
        dodirni("Vuleta"); dodirni("Jelen 0,5"); potvrdi("Vuleta")
        cekaj("Upisano: Vuleta · 1 stavka")
        dodirni("Miloš"); dodirni("Espresso"); dodirni("Espresso"); potvrdi("Miloš")
        cekaj("Upisano: Miloš · 1 stavka")
        dodirni("Šank"); dodirni("Nektar 0,5"); potvrdi("Šank")
        cekaj("Upisano: Šank · 1 stavka")
        assertEquals(3L, tureUBazi())
        assertEquals(1L, broj("SELECT COUNT(*) FROM tura t JOIN noc_primalac p ON p.id = t.primalac_id WHERE p.vrsta = 'SANK'"))
        cekaj("kom 1")
        cekaj("kom 2")
    }

    @Test fun dvostrukiDodirNaPotvrdi_jednaTura() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Vuleta")
        dodirni("Jelen 0,5")
        cekaj("Potvrdi · Vuleta")
        val dugme = compose.onNodeWithText("Potvrdi · Vuleta")
        dugme.performClick()
        runCatching { dugme.performClick() }
        cekaj("Upisano: Vuleta · 1 stavka")
        Thread.sleep(1000)
        assertEquals(1L, tureUBazi())
    }

    @Test fun stepper_viseManjeIUklanjanjeStavke() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Vuleta")
        dodirni("Jelen 0,5")
        dodirniOpis("Više")
        cekajUslov("Količina nije 2") { compose.onAllNodes(hasText("Jelen 0,5") and hasText("2")).fetchSemanticsNodes().isNotEmpty() || postoji("2") }
        dodirniOpis("Manje")
        dodirniOpis("Manje")
        cekaj("Dodaj piće u turu.")
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
    }

    // ---- poništavanje bez PIN-a ----

    @Test fun ponistiUTraci_uRoku_vanZbiraIUBazi() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Vuleta"); dodirni("Jelen 0,5"); potvrdi("Vuleta")
        cekaj("kom 1")
        cekaj("Poništi · ", dio = true)
        dodirniDio("Poništi · ")
        cekaj("Poništeno bez PIN-a")
        cekaj("još ništa")
        assertEquals(1L, broj("SELECT COUNT(*) FROM tura WHERE ponistena_u IS NOT NULL AND ponistena_bez_pina = 1"))
    }

    @Test fun ponistiIzToasta_uRoku() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Vuleta"); dodirni("Jelen 0,5"); potvrdi("Vuleta")
        cekaj("Upisano: Vuleta · 1 stavka")
        compose.onNode(hasText("Poništi") and hasClickAction()).performClick()
        cekaj("Poništeno bez PIN-a")
        cekaj("još ništa")
        assertEquals(1L, broj("SELECT COUNT(*) FROM tura WHERE ponistena_u IS NOT NULL"))
    }

    // ---- pretraga, filter, nacrt ----

    @Test fun pretraga_bezKvacicaT1IOcisti() {
        standardniMeni()
        otvoriNoc("Vuleta")
        cekaj("Jelen 0,5")
        upisi("sljiva")
        cekaj("Brkina šljiva")
        cekajNestanak("Jelen 0,5")
        compose.onNode(hasSetTextAction()).performTextClearance()
        upisi("mojito")
        cekaj("Nema pića „mojito“")
        dodirni("Očisti pretragu")
        cekaj("Jelen 0,5")
        cekaj("Espresso")
    }

    @Test fun filterKategorije_cipSuzavaPica() {
        standardniMeni()
        otvoriNoc("Vuleta")
        cekaj("Espresso")
        dodirni("Pivo")
        cekaj("Jelen 0,5")
        cekajNestanak("Espresso")
        assertFalse(postoji("Brkina šljiva"))
        dodirni("Sve")
        cekaj("Espresso")
    }

    @Test fun nacrt_preziviPonovnoPravljenjeEkrana() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Vuleta")
        dodirni("Jelen 0,5")
        cekaj("Potvrdi · Vuleta")
        // Nacrt mora biti u bazi prije nego što ekran nestane.
        cekajUslov("Nacrt nije upisan") { baza().query("SELECT stavke FROM nacrt_ture WHERE id = 1").use { it.moveToFirst(); it.getString(0) }.contains("kolicina") }
        compose.activityRule.scenario.recreate()
        cekaj("Potvrdi · Vuleta", ms = 20_000)
        assertEquals(0L, tureUBazi())
    }

    @Test fun s1_ekranSeNeGasi() {
        standardniMeni()
        otvoriNoc("Vuleta")
        cekaj("Jelen 0,5")
        var ukljuceno = false
        compose.activityRule.scenario.onActivity { a ->
            // UnosEkran postavlja keepScreenOn na LocalView (AndroidComposeView), koji je dublje u stablu.
            fun trazi(v: android.view.View): Boolean =
                v.keepScreenOn || (v is ViewGroup && (0 until v.childCount).any { trazi(v.getChildAt(it)) })
            ukljuceno = trazi(a.window.decorView)
        }
        assertTrue("KEEP_SCREEN_ON nije uključen na S1", ukljuceno)
    }

    // ---- veza sa podešavanjima ----

    @Test fun promjenaUlogeUTokuNoci_neMijenjaOtvorenuNoc() {
        standardniMeni()
        otvoriNoc("Vuleta", "Miloš")
        cekaj("ZA ŠANKOM")
        otkljucajPodesavanja()
        dodirni("Ljudi")
        cekaj("Stalni radnici")
        // Redoslijed: Vuleta, Srđan, Miloš, Smitran; Miloš je treći, pa je njegov „Konobar“ treći.
        compose.onAllNodes(hasText("Konobar") and hasClickAction())[2].performClick()
        cekajUslov("Uloga nije sačuvana") { broj("SELECT COUNT(*) FROM radnik WHERE ime = 'Miloš' AND uloga = 'KONOBAR'") == 1L }
        dodirni("Nazad na unos")
        cekaj("ZA ŠANKOM")
        cekaj("Miloš")
        assertEquals(1L, broj("SELECT COUNT(*) FROM noc_primalac WHERE ime = 'Miloš' AND vrsta = 'ZA_SANKOM'"))
    }

    @Test fun mjeraSkinutaSaPicaUNacrtu_stavkaIspadaIPotvrdiOnemoguceno() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Vuleta")
        dodirni("Bokal")
        cekaj("Bokal · 1")
        otkljucajPodesavanja()
        dodirniOpis("Izmijeni Brkina šljiva")
        cekaj("Izmjena · Brkina šljiva")
        dodirniOznaku("mjera:Bokal")
        dodirni("Sačuvaj izmjenu")
        cekaj("Sačuvano: Brkina šljiva")
        dodirni("Nazad na unos")
        cekaj("TURA ZA")
        cekaj("Dodaj piće u turu.")
        compose.onNodeWithText("Potvrdi").assertIsNotEnabled()
    }

    @Test fun unosTureRadiZaVrijemePinBlokade() {
        standardniMeni()
        otvoriNoc("Vuleta")
        dodirni("Podešavanja")
        cekaj("Odustani")
        repeat(10) { i ->
            pinTastatura("000001")
            cekajUslov("Pokušaj ${i + 1} nije upisan") { broj("SELECT pin_pogresnih_zaredom FROM postavke") == (i + 1).toLong() || postoji("PIN-02", dio = true) }
        }
        cekaj("PIN-02", dio = true)
        dodirni("Odustani")
        cekajNestanak("Odustani")
        dodirni("Vuleta"); dodirni("Jelen 0,5"); potvrdi("Vuleta")
        cekaj("Upisano: Vuleta · 1 stavka")
        assertEquals(1L, tureUBazi())
    }

    @Test fun iskljucenRadnikSeNeNudiNaS0() {
        otkljucajPodesavanja()
        dodirni("Ljudi")
        cekaj("Stalni radnici")
        dodirniOpis("Smitran")
        cekaj("Isključiti Smitran?")
        dodirni("Isključi")
        cekajNestanak("Isključiti Smitran?")
        dodirni("Nazad na unos")
        cekaj("Ko radi večeras?")
        cekaj("Miloš")
        assertFalse("Isključen Smitran se nudi na S0", postoji("Smitran"))
    }
}
