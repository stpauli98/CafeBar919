package dev.nextpixel.cafebar919.ui.podesavanja

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.repo.BrojaciMenija
import dev.nextpixel.cafebar919.repo.PinServis
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime
import java.time.ZoneId

// Tablet 1280×800 kao u Paperu; na zadanom malom ekranu stavke menija ispadaju iz prozora.
@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class PinProzorTest {
    @get:Rule val compose = createComposeRule()
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDateTime.of(2026, 11, 21, 23, 53).atZone(zona).toInstant(), zona)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    lateinit var db: AppDatabase
    lateinit var pin: PinServis

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        pin = PinServis(db, sat)
    }

    @After fun tearDown() { scope.cancel(); db.close() }

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst, substring = true).fetchSemanticsNodes().isNotEmpty()

    private fun unesi(cifre: String) = cifre.forEach { compose.onNodeWithText(it.toString()).performClick() }

    @Test fun pinProzor_tacanPinOtkljucava() {
        var otkljucano = 0
        compose.setContent { CafeBarTema { PinProzor(pin, { otkljucano++ }, {}, sat) } }
        unesi("123456")
        compose.waitUntil(5_000) { otkljucano == 1 }
        assertEquals(1, otkljucano)
    }

    @Test fun pinProzor_pogresanPrikazujePin01IBriseCifre() {
        var otkljucano = 0
        compose.setContent { CafeBarTema { PinProzor(pin, { otkljucano++ }, {}, sat) } }
        unesi("000001")
        compose.waitUntil(5_000) { postoji("PIN-01") }
        // Cifre su obrisane: novih šest cifara daje tačan PIN, a da je ostala šesta cifra, sedma bi bila odbačena.
        unesi("123456")
        compose.waitUntil(5_000) { otkljucano == 1 }
        assertEquals(1, otkljucano)
    }

    @Test fun pinProzor_blokiranPrikazujePin02IOnemogucenaTastatura() {
        compose.setContent { CafeBarTema { PinProzor(pin, {}, {}, sat) } }
        repeat(10) { i ->
            unesi("000001")
            if (i < 9) compose.waitUntil(5_000) { postoji("PIN-01") }
            if (i < 9) compose.waitForIdle()
        }
        compose.waitUntil(5_000) { postoji("PIN-02") }
        compose.onNodeWithText("Previše pogrešnih pokušaja. Novi pokušaj je moguć u 23:58. · PIN-02").assertExists()
        compose.onNodeWithText("1").assertIsNotEnabled()
    }

    @Test fun pinProzor_blokadaIstice_tastaturaSeVraca() {
        var otkljucano = 0
        compose.setContent { CafeBarTema { PinProzor(pin, { otkljucano++ }, {}, sat) } }
        repeat(10) { i ->
            unesi("000001")
            if (i < 9) compose.waitUntil(5_000) { postoji("PIN-01") }
            if (i < 9) compose.waitForIdle()
        }
        compose.waitUntil(5_000) { postoji("PIN-02") }
        compose.onNodeWithText("1").assertIsNotEnabled()
        sat.pomjeri(java.time.Duration.ofMinutes(5))
        compose.waitUntil(10_000) { !postoji("PIN-02") }
        compose.onNodeWithText("1").assertIsEnabled()
        unesi("123456")
        compose.waitUntil(5_000) { otkljucano == 1 }
    }

    @Test fun pinProzor_blokiranPriOtvaranju_istice() {
        repeat(10) { kotlinx.coroutines.runBlocking { pin.provjeri("000001") } }
        compose.setContent { CafeBarTema { PinProzor(pin, {}, {}, sat) } }
        compose.waitUntil(5_000) { postoji("PIN-02") }
        sat.pomjeri(java.time.Duration.ofMinutes(5))
        compose.waitUntil(10_000) { !postoji("PIN-02") }
        compose.onNodeWithText("1").assertIsEnabled()
    }

    @Test fun pin02_minutaSeZaokruzujeNagore() {
        val utc = java.time.ZoneId.of("UTC")
        val sat = java.time.Clock.fixed(java.time.Instant.parse("2026-11-21T22:00:00Z"), utc)
        assertEquals("Previše pogrešnih pokušaja. Novi pokušaj je moguć u 22:06. · PIN-02", pin02(java.time.Instant.parse("2026-11-21T22:05:40Z"), sat))
        assertEquals("Previše pogrešnih pokušaja. Novi pokušaj je moguć u 22:05. · PIN-02", pin02(java.time.Instant.parse("2026-11-21T22:05:00Z"), sat))
        assertEquals("Previše pogrešnih pokušaja. Novi pokušaj je moguć u 22:06. · PIN-02", pin02(java.time.Instant.parse("2026-11-21T22:05:00.001Z"), sat))
    }

    @Test fun pinProzor_odustaniZatvara() {
        var odustao = false
        compose.setContent { CafeBarTema { PinProzor(pin, {}, { odustao = true }, sat) } }
        compose.onNodeWithText("Odustani").performClick()
        assertTrue(odustao)
    }

    @Test fun ljuska_baneriIMeni() {
        val sesija = PodesavanjaSesija(scope, { 120 }); sesija.otkljucaj()
        compose.setContent {
            CafeBarTema {
                Ljuska(Sekcija.ARTIKLI, {}, {}, sesija, BrojaciMenija(15, 3, 6, 4), probniPin = true) {}
            }
        }
        compose.onNodeWithText("Još se koristi probni PIN 123456.", substring = true).assertExists()
        compose.onNodeWithText("15 pića · redoslijed").assertExists()
        compose.onNodeWithText("3 kategorije").assertExists()
        compose.onNodeWithText("6 mjera u litrima").assertExists()
        compose.onNodeWithText("4 stalna radnika").assertExists()
        compose.onNodeWithText("Sve promjene važe odmah, bez nove verzije aplikacije.").assertExists()
    }

    @Test fun ljuska_bezProbnogPinaNemaDrugogBanera() {
        val sesija = PodesavanjaSesija(scope, { 120 }); sesija.otkljucaj()
        compose.setContent {
            CafeBarTema { Ljuska(Sekcija.MJERE, {}, {}, sesija, BrojaciMenija(1, 1, 1, 1), probniPin = false) {} }
        }
        compose.onNodeWithText("Još se koristi probni PIN", substring = true).assertDoesNotExist()
        compose.onNodeWithText("1 piće · redoslijed").assertExists()
    }

    @Test fun ljuska_zakljucajSadaINazadZakljucavaju() {
        val sesija = PodesavanjaSesija(scope, { 120 }); sesija.otkljucaj()
        var nazad = 0
        compose.setContent {
            CafeBarTema { Ljuska(Sekcija.ARTIKLI, {}, { nazad++ }, sesija, BrojaciMenija(0, 0, 0, 0), probniPin = false) {} }
        }
        compose.onNodeWithText("Zaključaj sada").performClick()
        assertFalse(sesija.otkljucano.value)
        sesija.otkljucaj()
        compose.onNodeWithText("Nazad na unos").performClick()
        assertFalse(sesija.otkljucano.value)
        assertEquals(1, nazad)
    }

    @Test fun ljuska_izborSekcijeJavljaSekciju() {
        val sesija = PodesavanjaSesija(scope, { 120 }); sesija.otkljucaj()
        val izabrana = mutableIntStateOf(-1)
        compose.setContent {
            CafeBarTema { Ljuska(Sekcija.ARTIKLI, { izabrana.intValue = it.ordinal }, {}, sesija, BrojaciMenija(0, 0, 0, 0), probniPin = false) {} }
        }
        compose.onNodeWithText("PIN vlasnika").performClick()
        assertEquals(Sekcija.PIN.ordinal, izabrana.intValue)
    }

    @Test fun pinProzor_blokiranPriOtvaranjuPrikazujePin02() {
        kotlinx.coroutines.runBlocking { repeat(10) { pin.provjeri("000001") } }
        compose.setContent { CafeBarTema { PinProzor(pin, {}, {}, sat) } }
        compose.waitUntil(5_000) { postoji("PIN-02") }
        compose.onNodeWithText("Previše pogrešnih pokušaja. Novi pokušaj je moguć u 23:58. · PIN-02").assertExists()
        compose.onNodeWithText("1").assertIsNotEnabled()
    }

    @Test fun ljuska_dodirURadnomDijeluVracaBrojac() {
        var rok = 120
        val sesija = PodesavanjaSesija(scope, { rok }); sesija.otkljucaj()
        rok = 77
        compose.setContent {
            CafeBarTema {
                Ljuska(Sekcija.ARTIKLI, {}, {}, sesija, BrojaciMenija(1, 1, 1, 1), probniPin = false) {
                    Box(Modifier.fillMaxSize().testTag("radni"))
                }
            }
        }
        compose.onNodeWithTag("radni").performClick()
        assertEquals(77, sesija.preostaloS.value)
    }

    @Test fun ljuska_artikliBezPicaKazeNemaPica() {
        val sesija = PodesavanjaSesija(scope, { 120 }); sesija.otkljucaj()
        compose.setContent {
            CafeBarTema { Ljuska(Sekcija.ARTIKLI, {}, {}, sesija, BrojaciMenija(0, 0, 0, 0), probniPin = false) {} }
        }
        compose.onNodeWithText("nema pića · redoslijed").assertExists()
    }
}
