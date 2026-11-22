package dev.nextpixel.cafebar919.e2e

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Vlasnik uređuje meni i pravila kroz podešavanja (V1, V2, V4, V5, V6), pa se promjena vidi na šanku. */
@RunWith(AndroidJUnit4::class)
class PodesavanjaE2ETest : E2ETest() {

    private fun novaKategorija(naziv: String) {
        dodirni("Kategorije")
        cekaj("Nova kategorija")
        dodirni("Nova kategorija")
        cekaj("Sačuvaj kategoriju")
        upisi(naziv)
        dodirni("Sačuvaj kategoriju")
        cekaj("Sačuvano: $naziv")
    }

    private fun noviArtikal(naziv: String, kategorija: String, vararg mjere: String) {
        dodirni("Artikli")
        cekaj("Novi artikal")
        dodirni("Novi artikal")
        cekaj("Sačuvaj artikal")
        upisi(naziv)
        dodirniOznaku("kategorija:$kategorija")
        mjere.forEach { dodirniOznaku("mjera:$it") }
        dodirni("Sačuvaj artikal")
        cekaj("Sačuvano: $naziv")
    }

    @Test fun meniKrozPodesavanja_vidiSeNaSanku() {
        otkljucajPodesavanja()
        novaKategorija("Rakija")
        noviArtikal("Šljivovica Sokolova", "Rakija", "Doza", "Bokal")
        cekaj("Doza 0,03")
        cekaj("Bokal 0,25")
        dodirni("Nazad na unos")
        otvoriNoc("Vuleta")
        cekaj("Rakija")
        cekaj("Šljivovica Sokolova")
        cekaj("0,03")
        cekaj("Bokal")
    }

    @Test fun kategorija_duplikatIPrazno() {
        otkljucajPodesavanja()
        novaKategorija("Pivo")
        dodirni("Nova kategorija")
        cekaj("Sačuvaj kategoriju")
        dodirni("Sačuvaj kategoriju")
        cekaj("Obavezno polje.")
        upisi("pivo")
        dodirni("Sačuvaj kategoriju")
        cekaj("Ta kategorija već postoji.")
        assertEquals(1L, broj("SELECT COUNT(*) FROM kategorija"))
    }

    @Test fun artikal_duplikatIPreimenovanjeUPostojeci_ARTIKAL01() {
        standardniMeni()
        otkljucajPodesavanja()
        dodirni("Artikli")
        cekaj("Novi artikal")
        dodirni("Novi artikal")
        cekaj("Sačuvaj artikal")
        upisi("jelen 0,5")
        dodirniOznaku("kategorija:Pivo")
        dodirniOznaku("mjera:Komad")
        dodirni("Sačuvaj artikal")
        cekaj("Taj naziv već postoji · ARTIKAL-01")
        dodirni("Odustani")
        cekaj("Odbaciti novi artikal?")
        dodirni("Odbaci")
        cekajNestanak("Sačuvaj artikal")

        dodirniOpis("Izmijeni Nektar 0,5")
        cekaj("Izmjena · Nektar 0,5")
        compose.onNode(hasSetTextAction()).performTextClearance()
        upisi("Jelen 0,5")
        dodirni("Sačuvaj izmjenu")
        cekaj("Taj naziv već postoji · ARTIKAL-01")
        assertEquals(1L, broj("SELECT COUNT(*) FROM artikal WHERE naziv = 'Nektar 0,5'"))
    }

    @Test fun noviArtikal_neNudiIskljuceneKategorijeIMjere() {
        otkljucajPodesavanja()
        novaKategorija("Sokovi")
        novaKategorija("Kafa")
        // „Sokovi“ nema pića, pa se isključuje bez potvrde; „Boca 1 l“ ne koristi nijedno piće.
        dodirniOpis("Sokovi")
        cekajUslov("Kategorija nije isključena") { broj("SELECT aktivna FROM kategorija WHERE naziv = 'Sokovi'") == 0L }
        dodirni("Mjere")
        cekaj("Nova mjera")
        dodirniOpis("Boca 1 l")
        cekajUslov("Mjera nije isključena") { broj("SELECT aktivna FROM mjera WHERE naziv = 'Boca 1 l'") == 0L }
        dodirni("Artikli")
        dodirni("Novi artikal")
        cekaj("Sačuvaj artikal")
        cekaj("Kafa")
        assertTrue(compose.onAllNodesWithTag("kategorija:Kafa").fetchSemanticsNodes().isNotEmpty())
        assertTrue("Isključena kategorija se nudi", compose.onAllNodesWithTag("kategorija:Sokovi").fetchSemanticsNodes().isEmpty())
        assertTrue("Isključena mjera se nudi", compose.onAllNodesWithTag("mjera:Boca 1 l").fetchSemanticsNodes().isEmpty())
    }

    @Test fun mjeraIKategorijaUUpotrebi_neMoguSeIskljuciti() {
        standardniMeni()
        otkljucajPodesavanja()
        dodirni("Mjere")
        cekaj("Nova mjera")
        runCatching { dodirniOpis("Bokal") }
        Thread.sleep(800)
        assertEquals("Bokal koristi Brkina šljiva, a isključen je", 1L, broj("SELECT aktivna FROM mjera WHERE naziv = 'Bokal'"))
        dodirni("Kategorije")
        cekaj("Nova kategorija")
        runCatching { dodirniOpis("Pivo") }
        Thread.sleep(800)
        assertEquals("Pivo ima aktivna pića, a isključeno je", 1L, broj("SELECT aktivna FROM kategorija WHERE naziv = 'Pivo'"))
    }

    @Test fun mjera_litreVanOpsegaIPrazanNaziv() {
        otkljucajPodesavanja()
        dodirni("Mjere")
        cekaj("Nova mjera")
        dodirni("Nova mjera")
        cekaj("Sačuvaj mjeru")
        upisi("Čaša vina", 0)
        upisi("Čaša", 1)
        upisi("čaša", 2)
        upisi("9", 3)
        dodirni("Sačuvaj mjeru")
        cekaj("Litre moraju biti od 0,01 do 5,00.")
        compose.onAllNodes(hasSetTextAction())[3].performTextClearance()
        upisi("0,2", 3)
        dodirni("Sačuvaj mjeru")
        cekaj("Sačuvano: Čaša vina")
        assertEquals(20L, broj("SELECT CAST(ROUND(litara * 100) AS INTEGER) FROM mjera WHERE naziv = 'Čaša vina'"))
    }

    @Test fun noviRadnik_pojavljujeSeNaS0() {
        otkljucajPodesavanja()
        dodirni("Ljudi")
        cekaj("Novi radnik")
        dodirni("Novi radnik")
        cekaj("Sačuvaj radnika")
        dodirni("Sačuvaj radnika")
        cekaj("Obavezno polje.")
        upisi("Đorđe")
        dodirni("Sačuvaj radnika")
        cekaj("Sačuvano: Đorđe")
        dodirni("Nazad na unos")
        cekaj("Ko radi večeras?")
        cekaj("Đorđe")
    }

    @Test fun pravila_rokZaPonistavanjeVaziNaSanku() {
        standardniMeni()
        otkljucajPodesavanja()
        dodirni("Pravila rada")
        cekaj("60 s")
        dodirniOpis("Plus", 0)
        cekaj("70 s")
        assertEquals(70L, broj("SELECT rok_bez_pina_s FROM postavke"))
        dodirni("Nazad na unos")
        otvoriNoc("Vuleta")
        dodirni("Vuleta"); dodirni("Jelen 0,5")
        dodirni("Potvrdi · Vuleta")
        cekajUslov("Odbrojavanje ne kreće od 70 s") { postoji("Poništi · 70 s") || postoji("Poništi · 69 s") || postoji("Poništi · 68 s") }
    }

    @Test fun pravila_zakljucavanjeMijenjaRokSesije() {
        otkljucajPodesavanja()
        cekaj("Zaključava se samo za 1 min", dio = true)
        dodirni("Pravila rada")
        cekaj("2 min")
        dodirniOpis("Plus", 2)
        cekaj("3 min")
        assertEquals(180L, broj("SELECT zakljucavanje_s FROM postavke"))
        cekajUslov("Baner ne prati novi rok zaključavanja") { postoji("Zaključava se samo za 2 min", dio = true) || postoji("Zaključava se samo za 3 min", dio = true) }
        assertFalse(postoji("Zaključava se samo za 1 min", dio = true))
    }

    @Test fun pravila_posljednjiRazlogSeNeMozeUkloniti() {
        otkljucajPodesavanja()
        dodirni("Pravila rada")
        cekaj("Razlozi poništavanja")
        listOf("Pogrešan konobar", "Pogrešno piće", "Pogrešna količina").forEach {
            dodirniOpis("Ukloni $it")
            cekajNestanak(it)
        }
        cekaj("Uneseno dvaput")
        assertTrue(compose.onAllNodesWithContentDescription("Ukloni Uneseno dvaput").fetchSemanticsNodes().isEmpty())
        assertEquals(1L, broj("SELECT COUNT(*) FROM razlog_ponistavanja WHERE aktivan = 1"))
    }

    @Test fun nazadNaUnos_zakljucavaPodesavanja() {
        otkljucajPodesavanja()
        dodirni("Nazad na unos")
        cekaj("Ko radi večeras?")
        dodirni("Podešavanja")
        cekaj("Odustani")
        assertFalse(postoji("Otključano za vlasnika."))
        assertTrue(compose.onAllNodes(hasText("1") and hasClickAction()).fetchSemanticsNodes().isNotEmpty())
    }
}
