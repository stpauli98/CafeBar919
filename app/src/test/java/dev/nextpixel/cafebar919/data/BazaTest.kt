package dev.nextpixel.cafebar919.data

import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.domain.Pin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BazaTest {
    lateinit var db: AppDatabase
    @Before fun setUp() { db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null) }
    @After fun tearDown() = db.close()

    @Test fun pocetneMjere() = runTest {
        val m = db.mjeraDao().sve().first()
        assertEquals(listOf("Komad","Doza","Bokal","Boca 0,7","Boca 1 l"), m.map { it.naziv })
        assertEquals(listOf("Komad","0,03","Bokal","Boca","Boca 1 l"), m.map { it.natpis })
        assertEquals(listOf("kom","doza","bok.","boca 0,7","boca 1 l"), m.map { it.uZbiru })
        assertEquals(listOf(null, 0.03, 0.25, 0.70, 1.00), m.map { it.litara })
        assertTrue(m.all { it.aktivna })
    }
    @Test fun pocetniRadnici() = runTest {
        val r = db.radnikDao().svi().first()
        assertEquals(listOf("Vuleta" to Uloga.KONOBAR, "Srđan" to Uloga.KONOBAR, "Miloš" to Uloga.ZA_SANKOM, "Smitran" to Uloga.ZA_SANKOM), r.map { it.ime to it.uloga })
    }
    @Test fun pocetniRazloziBezVraceno() = runTest {
        assertEquals(listOf("Pogrešan konobar","Pogrešno piće","Pogrešna količina","Uneseno dvaput"), db.razlogDao().svi().first().map { it.tekst })
    }
    @Test fun nemaKategorijaNiArtikala() = runTest {
        assertTrue(db.kategorijaDao().sve().first().isEmpty()); assertTrue(db.artikalDao().svi().first().isEmpty())
    }
    @Test fun pocetnePostavke() = runTest {
        val p = db.postavkeDao().jedne()
        assertEquals(60, p.rokBezPinaS); assertEquals(30, p.istorijaDana); assertEquals(120, p.zakljucavanjeS)
        assertTrue(p.ispomocDozvoljena); assertTrue(p.pinProbni); assertTrue(Pin.provjeri("123456", p.pinHes, p.pinSo))
    }
}
