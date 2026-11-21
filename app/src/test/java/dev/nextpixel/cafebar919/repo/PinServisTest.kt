package dev.nextpixel.cafebar919.repo

import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.domain.Pin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class PinServisTest {
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDateTime.of(2026, 11, 21, 20, 0).atZone(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var s: PinServis

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        s = PinServis(db, sat)
    }
    @After fun tearDown() = db.close()

    @Test fun desetPogresnih_blokira5min_paPustaPonovo() = runTest {
        repeat(9) { assertEquals(ProvjeraPina.Pogresan(9 - it), s.provjeri("000001")) }
        assertEquals(ProvjeraPina.Blokiran(sat.instant().plus(Duration.ofMinutes(5))), s.provjeri("000001"))
        assertTrue(s.provjeri("123456") is ProvjeraPina.Blokiran)
        sat.pomjeri(Duration.ofMinutes(5))
        assertEquals(ProvjeraPina.Tacan, s.provjeri("123456"))
    }

    @Test fun tacanBriseBrojac() = runTest {
        repeat(9) { s.provjeri("000001") }
        s.provjeri("123456")
        assertEquals(ProvjeraPina.Pogresan(9), s.provjeri("000001"))
    }

    @Test fun blokadaPreziviNovuInstancuServisa() = runTest {
        repeat(10) { s.provjeri("000001") }
        assertTrue(PinServis(db, sat).provjeri("123456") is ProvjeraPina.Blokiran)
    }

    @Test fun blokiranDo_nullKadNijeBlokiran_paVrijemeKadJeste() = runTest {
        assertNull(s.blokiranDo())
        repeat(10) { s.provjeri("000001") }
        assertEquals(sat.instant().plus(Duration.ofMinutes(5)), s.blokiranDo())
        sat.pomjeri(Duration.ofMinutes(5))
        assertNull(s.blokiranDo())
    }

    @Test fun poIstekuBlokadeBrojacKreceOdNule() = runTest {
        repeat(10) { s.provjeri("000001") }
        sat.pomjeri(Duration.ofMinutes(5))
        assertEquals(ProvjeraPina.Pogresan(9), s.provjeri("000001"))
        assertNull(db.postavkeDao().jedne().pinBlokiranDo)
    }

    @Test fun unosKojiNijeSestCifaraBrojiSeKaoPogresan() = runTest {
        assertEquals(ProvjeraPina.Pogresan(9), s.provjeri("12"))
        assertEquals(ProvjeraPina.Pogresan(8), s.provjeri("abcdef"))
    }

    @Test fun promjena() = runTest {
        assertEquals(PromjenaPina.PogresanTrenutni, s.promijeni("111112", "482915"))
        assertEquals(PromjenaPina.Zabranjen, s.promijeni("123456", "777777"))
        assertEquals(PromjenaPina.NeispravanFormat, s.promijeni("123456", "4829"))
        assertTrue(s.probni.first())
        assertEquals(PromjenaPina.Uspjeh, s.promijeni("123456", "482915"))
        assertFalse(s.probni.first())
        assertEquals(ProvjeraPina.Tacan, s.provjeri("482915"))
    }

    @Test fun promjena_pogresanTrenutniBrojiSeIDesetiBlokira() = runTest {
        repeat(9) { assertEquals(PromjenaPina.PogresanTrenutni, s.promijeni("111112", "482915")) }
        assertEquals(
            PromjenaPina.Blokiran(sat.instant().plus(Duration.ofMinutes(5))),
            s.promijeni("111112", "482915"),
        )
        assertEquals(
            PromjenaPina.Blokiran(sat.instant().plus(Duration.ofMinutes(5))),
            s.promijeni("123456", "482915"),
        )
    }

    @Test fun promjena_tacanTrenutniBriseBrojac() = runTest {
        repeat(5) { s.provjeri("000001") }
        assertEquals(PromjenaPina.Zabranjen, s.promijeni("123456", "777777"))
        assertEquals(ProvjeraPina.Pogresan(9), s.provjeri("000001"))
    }

    @Test fun noviPinSeCuvaKaoHes() = runTest {
        s.promijeni("123456", "482915")
        val p = db.postavkeDao().jedne()
        assertTrue(Pin.provjeri("482915", p.pinHes, p.pinSo))
        assertFalse(p.pinProbni)
    }
}
