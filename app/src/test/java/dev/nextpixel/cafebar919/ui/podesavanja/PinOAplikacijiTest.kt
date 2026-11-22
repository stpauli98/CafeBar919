package dev.nextpixel.cafebar919.ui.podesavanja

import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.BuildConfig
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.repo.BrojaciMenija
import dev.nextpixel.cafebar919.repo.PinServis
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.repo.ProvjeraPina
import dev.nextpixel.cafebar919.ui.components.ToastHost
import dev.nextpixel.cafebar919.ui.components.ToastStanje
import dev.nextpixel.cafebar919.ui.podesavanja.oaplikaciji.OAplikacijiEkran
import dev.nextpixel.cafebar919.ui.podesavanja.pin.PromjenaPinaEkran
import dev.nextpixel.cafebar919.ui.podesavanja.pin.PromjenaPinaViewModel
import dev.nextpixel.cafebar919.ui.theme.CafeBarTema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
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
import java.time.Instant
import java.time.ZoneId

// Tablet 1280×800 kao u Paperu.
@Config(qualifiers = "w1280dp-h800dp")
@RunWith(RobolectricTestRunner::class)
class PinOAplikacijiTest {
    @get:Rule val compose = createComposeRule()
    private val sat = MutableClock(Instant.parse("2026-11-21T18:00:00Z"), ZoneId.of("UTC"))
    private val prikazano = mutableStateOf(true)
    private val sesijaScope = CoroutineScope(Dispatchers.Default)
    lateinit var db: AppDatabase
    lateinit var repo: PodesavanjaRepo
    lateinit var pin: PinServis

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        repo = PodesavanjaRepo(db, sat)
        pin = PinServis(db, sat)
    }

    @After fun tearDown() {
        sesijaScope.cancel()
        // Sadržaj se uklanja prije zatvaranja baze: tokovi koji slušaju Room ne smiju se ponovo pitati poslije close().
        prikazano.value = false
        compose.waitForIdle()
        db.close()
    }

    private fun <T> poziv(blok: suspend () -> T): T = runBlocking { blok() }

    /** Pravi ljuska (sa banerom probnog PIN-a kao u AppNav) oko ekrana promjene PIN-a. */
    private fun prikaziPromjenu() {
        val vm = PromjenaPinaViewModel(pin, sat)
        val toast = ToastStanje()
        val sesija = PodesavanjaSesija(sesijaScope) { 600 }
        compose.setContent {
            CafeBarTema {
                if (!prikazano.value) return@CafeBarTema
                val probni by pin.probni.collectAsState(false)
                Box(Modifier.fillMaxSize()) {
                    Ljuska(Sekcija.PIN, {}, {}, sesija, BrojaciMenija(0, 0, 0, 0), probni) { PromjenaPinaEkran(vm, toast) }
                    ToastHost(toast, Modifier.align(Alignment.BottomCenter))
                }
            }
        }
        // Baner probnog PIN-a pomjera raspored kad se učita; tek poslije toga se dodiruje tastatura.
        if (poziv { pin.probni.first() }) cekaj(baner)
    }

    private fun postoji(tekst: String) = compose.onAllNodesWithText(tekst, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
    private fun cekaj(tekst: String) = compose.waitUntil(10_000) { shadowOf(Looper.getMainLooper()).idle(); postoji(tekst) }
    private fun cekajNestanak(tekst: String) = compose.waitUntil(10_000) { shadowOf(Looper.getMainLooper()).idle(); !postoji(tekst) }
    private fun cekajUslov(uslov: () -> Boolean) = compose.waitUntil(10_000) { shadowOf(Looper.getMainLooper()).idle(); uslov() }

    // Brojevi 1–3 postoje i u stepperu (lijevo); tastatura je poslije njega u stablu.
    private fun taster(c: String) = compose.onAllNodesWithText(c).onLast()
    private fun upisi(cifre: String) = cifre.forEach { taster(it.toString()).performClick() }

    private val baner = "Još se koristi probni PIN 123456."

    @Test fun promjenaPina_cijeliTok() {
        prikaziPromjenu()
        cekaj(baner)
        cekaj("Korak 1 od 3 · Trenutni PIN")
        upisi("123456")
        cekaj("Korak 2 od 3 · Novi PIN")
        upisi("482915")
        cekaj("Korak 3 od 3 · Ponovi novi PIN")
        upisi("482915")
        cekaj("PIN je promijenjen.")
        cekaj("Korak 1 od 3 · Trenutni PIN")
        cekajNestanak(baner)
        assertFalse(poziv { pin.probni.first() })
        // Provjere pišu brojač u bazu; ovo je zato zadnje.
        assertTrue(poziv { pin.provjeri("123456") } is ProvjeraPina.Pogresan)
        assertEquals(ProvjeraPina.Tacan, poziv { pin.provjeri("482915") })
        compose.waitForIdle()
    }

    @Test fun promjenaPina_pogresanTrenutni() {
        prikaziPromjenu()
        cekaj("Korak 1 od 3")
        upisi("000001")
        cekaj("PIN-01")
        cekaj("Korak 1 od 3")
        assertTrue(poziv { pin.probni.first() })
        assertEquals(ProvjeraPina.Tacan, poziv { pin.provjeri("123456") })
    }

    @Test fun promjenaPina_nepoklapanje() {
        prikaziPromjenu()
        cekaj("Korak 1 od 3")
        upisi("123456")
        cekaj("Korak 2 od 3")
        upisi("482915")
        cekaj("Korak 3 od 3")
        upisi("482916")
        cekaj("PIN-ovi se ne poklapaju. Upiši novi PIN ponovo.")
        cekaj("Korak 2 od 3")
        assertTrue(poziv { pin.probni.first() })
    }

    @Test fun promjenaPina_zabranjen() {
        prikaziPromjenu()
        cekaj("Korak 1 od 3")
        upisi("123456")
        cekaj("Korak 2 od 3")
        upisi("111111")
        cekaj("Taj PIN je lako pogoditi. Izaberi drugi.")
        cekaj("Korak 2 od 3")
    }

    @Test fun promjenaPina_blokadaPoslijeDesetPogresnih_onemogucujeTastaturu() {
        prikaziPromjenu()
        cekaj("Korak 1 od 3")
        repeat(10) {
            upisi("000001")
            cekajUslov { postoji("PIN-01") || postoji("PIN-02") }
            if (it < 9) cekajNestanak("PIN-02")
            // Poruka se briše prvom novom cifrom; sljedeći pokušaj kreće čist.
        }
        cekaj("Novi pokušaj je moguć u 18:05. · PIN-02")
        taster("5").assertIsNotEnabled()
    }

    @Test fun promjenaPina_blokiranPriOtvaranju() {
        repeat(10) { poziv { pin.provjeri("000001") } }
        prikaziPromjenu()
        cekaj("Novi pokušaj je moguć u 18:05. · PIN-02")
        taster("1").assertIsNotEnabled()
    }

    @Test fun promjenaPina_blokadaIstice_tastaturaSeVraca() {
        repeat(10) { poziv { pin.provjeri("000001") } }
        prikaziPromjenu()
        cekaj("Novi pokušaj je moguć u 18:05. · PIN-02")
        taster("1").assertIsNotEnabled()
        sat.pomjeri(java.time.Duration.ofMinutes(5))
        compose.waitUntil(10_000) { shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(2)); !postoji("PIN-02") }
        taster("1").assertIsEnabled()
        upisi("123456")
        cekaj("Korak 2 od 3 · Novi PIN")
    }

    @Test fun oAplikaciji_istorijaIzPostavki() {
        poziv { repo.postaviIstoriju(45) }
        compose.setContent { CafeBarTema { if (prikazano.value) OAplikacijiEkran(repo) } }
        cekaj("45 dana · podešava se u Pravilima rada")
        compose.onNodeWithText("Evidencija pića za Caffe Bar 919.").assertExists()
        compose.onNodeWithText(BuildConfig.VERSION_NAME).assertExists()
        compose.onNodeWithText("NextPixel · www.nextpixel.dev").assertExists()
        compose.onNodeWithText("+387 66 603 900").assertExists()
        compose.onNodeWithText("Pomoć oko aplikacije · NextPixel, Nikola").assertExists()
        compose.onNodeWithText("Za greške u radu aplikacije i kad se evidencija ne otvara (BAZA-01). Zaboravljen PIN se ne može poništiti.").assertExists()
    }
}
