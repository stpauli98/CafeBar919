package dev.nextpixel.cafebar919.e2e

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/** V0 PIN, ljuska podešavanja i V3 promjena PIN-a, od prve instalacije. */
@RunWith(AndroidJUnit4::class)
class PinE2ETest : E2ETest() {

    @Test fun prviStart_S0_radniciIZaključanaPodesavanja() {
        cekaj("Ko radi večeras?")
        listOf("Vuleta", "Srđan", "Miloš", "Smitran").forEach { cekaj(it) }
        cekaj("Šank za prosuto je uvijek uključen", dio = true)
        cekaj("Podešavanja")
    }

    @Test fun pogresanPin_PIN01_paIspravanOtvaraPodesavanjaSaUpozorenjemZaProbniPin() {
        cekaj("Ko radi večeras?")
        dodirni("Podešavanja")
        cekaj("Odustani")
        pinTastatura("111111")
        cekaj("PIN nije tačan. Unesi ponovo. · PIN-01")
        pinTastatura("123456")
        cekaj("Otključano za vlasnika.")
        cekaj("Još se koristi probni PIN 123456.", dio = true)
        listOf("Artikli", "Kategorije", "Mjere", "Ljudi", "PIN vlasnika", "Pravila rada", "O aplikaciji").forEach { cekaj(it) }
    }

    @Test fun odustaniOdPina_ostajeNaSanku() {
        cekaj("Ko radi večeras?")
        dodirni("Podešavanja")
        cekaj("Odustani")
        dodirni("Odustani")
        cekajNestanak("Odustani")
        cekaj("Ko radi večeras?")
    }

    @Test fun desetPogresnih_PIN02_blokadaIIspravanPinNeOtvara() {
        cekaj("Ko radi večeras?")
        dodirni("Podešavanja")
        cekaj("Odustani")
        repeat(10) { i ->
            pinTastatura("000001")
            // Provjera PIN-a traje (PBKDF2); sljedeći pokušaj tek kad je ovaj upisan u bazu.
            cekajUslov("Pokušaj ${i + 1} nije upisan") { broj("SELECT pin_pogresnih_zaredom FROM postavke") == (i + 1).toLong() || postoji("PIN-02", dio = true) }
        }
        cekaj("Previše pogrešnih pokušaja.", dio = true)
        cekaj("PIN-02", dio = true)
        // Za vrijeme blokade tastatura je onemogućena (V0-08), pa ni tačan PIN ne otključava.
        "1234567890".forEach { c ->
            compose.onNode(androidx.compose.ui.test.hasTextExactly(c.toString()) and androidx.compose.ui.test.hasClickAction())
                .assertIsNotEnabled()
        }
        pinTastatura("123456")
        Thread.sleep(1500)
        org.junit.Assert.assertFalse("Tačan PIN je otključao za vrijeme blokade", postoji("Otključano za vlasnika."))
        org.junit.Assert.assertEquals(10L, broj("SELECT pin_pogresnih_zaredom FROM postavke"))
    }

    @Test fun zakljucajSada_traziPinPonovo() {
        otkljucajPodesavanja()
        dodirni("Zaključaj sada")
        cekajNestanak("Otključano za vlasnika.")
        cekaj("Ko radi večeras?")
        dodirni("Podešavanja")
        cekaj("Odustani")
    }

    @Test fun promjenaPina_zabranjenNepoklapanjePaNoviRadiStariNe() {
        otkljucajPodesavanja()
        dodirni("PIN vlasnika")
        cekaj("Promjena PIN-a")
        cekaj("KORAK 1 OD 3", dio = true)
        pinTastatura("123456")
        cekaj("KORAK 2 OD 3", dio = true)
        pinTastatura("111111")
        cekaj("Taj PIN je lako pogoditi. Izaberi drugi.")
        pinTastatura("482915")
        cekaj("KORAK 3 OD 3", dio = true)
        pinTastatura("482916")
        cekaj("PIN-ovi se ne poklapaju. Upiši novi PIN ponovo.")
        pinTastatura("482915")
        cekaj("KORAK 3 OD 3", dio = true)
        pinTastatura("482915")
        cekaj("PIN je promijenjen.")
        cekajNestanak("Još se koristi probni PIN 123456.")

        dodirni("Zaključaj sada")
        cekaj("Ko radi večeras?")
        dodirni("Podešavanja")
        cekaj("Odustani")
        pinTastatura("123456")
        cekaj("PIN nije tačan. Unesi ponovo. · PIN-01")
        pinTastatura("482915")
        cekaj("Otključano za vlasnika.")
    }

    @Test fun oAplikaciji_verzijaIKontakt() {
        otkljucajPodesavanja()
        dodirni("O aplikaciji")
        cekaj("Evidencija pića za Caffe Bar 919.")
        cekaj("NextPixel · www.nextpixel.dev")
        cekaj("Zaboravljen PIN se ne može poništiti.", dio = true)
        cekaj("1.1-debug", dio = true)
    }
}
