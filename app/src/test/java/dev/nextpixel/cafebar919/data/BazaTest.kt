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

    private suspend fun napraviNocSTurom(kljuc: String): Triple<Long, Long, Long> {
        val noc = db.nocDao().dodajNoc(NocE(datum = 20_000, otvorenaU = 1_000))
        val primalac = db.nocDao().dodajPrimaoce(listOf(NocPrimalacE(nocId = noc, vrsta = VrstaPrimaoca.KONOBAR, radnikId = null, ime = "Vuleta", redoslijed = 0))).single()
        val tura = db.nocDao().dodajTuru(TuraE(nocId = noc, primalacId = primalac, potvrdjenaU = 2_000, kljuc = kljuc))
        db.nocDao().dodajStavke(listOf(StavkaTureE(turaId = tura, artikalId = 1, artikalNaziv = "Jelen pivo", artikalRedoslijed = 0, kategorijaId = 1, kategorijaNaziv = "Pivo", kategorijaRedoslijed = 0, mjeraId = 1, mjeraNaziv = "Komad", mjeraNatpis = "Komad", mjeraUZbiru = "kom", mjeraRedoslijed = 0, litara = null, kolicina = 2)))
        return Triple(noc, primalac, tura)
    }

    private fun broj(tabela: String): Int =
        db.openHelper.writableDatabase.query("SELECT COUNT(*) FROM $tabela").use { it.moveToFirst(); it.getInt(0) }

    @Test fun brisanjeNociBriseSveVezano() = runTest {
        val (noc) = napraviNocSTurom("k1")
        assertEquals(1, broj("noc_primalac")); assertEquals(1, broj("tura")); assertEquals(1, broj("stavka_ture"))
        db.openHelper.writableDatabase.execSQL("DELETE FROM noc WHERE id = ?", arrayOf<Any>(noc))
        assertEquals(0, broj("noc")); assertEquals(0, broj("noc_primalac")); assertEquals(0, broj("tura")); assertEquals(0, broj("stavka_ture"))
    }

    @Test fun istiKljucDvaput_pada() = runTest {
        val (noc, primalac) = napraviNocSTurom("isti")
        val e = runCatching { db.nocDao().dodajTuru(TuraE(nocId = noc, primalacId = primalac, potvrdjenaU = 3_000, kljuc = "isti")) }
        assertTrue(e.exceptionOrNull() is android.database.sqlite.SQLiteConstraintException)
        assertEquals(1, broj("tura"))
    }

    @Test fun brisanjePrimaocaSaTurom_pada() = runTest {
        val (_, primalac) = napraviNocSTurom("k2")
        val e = runCatching { db.openHelper.writableDatabase.execSQL("DELETE FROM noc_primalac WHERE id = ?", arrayOf<Any>(primalac)) }
        assertTrue(e.exceptionOrNull() is android.database.sqlite.SQLiteConstraintException)
        assertEquals(1, broj("noc_primalac")); assertEquals(1, broj("tura"))
    }
}
