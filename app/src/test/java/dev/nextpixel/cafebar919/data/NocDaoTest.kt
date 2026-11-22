package dev.nextpixel.cafebar919.data

import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.data.dao.NocDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NocDaoTest {
    lateinit var db: AppDatabase
    lateinit var dao: NocDao
    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        dao = db.nocDao()
    }
    @After fun tearDown() = db.close()

    private suspend fun primalac(nocId: Long, ime: String = "Vuleta", redoslijed: Int = 0) =
        dao.dodajPrimaoce(listOf(NocPrimalacE(nocId = nocId, vrsta = VrstaPrimaoca.KONOBAR, radnikId = null, ime = ime, redoslijed = redoslijed))).single()

    private fun stavka(turaId: Long, kolicina: Int) = StavkaTureE(
        turaId = turaId, artikalId = 1, artikalNaziv = "Jelen pivo", artikalRedoslijed = 0, kategorijaId = 1,
        kategorijaNaziv = "Pivo", kategorijaRedoslijed = 0, mjeraId = 1, mjeraNaziv = "Komad", mjeraNatpis = "Komad",
        mjeraUZbiru = "kom", mjeraRedoslijed = 0, litara = null, kolicina = kolicina,
    )

    @Test fun aktivna_samoNezatvorena() = runTest {
        dao.dodajNoc(NocE(datum = 20_000, otvorenaU = 1, zatvorenaU = 2))
        assertNull(dao.aktivnaJednom())
        val id = dao.dodajNoc(NocE(datum = 20_001, otvorenaU = 3))
        assertEquals(id, dao.aktivna().first()?.id)
        assertEquals(id, dao.aktivnaJednom()?.id)
    }

    @Test fun primaoci_poRedoslijedu() = runTest {
        val noc = dao.dodajNoc(NocE(datum = 20_000, otvorenaU = 1))
        primalac(noc, "B", 1); primalac(noc, "A", 0)
        assertEquals(listOf("A", "B"), dao.primaoci(noc).first().map { it.ime })
        assertEquals(listOf("A", "B"), dao.primaociJednom(noc).map { it.ime })
    }

    @Test fun nacrt_iTure() = runTest {
        assertNull(dao.nacrt())
        val noc = dao.dodajNoc(NocE(datum = 20_000, otvorenaU = 1))
        val p = primalac(noc)
        dao.upisiNacrt(NacrtTureE(primalacId = p, kljuc = "k0", izmijenjenU = 5))
        assertEquals("k0", dao.nacrt()?.kljuc)
        val t = dao.dodajTuru(TuraE(nocId = noc, primalacId = p, potvrdjenaU = 2, kljuc = "k1"))
        assertEquals(t, dao.turaPoKljucu("k1")?.id)
        dao.izmijeniTuru(dao.tura(t)!!.copy(ponistenaU = 9))
        assertEquals(9L, dao.tura(t)?.ponistenaU)
    }

    @Test fun posljednjeTure_najnovijePrvoSaStavkamaIPrimaocem() = runTest {
        val noc = dao.dodajNoc(NocE(datum = 20_000, otvorenaU = 1))
        val p = primalac(noc)
        var zadnja = 0L
        for (i in 1..12) {
            zadnja = dao.dodajTuru(TuraE(nocId = noc, primalacId = p, potvrdjenaU = i.toLong(), kljuc = "k$i"))
            dao.dodajStavke(listOf(stavka(zadnja, i)))
        }
        val ture = dao.posljednjeTure(noc, 10).first()
        assertEquals(10, ture.size)
        assertEquals(zadnja, ture.first().tura.id)
        assertEquals("Vuleta", ture.first().primalac.ime)
        assertEquals(1, ture.first().stavke.size)
    }

    @Test fun stavkePoPrimaocu_bezPonistenih() = runTest {
        val noc = dao.dodajNoc(NocE(datum = 20_000, otvorenaU = 1))
        val p = primalac(noc)
        val t1 = dao.dodajTuru(TuraE(nocId = noc, primalacId = p, potvrdjenaU = 1, kljuc = "a"))
        dao.dodajStavke(listOf(stavka(t1, 4)))
        val t2 = dao.dodajTuru(TuraE(nocId = noc, primalacId = p, potvrdjenaU = 2, kljuc = "b", ponistenaU = 3))
        dao.dodajStavke(listOf(stavka(t2, 7)))
        val s = dao.stavkePoPrimaocu(noc).first()
        assertEquals(listOf(4), s.map { it.kolicina })
        assertEquals(p, s.single().primalacId)
        assertEquals("kom", s.single().uZbiru)
    }
}
