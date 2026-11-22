package dev.nextpixel.cafebar919.repo

import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.NacrtTureE
import dev.nextpixel.cafebar919.data.VrstaPrimaoca.ISPOMOC
import dev.nextpixel.cafebar919.data.VrstaPrimaoca.KONOBAR
import dev.nextpixel.cafebar919.data.VrstaPrimaoca.SANK
import dev.nextpixel.cafebar919.data.VrstaPrimaoca.ZA_SANKOM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class NocRepoTest {
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDate.of(2026, 11, 20).atTime(23, 50).atZone(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var repo: NocRepo
    lateinit var podesavanja: PodesavanjaRepo

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        repo = NocRepo(db, sat)
        podesavanja = PodesavanjaRepo(db, sat)
    }
    @After fun tearDown() = db.close()

    private suspend fun ids(vararg imena: String): List<Long> {
        val svi = db.radnikDao().jednom()
        return imena.map { ime -> svi.first { it.ime == ime }.id }
    }
    private suspend fun brojNoci(): Int = withContext(Dispatchers.IO) { db.query("SELECT COUNT(*) FROM noc", null).use { it.moveToFirst(); it.getInt(0) } }

    @Test fun otvoriNoc_primaociRedomISankNaKraju() = runTest {
        val (vuleta, srdjan, milos) = ids("Vuleta", "Srđan", "Miloš")
        val noc = repo.otvoriNoc(listOf(milos, vuleta, srdjan), listOf("Dejan"))
        val p = repo.primaoci(noc).first()
        assertEquals(listOf("Vuleta", "Srđan", "Miloš", "Dejan", "Šank"), p.map { it.ime })
        assertEquals(listOf(KONOBAR, KONOBAR, ZA_SANKOM, ISPOMOC, SANK), p.map { it.vrsta })
        assertEquals(listOf(0, 1, 2, 3, 4), p.map { it.redoslijed })
        assertEquals(null, p.last().radnikId)
    }
    @Test fun otvoriNoc_bezIkogaImaSamoSank() = runTest {
        assertEquals(listOf("Šank"), repo.primaoci(repo.otvoriNoc(emptyList(), emptyList())).first().map { it.ime })
    }
    @Test fun otvoriNoc_dvaputVracaIstuNoc() = runTest {
        val a = repo.otvoriNoc(emptyList(), emptyList()); val b = repo.otvoriNoc(ids("Vuleta").toList(), emptyList())
        assertEquals(a, b); assertEquals(1, brojNoci())
        assertEquals(1, repo.primaoci(a).first().size)
    }
    @Test fun otvoriNoc_prazniNacrt() = runTest {
        db.nocDao().upisiNacrt(NacrtTureE(1, null, "stari", "[{\"x\":1}]", 5L))
        repo.otvoriNoc(emptyList(), emptyList())
        val n = db.nocDao().nacrt()!!
        assertEquals("[]", n.stavke); assertEquals(null, n.primalacId); assertNotEquals("stari", n.kljuc)
    }
    @Test fun otvoriNoc_preskaceIskljucenogRadnika() = runTest {
        val (smitran, milos) = ids("Smitran", "Miloš")
        podesavanja.postaviAktivanRadnik(smitran, false)
        val p = repo.primaoci(repo.otvoriNoc(listOf(smitran, milos, 9999L), emptyList())).first()
        assertEquals(listOf("Miloš", "Šank"), p.map { it.ime })
    }
    @Test fun otvoriNoc_preskaceLosuIspomoc() = runTest {
        val p = repo.primaoci(repo.otvoriNoc(emptyList(), listOf("  ", "Šank", "Dejan", "dejan", "Era"))).first()
        assertEquals(listOf("Dejan", "Era", "Šank"), p.map { it.ime })
    }
    @Test fun otvoriNoc_ispomocSaImenomRadnikaSePreskace() = runTest {
        val p = repo.primaoci(repo.otvoriNoc(emptyList(), listOf("vuleta"))).first()
        assertEquals(listOf("Šank"), p.map { it.ime })
    }
    @Test fun otvoriNoc_ispomocSaImenomIskljucenogRadnikaJeDozvoljena() = runTest {
        podesavanja.postaviAktivanRadnik(ids("Smitran").single(), false)
        val p = repo.primaoci(repo.otvoriNoc(emptyList(), listOf("Smitran"))).first()
        assertEquals(listOf("Smitran", "Šank"), p.map { it.ime })
        assertEquals(listOf(ISPOMOC, SANK), p.map { it.vrsta })
    }
    @Test fun datumNociOstajePoslijePonoci() = runTest {
        val noc = repo.otvoriNoc(emptyList(), emptyList())
        sat.pomjeri(Duration.ofHours(2))
        val a = repo.aktivna.first()!!
        assertEquals(noc, a.id)
        assertEquals(LocalDate.of(2026, 11, 20).toEpochDay(), a.datum)
        assertFalse(a.otvorenaU == sat.millis())
    }
}
