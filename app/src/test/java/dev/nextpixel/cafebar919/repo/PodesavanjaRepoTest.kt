package dev.nextpixel.cafebar919.repo

import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.NocPrimalacE
import dev.nextpixel.cafebar919.data.StavkaTureE
import dev.nextpixel.cafebar919.data.TuraE
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.data.VrstaPrimaoca
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class PodesavanjaRepoTest {
    private val zona = ZoneId.systemDefault()
    private val danas = LocalDate.of(2026, 11, 21)
    private val sat = Clock.fixed(danas.atStartOfDay(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var repo: PodesavanjaRepo

    @Before fun setUp() {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        repo = PodesavanjaRepo(db, sat)
    }
    @After fun tearDown() = db.close()

    private suspend fun kat(naziv: String): Long {
        assertEquals(Ishod.Ok, repo.sacuvajKategoriju(null, naziv))
        return repo.kategorije.first().first { it.k.naziv == naziv }.k.id
    }
    private suspend fun mjeraId(naziv: String): Long = repo.mjere.first().first { it.m.naziv == naziv }.m.id
    private suspend fun artikalId(naziv: String): Long = repo.artikli.first().first { it.a.naziv == naziv }.a.id

    @Test fun sacuvajArtikal_duplikatBezObziraNaVelicinuIRazmake() = runTest {
        val k = kat("Žestina"); val doza = mjeraId("Doza")
        assertEquals(Ishod.Ok, repo.sacuvajArtikal(null, "Jack Daniel’s", k, listOf(doza)))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), repo.sacuvajArtikal(null, "  jack daniel’s ", k, listOf(doza)))
    }
    @Test fun sacuvajArtikal_jednaDoTriMjere() = runTest {
        val k = kat("Žestina"); val sve = repo.mjere.first().map { it.m.id }
        assertEquals(Ishod.Odbijeno(Odbijanje.BROJ_MJERA), repo.sacuvajArtikal(null, "Viljamovka", k, emptyList()))
        assertEquals(Ishod.Odbijeno(Odbijanje.BROJ_MJERA), repo.sacuvajArtikal(null, "Viljamovka", k, sve.take(4)))
        assertEquals(Ishod.Odbijeno(Odbijanje.BEZ_KATEGORIJE), repo.sacuvajArtikal(null, "Viljamovka", null, sve.take(1)))
    }
    @Test fun iskljucenArtikalNeSmetaIstomNazivu() = runTest {
        val k = kat("Žestina"); val doza = mjeraId("Doza")
        repo.sacuvajArtikal(null, "Smirnoff", k, listOf(doza))
        assertEquals(Ishod.Ok, repo.postaviAktivanArtikal(artikalId("Smirnoff"), false))
        assertEquals(Ishod.Ok, repo.sacuvajArtikal(null, "Smirnoff", k, listOf(doza)))
    }
    @Test fun ukljuciArtikal_odbijaDupliNaziv() = runTest {
        val k = kat("Žestina"); val doza = mjeraId("Doza")
        repo.sacuvajArtikal(null, "Smirnoff", k, listOf(doza))
        val a = artikalId("Smirnoff")
        repo.postaviAktivanArtikal(a, false)
        assertEquals(Ishod.Ok, repo.sacuvajArtikal(null, "Smirnoff", k, listOf(doza)))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), repo.postaviAktivanArtikal(a, true))
    }
    @Test fun ukljuciArtikal_odbijaKadJeMjeraIskljucena() = runTest {
        val k = kat("Žestina"); val bokal = mjeraId("Bokal")
        repo.sacuvajArtikal(null, "Vino", k, listOf(bokal))
        val a = artikalId("Vino")
        repo.postaviAktivanArtikal(a, false)
        assertEquals(Ishod.Ok, repo.postaviAktivnaMjera(bokal, false))
        assertEquals(Ishod.Odbijeno(Odbijanje.MJERA_ISKLJUCENA), repo.postaviAktivanArtikal(a, true))
    }
    @Test fun ukljuciArtikal_odbijaKadJeKategorijaIskljucena() = runTest {
        val k = kat("Vino"); val bokal = mjeraId("Bokal")
        repo.sacuvajArtikal(null, "Crveno", k, listOf(bokal))
        val a = artikalId("Crveno")
        repo.postaviAktivanArtikal(a, false)
        assertEquals(Ishod.Ok, repo.postaviAktivnaKategorija(k, false))
        assertEquals(Ishod.Odbijeno(Odbijanje.KATEGORIJA_ISKLJUCENA), repo.postaviAktivanArtikal(a, true))
    }
    @Test fun ukljuciRadnika_odbijaDupliNaziv() = runTest {
        val stari = repo.radnici.first().first { it.ime == "Vuleta" }.id
        assertEquals(Ishod.Ok, repo.postaviAktivanRadnik(stari, false))
        assertEquals(Ishod.Ok, repo.sacuvajRadnika(null, "Vuleta", Uloga.KONOBAR))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), repo.postaviAktivanRadnik(stari, true))
    }
    @Test fun mjeraUUpotrebi_neMozeSeIskljuciti() = runTest {
        val k = kat("Vino"); val bokal = mjeraId("Bokal")
        repo.sacuvajArtikal(null, "Crveno", k, listOf(bokal))
        assertEquals(Ishod.Odbijeno(Odbijanje.U_UPOTREBI), repo.postaviAktivnaMjera(bokal, false))
        repo.postaviAktivanArtikal(artikalId("Crveno"), false)
        assertEquals(Ishod.Ok, repo.postaviAktivnaMjera(bokal, false))
    }
    @Test fun kategorijaSaAktivnimPicem_neMozeSeIskljuciti() = runTest {
        val k = kat("Vino"); val bokal = mjeraId("Bokal")
        repo.sacuvajArtikal(null, "Crveno", k, listOf(bokal))
        assertEquals(Ishod.Odbijeno(Odbijanje.U_UPOTREBI), repo.postaviAktivnaKategorija(k, false))
        repo.postaviAktivanArtikal(artikalId("Crveno"), false)
        assertEquals(Ishod.Ok, repo.postaviAktivnaKategorija(k, false))
    }
    @Test fun mjera_validacija() = runTest {
        assertEquals(Ishod.Odbijeno(Odbijanje.U_ZBIRU_PRAZNO), repo.sacuvajMjeru(null, "Čaša vina", "Čaša", "", 0.2))
        assertEquals(Ishod.Odbijeno(Odbijanje.LITRE_VAN_OPSEGA), repo.sacuvajMjeru(null, "Čaša vina", "Čaša", "čaša", 0.0))
        assertEquals(Ishod.Odbijeno(Odbijanje.NATPIS_PREDUG), repo.sacuvajMjeru(null, "Čaša vina", "Čaša vina 2", "čaša", 0.2))
    }
    @Test fun redoslijedArtikala_cuvaSe() = runTest {
        val k = kat("Pivo"); val kom = mjeraId("Komad")
        listOf("A", "B", "C").forEach { assertEquals(Ishod.Ok, repo.sacuvajArtikal(null, it, k, listOf(kom))) }
        val a = artikalId("A"); val b = artikalId("B"); val c = artikalId("C")
        repo.redoslijedArtikala(listOf(c, a, b))
        assertEquals(listOf(c, a, b), repo.artikli.first().map { it.a.id })
    }
    @Test fun brojaciMenija_samoAktivni() = runTest {
        val k = kat("Pivo"); val kom = mjeraId("Komad")
        listOf("A", "B", "C").forEach { repo.sacuvajArtikal(null, it, k, listOf(kom)) }
        repo.postaviAktivanArtikal(artikalId("C"), false)
        val b = repo.brojaciMenija.first()
        assertEquals(2, b.pica); assertEquals(5, b.mjera); assertEquals(4, b.stalnihRadnika); assertEquals(1, b.kategorija)
    }
    @Test fun pravilaRada_granice() = runTest {
        assertEquals(Ishod.Odbijeno(Odbijanje.VRIJEDNOST_VAN_OPSEGA), repo.postaviRokBezPina(310))
        assertEquals(Ishod.Odbijeno(Odbijanje.VRIJEDNOST_VAN_OPSEGA), repo.postaviIstoriju(6))
        assertEquals(Ishod.Ok, repo.postaviZakljucavanje(60)); assertEquals(60, repo.postavke.first().zakljucavanjeS)
    }
    @Test fun nociKojeBiNestale_brojiSamoZatvoreneStarije() = runTest {
        val d = db.nocDao()
        d.dodajNoc(NocE(datum = LocalDate.of(2026, 10, 1).toEpochDay(), otvorenaU = 1, zatvorenaU = 2))
        d.dodajNoc(NocE(datum = LocalDate.of(2026, 11, 20).toEpochDay(), otvorenaU = 1, zatvorenaU = 2))
        d.dodajNoc(NocE(datum = LocalDate.of(2026, 10, 1).toEpochDay(), otvorenaU = 1))
        assertEquals(1, repo.nociKojeBiNestale(30))
    }
    @Test fun razlog_dodajIUkloni() = runTest {
        assertEquals(Ishod.Ok, repo.dodajRazlog("Vraćeno"))
        assertEquals(Ishod.Odbijeno(Odbijanje.NAZIV_POSTOJI), repo.dodajRazlog(" vraćeno"))
        val id = repo.razlozi.first().first { it.tekst == "Vraćeno" }.id
        repo.ukloniRazlog(id)
        assertFalse(repo.razlozi.first().any { it.tekst == "Vraćeno" })
    }
    @Test fun unosiUAktivnojNoci_saberePoMjeri() = runTest {
        val k = kat("Vino"); val bokal = mjeraId("Bokal")
        repo.sacuvajArtikal(null, "Crveno", k, listOf(bokal))
        val a = repo.artikli.first().first { it.a.naziv == "Crveno" }
        assertTrue(repo.unosiUAktivnojNoci(a.a.id).isEmpty())
        val d = db.nocDao()
        val noc = d.dodajNoc(NocE(datum = danas.toEpochDay(), otvorenaU = 1))
        val p = d.dodajPrimaoce(listOf(NocPrimalacE(nocId = noc, vrsta = VrstaPrimaoca.KONOBAR, radnikId = null, ime = "Vuleta", redoslijed = 0))).single()
        val m = repo.mjere.first().first { it.m.id == bokal }.m
        suspend fun tura(kljuc: String, kol: Int, ponistena: Long?) {
            val t = d.dodajTuru(TuraE(nocId = noc, primalacId = p, potvrdjenaU = 2, kljuc = kljuc, ponistenaU = ponistena))
            d.dodajStavke(listOf(StavkaTureE(turaId = t, artikalId = a.a.id, artikalNaziv = "Crveno", artikalRedoslijed = a.a.redoslijed, kategorijaId = k, kategorijaNaziv = "Vino", kategorijaRedoslijed = 0, mjeraId = m.id, mjeraNaziv = m.naziv, mjeraNatpis = m.natpis, mjeraUZbiru = m.uZbiru, mjeraRedoslijed = m.redoslijed, litara = m.litara, kolicina = kol)))
        }
        tura("t1", 2, null); tura("t2", 1, null); tura("t3", 5, 3)
        assertEquals(listOf(Format.MjeraBroj("bok.", m.redoslijed, 3)), repo.unosiUAktivnojNoci(a.a.id))
    }
}
