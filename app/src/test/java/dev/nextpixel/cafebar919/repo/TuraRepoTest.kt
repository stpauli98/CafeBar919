package dev.nextpixel.cafebar919.repo

import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.NacrtTureE
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Nacrt
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.StavkaNacrta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class TuraRepoTest {
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDate.of(2026, 11, 21).atTime(23, 10).atZone(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var repo: TuraRepo
    lateinit var podesavanja: PodesavanjaRepo

    private var noc = 0L
    private var vuleta = 0L
    private var srdjan = 0L
    private var pivo = 0L
    private var zestina = 0L
    private var jelen = 0L
    private var jack = 0L
    private var komad = 0L
    private var doza = 0L
    private lateinit var kljuc: String
    private lateinit var stavke: List<StavkaNacrta>

    @Before fun setUp() = runTest {
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        repo = TuraRepo(db, sat)
        podesavanja = PodesavanjaRepo(db, sat)

        podesavanja.sacuvajKategoriju(null, "Pivo")
        podesavanja.sacuvajKategoriju(null, "Žestina")
        val kat = podesavanja.kategorije.first()
        pivo = kat.first { it.k.naziv == "Pivo" }.k.id
        zestina = kat.first { it.k.naziv == "Žestina" }.k.id
        val mj = podesavanja.mjere.first()
        komad = mj.first { it.m.naziv == "Komad" }.m.id
        doza = mj.first { it.m.naziv == "Doza" }.m.id
        assertEquals(Ishod.Ok, podesavanja.sacuvajArtikal(null, "Jelen pivo", pivo, listOf(komad)))
        assertEquals(Ishod.Ok, podesavanja.sacuvajArtikal(null, "Jack Daniel’s", zestina, listOf(doza)))
        val art = podesavanja.artikli.first()
        jelen = art.first { it.a.naziv == "Jelen pivo" }.a.id
        jack = art.first { it.a.naziv == "Jack Daniel’s" }.a.id

        val radnici = db.radnikDao().jednom()
        noc = NocRepo(db, sat).otvoriNoc(radnici.filter { it.ime in setOf("Vuleta", "Srđan") }.map { it.id }, emptyList())
        val primaoci = db.nocDao().primaociJednom(noc)
        vuleta = primaoci.first { it.ime == "Vuleta" }.id
        srdjan = primaoci.first { it.ime == "Srđan" }.id

        kljuc = repo.nacrt().kljuc
        stavke = listOf(StavkaNacrta(jelen, komad, 4), StavkaNacrta(jack, doza, 2))
    }
    @After fun tearDown() = db.close()

    private fun sql(s: String) = db.openHelper.writableDatabase.execSQL(s)
    private suspend fun brojTura(): Int = broj("SELECT COUNT(*) FROM tura")
    private suspend fun broj(upit: String): Int = withContext(Dispatchers.IO) {
        db.query(upit, null).use { it.moveToFirst(); it.getInt(0) }
    }

    // ---- potvrda ----

    @Test fun potvrdi_upisujeSnimakIPrazniNacrt() = runTest {
        val r = repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jack, doza, 2), StavkaNacrta(jelen, komad, 4)))
        r as Potvrda.Upisana
        assertEquals("Vuleta", r.ime); assertEquals(2, r.brojStavki)

        val t = db.nocDao().tura(r.turaId)!!
        assertEquals(noc, t.nocId); assertEquals(vuleta, t.primalacId); assertEquals(kljuc, t.kljuc)
        assertEquals(sat.millis(), t.potvrdjenaU); assertNull(t.ponistenaU); assertFalse(t.ponistenaBezPina)

        val st = repo.posljednje(noc).first().single().stavke
        assertEquals(listOf("Jack Daniel’s", "Jelen pivo"), st.map { it.artikalNaziv })
        val j = st[0]
        assertEquals(jack, j.artikalId); assertEquals(1, j.artikalRedoslijed)
        assertEquals(zestina, j.kategorijaId); assertEquals("Žestina", j.kategorijaNaziv); assertEquals(1, j.kategorijaRedoslijed)
        assertEquals(doza, j.mjeraId); assertEquals("Doza", j.mjeraNaziv); assertEquals("0,03", j.mjeraNatpis)
        assertEquals("doza", j.mjeraUZbiru); assertEquals(1, j.mjeraRedoslijed); assertEquals(0.03, j.litara!!, 0.0); assertEquals(2, j.kolicina)
        val p = st[1]
        assertEquals("Pivo", p.kategorijaNaziv); assertEquals("kom", p.mjeraUZbiru); assertNull(p.litara); assertEquals(4, p.kolicina)

        val n = repo.nacrt()
        assertEquals("[]", n.stavke); assertNull(n.primalacId); assertNotEquals(kljuc, n.kljuc); assertEquals(r.noviKljuc, n.kljuc)
    }

    @Test fun potvrdi_istiKljucDvaputJednaTura() = runTest {
        val prva = repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana
        val drugi = repo.potvrdi(kljuc, vuleta, stavke)
        assertTrue(drugi is Potvrda.Upisana); assertEquals(1, brojTura())
        drugi as Potvrda.Upisana
        assertEquals(prva.turaId, drugi.turaId); assertEquals("Vuleta", drugi.ime); assertEquals(2, drugi.brojStavki)
        assertEquals(repo.nacrt().kljuc, drugi.noviKljuc); assertEquals(prva.noviKljuc, drugi.noviKljuc)
        assertEquals(2, broj("SELECT COUNT(*) FROM stavka_ture"))
    }

    @Test fun potvrdi_poslijeGreskeIstiKljucJednaTura() = runTest {
        sql("CREATE TRIGGER kvar BEFORE INSERT ON tura BEGIN SELECT RAISE(ABORT, 'kvar'); END")
        repo.sacuvajNacrt(kljuc, vuleta, stavke)
        assertEquals(Potvrda.NijeUpisana, repo.potvrdi(kljuc, vuleta, stavke))
        assertEquals(0, brojTura()); assertEquals(Nacrt.uJson(stavke), repo.nacrt().stavke)   // nacrt ostao
        assertEquals(kljuc, repo.nacrt().kljuc); assertEquals(vuleta, repo.nacrt().primalacId)
        sql("DROP TRIGGER kvar")
        assertTrue(repo.potvrdi(kljuc, vuleta, stavke) is Potvrda.Upisana); assertEquals(1, brojTura())
    }

    @Test fun potvrdi_greskaNaStavkamaVracaICijeluTuru() = runTest {
        // greška koja nije ograničenje (nepostojeća tabela), poslije upisa ture
        sql("CREATE TRIGGER kvar BEFORE INSERT ON stavka_ture BEGIN INSERT INTO nema_tabele VALUES (1); END")
        repo.sacuvajNacrt(kljuc, vuleta, stavke)
        assertEquals(Potvrda.NijeUpisana, repo.potvrdi(kljuc, vuleta, stavke))
        assertEquals(0, brojTura()); assertEquals(0, broj("SELECT COUNT(*) FROM stavka_ture"))
        assertEquals(kljuc, repo.nacrt().kljuc); assertEquals(Nacrt.uJson(stavke), repo.nacrt().stavke)
        sql("DROP TRIGGER kvar")
        assertEquals(2, (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).brojStavki)
    }

    @Test fun potvrdi_snimakOstajePoslijeIzmjeneArtikla() = runTest {
        repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jack, doza, 1)))
        podesavanja.sacuvajArtikal(jack, "Jack Daniel’s No. 7", zestina, listOf(doza))
        podesavanja.sacuvajMjeru(doza, "Doza", "0,05", "dupla", 0.05)
        val s = repo.posljednje(noc).first().single().stavke.single()
        assertEquals("Jack Daniel’s", s.artikalNaziv); assertEquals("doza", s.mjeraUZbiru); assertEquals(0.03, s.litara!!, 0.0)
    }

    @Test fun potvrdi_preskaceIskljucenArtikal() = runTest {
        podesavanja.postaviAktivanArtikal(jelen, false)
        val r = repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jelen, komad, 2), StavkaNacrta(jack, doza, 1))) as Potvrda.Upisana
        assertEquals(1, r.brojStavki)
        assertEquals(listOf(jack), repo.posljednje(noc).first().single().stavke.map { it.artikalId })
    }

    @Test fun potvrdi_preskaceMjeruKojaViseNijeUzPice() = runTest {
        val r = repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jelen, doza, 2), StavkaNacrta(jack, doza, 1))) as Potvrda.Upisana
        assertEquals(1, r.brojStavki)
    }

    @Test fun potvrdi_odbijanja() = runTest {
        assertEquals(Potvrda.Odbijena(Odbijanje.BEZ_PRIMAOCA), repo.potvrdi(kljuc, null, stavke))
        assertEquals(Potvrda.Odbijena(Odbijanje.BEZ_PRIMAOCA), repo.potvrdi(kljuc, 9999L, stavke))
        assertEquals(Potvrda.Odbijena(Odbijanje.PRAZNA_TURA), repo.potvrdi(kljuc, vuleta, emptyList()))
        podesavanja.postaviAktivanArtikal(jelen, false)
        assertEquals(Potvrda.Odbijena(Odbijanje.PRAZNA_TURA), repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jelen, komad, 3))))
        assertEquals(0, brojTura())
        assertEquals(kljuc, repo.nacrt().kljuc)
    }

    @Test fun potvrdi_bezAktivneNoci() = runTest {
        sql("UPDATE noc SET zatvorena_u = ${sat.millis()}")
        assertEquals(Potvrda.Odbijena(Odbijanje.NEMA_NOCI), repo.potvrdi(kljuc, vuleta, stavke))
        assertEquals(0, brojTura())
    }

    @Test fun potvrdi_nacrtSaVecUpisanimKljucemDobijaNovi() = runTest {
        repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana
        // nacrt koji je mimo repozitorija ostao na upisanom ključu ne smije progutati sljedeću turu
        db.nocDao().upisiNacrt(NacrtTureE(1, vuleta, kljuc, Nacrt.uJson(stavke), sat.millis()))
        val ponovo = repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana
        assertNotEquals(kljuc, ponovo.noviKljuc)
        val n = repo.nacrt()
        assertEquals(ponovo.noviKljuc, n.kljuc); assertEquals("[]", n.stavke); assertNull(n.primalacId)
        assertEquals(1, brojTura())
    }

    // ---- nacrt ----

    @Test fun nacrt_praviPrazanKadGaNema() = runTest {
        sql("DELETE FROM nacrt_ture")
        val n = repo.nacrt()
        assertEquals(1, n.id); assertEquals("[]", n.stavke); assertNull(n.primalacId); assertEquals(sat.millis(), n.izmijenjenU)
        assertEquals(n.kljuc, repo.nacrt().kljuc)
    }

    @Test fun sacuvajNacrt_preziviICitaSe() = runTest {
        sat.pomjeri(Duration.ofSeconds(5))
        repo.sacuvajNacrt(kljuc, srdjan, stavke)
        val n = repo.nacrt()
        assertEquals(kljuc, n.kljuc); assertEquals(srdjan, n.primalacId); assertEquals(stavke, Nacrt.izJsona(n.stavke))
        assertEquals(sat.millis(), n.izmijenjenU)
    }

    @Test fun sacuvajNacrt_zakasnioUpisPoslijePotvrdeSeIgnorise() = runTest {
        val r = repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana
        repo.sacuvajNacrt(kljuc, vuleta, stavke)
        val n = repo.nacrt()
        assertEquals(r.noviKljuc, n.kljuc); assertEquals("[]", n.stavke); assertNull(n.primalacId)
    }

    @Test fun vazeceStavke_samoParoviIzKataloga() = runTest {
        val k = repo.katalog.first()
        val ulaz = listOf(StavkaNacrta(jelen, komad, 2), StavkaNacrta(jelen, doza, 1), StavkaNacrta(9999L, komad, 1), StavkaNacrta(jack, doza, 3))
        assertEquals(listOf(StavkaNacrta(jelen, komad, 2), StavkaNacrta(jack, doza, 3)), TuraRepo.vazeceStavke(ulaz, k))
        assertEquals(emptyList<StavkaNacrta>(), TuraRepo.vazeceStavke(ulaz, emptyList()))
    }

    // ---- poništavanje ----

    @Test fun ponistiBezPina_uRokuIPoslije() = runTest {
        val t = (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
        sat.pomjeri(Duration.ofSeconds(59)); assertEquals(Ishod.Ok, repo.ponistiBezPina(t))
        val pon = db.nocDao().tura(t)!!
        assertEquals(sat.millis(), pon.ponistenaU); assertTrue(pon.ponistenaBezPina); assertNull(pon.razlogTekst)
        assertEquals(Ishod.Odbijeno(Odbijanje.VEC_PONISTENA), repo.ponistiBezPina(t))
        val t2 = (repo.potvrdi(repo.nacrt().kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
        sat.pomjeri(Duration.ofSeconds(60)); assertEquals(Ishod.Odbijeno(Odbijanje.ROK_ISTEKAO), repo.ponistiBezPina(t2))
        assertNull(db.nocDao().tura(t2)!!.ponistenaU)
    }

    @Test fun ponistiBezPina_satUnazadOdbija() = runTest {
        val t = (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
        sat.pomjeri(Duration.ofMinutes(-10)); assertEquals(Ishod.Odbijeno(Odbijanje.ROK_ISTEKAO), repo.ponistiBezPina(t))
    }

    @Test fun ponistiBezPina_rokIzPostavki() = runTest {
        assertEquals(Ishod.Ok, podesavanja.postaviRokBezPina(0))
        assertEquals(0, repo.rokBezPinaS()); assertEquals(0, repo.rokBezPina.first())
        val t = (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
        assertEquals(Ishod.Odbijeno(Odbijanje.ROK_ISTEKAO), repo.ponistiBezPina(t))
        assertEquals(Ishod.Ok, podesavanja.postaviRokBezPina(120))
        sat.pomjeri(Duration.ofSeconds(90))
        assertEquals(120, repo.rokBezPinaS()); assertEquals(Ishod.Ok, repo.ponistiBezPina(t))
    }

    @Test fun ponistiBezPina_nemaTureIliNociOdbija() = runTest {
        assertEquals(Ishod.Odbijeno(Odbijanje.NEMA_NOCI), repo.ponistiBezPina(9999L))
        val t = (repo.potvrdi(kljuc, vuleta, stavke) as Potvrda.Upisana).turaId
        sql("UPDATE noc SET zatvorena_u = ${sat.millis()}")
        assertEquals(Ishod.Odbijeno(Odbijanje.NEMA_NOCI), repo.ponistiBezPina(t))
        assertNull(db.nocDao().tura(t)!!.ponistenaU)
    }

    // ---- prikazi ----

    @Test fun zbiroviPoPrimaocu_bezPonistenih() = runTest {
        repo.potvrdi(kljuc, vuleta, listOf(StavkaNacrta(jelen, komad, 4), StavkaNacrta(jack, doza, 2)))
        val t = (repo.potvrdi(repo.nacrt().kljuc, vuleta, listOf(StavkaNacrta(jack, doza, 1))) as Potvrda.Upisana).turaId
        assertEquals(mapOf(vuleta to "kom 4 · doza 3"), repo.zbiroviPoPrimaocu(noc).first())
        assertEquals(Ishod.Ok, repo.ponistiBezPina(t))
        val z = repo.zbiroviPoPrimaocu(noc).first()
        assertEquals(mapOf(vuleta to "kom 4 · doza 2"), z)
        assertFalse(srdjan in z)
    }

    @Test fun posljednje_desetNajnovijihStavkeRedomUnosa() = runTest {
        repeat(11) { i -> repo.potvrdi(repo.nacrt().kljuc, if (i % 2 == 0) vuleta else srdjan, stavke) }
        val p = repo.posljednje(noc).first()
        assertEquals(10, p.size)
        assertEquals(p.map { it.tura.id }.sortedDescending(), p.map { it.tura.id })
        assertEquals("Vuleta", p.first().primalac.ime)
        p.forEach { assertEquals(it.stavke.map { s -> s.id }.sorted(), it.stavke.map { s -> s.id }) }
        assertEquals(listOf("Jelen pivo", "Jack Daniel’s"), p.first().stavke.map { it.artikalNaziv })
    }

    @Test fun katalog_samoAktivnoSaAktivnomKategorijom() = runTest {
        assertEquals(listOf("Jelen pivo", "Jack Daniel’s"), repo.katalog.first().map { it.a.naziv })
        assertEquals(listOf("Komad"), repo.katalog.first().first().mjere.map { it.naziv })

        // isključena kategorija skriva njena pića (stanje do kog podešavanja ne dovode, ali katalog ga mora podnijeti)
        sql("UPDATE kategorija SET aktivna = 0 WHERE id = $pivo")
        assertEquals(listOf("Jack Daniel’s"), repo.katalog.first().map { it.a.naziv })
        sql("UPDATE kategorija SET aktivna = 1 WHERE id = $pivo")

        podesavanja.postaviAktivanArtikal(jelen, false)
        assertEquals(listOf("Jack Daniel’s"), repo.katalog.first().map { it.a.naziv })
    }

    @Test fun katalog_aktivneMjereRedomIzArtikla_bezMjereIzostavljeno() = runTest {
        val bokal = podesavanja.mjere.first().first { it.m.naziv == "Bokal" }.m.id
        assertEquals(Ishod.Ok, podesavanja.sacuvajArtikal(jack, "Jack Daniel’s", zestina, listOf(bokal, doza)))
        assertEquals(listOf("Bokal", "Doza"), repo.katalog.first().first { it.a.id == jack }.mjere.map { it.naziv })

        sql("UPDATE mjera SET aktivna = 0 WHERE id = $bokal")
        assertEquals(listOf("Doza"), repo.katalog.first().first { it.a.id == jack }.mjere.map { it.naziv })
        sql("UPDATE mjera SET aktivna = 0 WHERE id = $komad")
        assertEquals(listOf("Jack Daniel’s"), repo.katalog.first().map { it.a.naziv })

        podesavanja.redoslijedArtikala(listOf(jack, jelen))
        sql("UPDATE mjera SET aktivna = 1 WHERE id = $komad")
        assertEquals(listOf("Jack Daniel’s", "Jelen pivo"), repo.katalog.first().map { it.a.naziv })
    }
}
