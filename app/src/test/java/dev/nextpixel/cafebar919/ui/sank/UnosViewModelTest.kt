package dev.nextpixel.cafebar919.ui.sank

import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import dev.nextpixel.cafebar919.MutableClock
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Nacrt
import dev.nextpixel.cafebar919.domain.StavkaNacrta
import dev.nextpixel.cafebar919.repo.NocRepo
import dev.nextpixel.cafebar919.repo.PodesavanjaRepo
import dev.nextpixel.cafebar919.repo.TuraRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import java.time.LocalDate
import java.time.ZoneId

/**
 * ViewModel S1 nad pravom bazom u memoriji. `Dispatchers.Main` je `StandardTestDispatcher`, pa korutine ViewModel-a
 * čekaju u redu dok test ne pusti raspoređivač; rad baze je na nitima Room-a, a test ga čeka kroz `stanje.first { … }`
 * ili kroz upit u bazu (bez spavanja).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UnosViewModelTest {
    private val zona = ZoneId.systemDefault()
    private val sat = MutableClock(LocalDate.of(2026, 11, 21).atTime(23, 10).atZone(zona).toInstant(), zona)
    lateinit var db: AppDatabase
    lateinit var ture: TuraRepo
    lateinit var nocRepo: NocRepo
    lateinit var podesavanja: PodesavanjaRepo
    private val vms = mutableListOf<UnosViewModel>()

    private var nocId = 0L
    private var vuleta = 0L
    private var srdjan = 0L
    private var pivo = 0L
    private var zestina = 0L
    private var jelen = 0L
    private var jack = 0L
    private var komad = 0L
    private var doza = 0L
    private lateinit var kljuc0: String

    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        ShadowLog.clear()
        db = AppDatabase.napravi(ApplicationProvider.getApplicationContext(), null)
        ture = TuraRepo(db, sat)
        nocRepo = NocRepo(db, sat)
        podesavanja = PodesavanjaRepo(db, sat)
        runBlocking {
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
            nocId = nocRepo.otvoriNoc(radnici.filter { it.ime in setOf("Vuleta", "Srđan") }.map { it.id }, emptyList())
            val primaoci = db.nocDao().primaociJednom(nocId)
            vuleta = primaoci.first { it.ime == "Vuleta" }.id
            srdjan = primaoci.first { it.ime == "Srđan" }.id
            kljuc0 = ture.nacrt().kljuc
        }
    }

    @After fun tearDown() {
        vms.forEach { it.viewModelScope.cancel() }
        Dispatchers.resetMain()
        db.close()
    }

    private fun noviVm() = UnosViewModel(ture, nocRepo, sat).also { vms += it }

    private suspend fun UnosViewModel.cekaj(uslov: (UnosStanje) -> Boolean = { true }): UnosStanje =
        stanje.filterNotNull().first(uslov)

    private fun TestScope.skupljaj(vm: UnosViewModel): List<UnosDogadjaj> {
        val l = mutableListOf<UnosDogadjaj>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.dogadjaji.toList(l) }
        return l
    }

    private suspend fun broj(upit: String): Int = withContext(Dispatchers.IO) {
        db.query(upit, null).use { it.moveToFirst(); it.getInt(0) }
    }

    private suspend fun brojTura(): Int = broj("SELECT COUNT(*) FROM tura")

    private fun sql(s: String) = db.openHelper.writableDatabase.execSQL(s)

    /** Čeka uslov koji zavisi od niti baze: svaki krug pusti raspoređivač i prođe jednim upitom kroz bazu. */
    private suspend fun TestScope.cekajUslov(uslov: suspend () -> Boolean) {
        repeat(200) {
            advanceUntilIdle()
            if (uslov()) return
            brojTura()
        }
        fail("Uslov nije ispunjen")
    }

    private fun greskeUDnevniku(): Int = ShadowLog.getLogsForTag("CafeBar919").count { it.type == android.util.Log.ERROR }

    // ---- potvrda ----

    @Test fun potvrda_prazniTuruIOdznacavaPrimaoca() = runTest {
        val vm = noviVm()
        val dogadjaji = skupljaj(vm)
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        vm.dodaj(jack, doza)
        val prije = vm.cekaj { it.primalacId == vuleta && it.stavke == listOf(StavkaNacrta(jack, doza, 2)) }
        assertFalse(prije.upisuje)

        vm.potvrdi()
        assertTrue(vm.cekaj { it.upisuje }.upisuje)
        val s = vm.cekaj { !it.upisuje }
        assertEquals(emptyList<StavkaNacrta>(), s.stavke)
        assertNull(s.primalacId)
        assertFalse(s.greskaUpisa)
        val d = dogadjaji.single() as UnosDogadjaj.Upisana
        assertEquals("Vuleta", d.ime)
        assertEquals(1, d.brojStavki)
        assertEquals(60, d.rokS)
        assertEquals(1, brojTura())
        assertEquals(d.turaId, vm.cekaj { it.posljednje.isNotEmpty() }.posljednje.single().tura.id)
    }

    @Test fun potvrdi_dvaDodiraJednaTura() = runTest {
        val vm = noviVm()
        val dogadjaji = skupljaj(vm)
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jelen, komad)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == vuleta }

        vm.potvrdi()
        vm.potvrdi()
        vm.cekaj { it.stavke.isEmpty() && !it.upisuje }
        advanceUntilIdle()
        assertEquals(1, brojTura())
        assertEquals(1, dogadjaji.size)
    }

    @Test fun potvrdi_izmjenaDokUpisujeSeIgnorise() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == vuleta }

        vm.potvrdi()
        vm.dodaj(jelen, komad)
        vm.promijeni(jack, doza, 1)
        vm.izaberiPrimaoca(srdjan)
        vm.cekaj { it.stavke.isEmpty() && !it.upisuje }
        assertEquals(1, broj("SELECT COUNT(*) FROM stavka_ture"))
        assertEquals(1, broj("SELECT kolicina FROM stavka_ture"))
        assertEquals(vuleta.toInt(), broj("SELECT primalac_id FROM tura"))
    }

    @Test fun upis01_turaOstajeIPonovoUpisujeJednom() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == vuleta }

        sql("CREATE TRIGGER kvar BEFORE INSERT ON tura BEGIN SELECT RAISE(ABORT, 'kvar'); END")
        vm.potvrdi()
        val greska = vm.cekaj { it.greskaUpisa && !it.upisuje }
        assertEquals(listOf(StavkaNacrta(jack, doza, 2)), greska.stavke)
        assertEquals(vuleta, greska.primalacId)
        assertEquals(0, brojTura())

        sql("DROP TRIGGER kvar")
        vm.potvrdi()
        val s = vm.cekaj { !it.greskaUpisa && !it.upisuje && it.stavke.isEmpty() }
        assertNull(s.primalacId)
        assertEquals(1, brojTura())
        assertEquals(kljuc0, db.nocDao().turaPoKljucu(kljuc0)?.kljuc)
    }

    @Test fun dvijeTureZaRedom_svakaSaSvojimStavkama() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == vuleta }
        vm.potvrdi()
        vm.cekaj { it.stavke.isEmpty() && !it.upisuje }

        vm.izaberiPrimaoca(srdjan)
        vm.dodaj(jelen, komad)
        vm.dodaj(jelen, komad)
        vm.dodaj(jelen, komad)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == srdjan }
        vm.potvrdi()
        vm.cekaj { it.stavke.isEmpty() && !it.upisuje }

        assertEquals(2, brojTura())
        val l = vm.cekaj { it.posljednje.size == 2 }.posljednje
        val zaSrdjana = l.first { it.tura.primalacId == srdjan }
        val zaVuletu = l.first { it.tura.primalacId == vuleta }
        assertEquals(listOf("Jelen pivo" to 3), zaSrdjana.stavke.map { it.artikalNaziv to it.kolicina })
        assertEquals(listOf("Jack Daniel’s" to 1), zaVuletu.stavke.map { it.artikalNaziv to it.kolicina })
        // Nacrt u bazi je prazan i na novom ključu.
        val n = ture.nacrt()
        assertEquals("[]", n.stavke)
        assertNull(db.nocDao().turaPoKljucu(n.kljuc))
    }

    @Test fun potvrdiBezPrimaoca_odbijenaBezPromjene() = runTest {
        val vm = noviVm()
        val dogadjaji = skupljaj(vm)
        vm.cekaj()
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.isNotEmpty() }
        vm.potvrdi()
        vm.cekaj { it.upisuje }
        val s = vm.cekaj { !it.upisuje }
        assertEquals(listOf(StavkaNacrta(jack, doza, 1)), s.stavke)
        assertFalse(s.greskaUpisa)
        assertEquals(0, brojTura())
        assertTrue(dogadjaji.isEmpty())
    }

    // ---- nacrt ----

    @Test fun nacrt_preziviNoviViewModel() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiPrimaoca(srdjan)
        repeat(3) { vm.dodaj(jelen, komad) }
        vm.cekaj { it.stavke == listOf(StavkaNacrta(jelen, komad, 3)) && it.primalacId == srdjan }
        cekajUslov { ture.nacrt().let { it.primalacId == srdjan && Nacrt.izJsona(it.stavke) == listOf(StavkaNacrta(jelen, komad, 3)) } }

        vm.viewModelScope.cancel()
        val drugi = noviVm()
        val s = drugi.cekaj()
        assertEquals(srdjan, s.primalacId)
        assertEquals(listOf(StavkaNacrta(jelen, komad, 3)), s.stavke)
        assertEquals(kljuc0, ture.nacrt().kljuc)
    }

    @Test fun nacrt_primalacDrugeNociPostajeNull() = runTest {
        // Nacrt pokazuje na primaoca zatvorene noći; nova noć ga ne poznaje.
        sql("UPDATE noc SET zatvorena_u = ${sat.millis()} WHERE id = $nocId")
        nocRepo.otvoriNoc(emptyList(), emptyList())
        ture.sacuvajNacrt(ture.nacrt().kljuc, vuleta, listOf(StavkaNacrta(jack, doza, 1)))

        val vm = noviVm()
        val s = vm.cekaj()
        assertNull(s.primalacId)
        assertEquals(listOf("Šank"), s.primaoci.map { it.ime })
        assertEquals(listOf(StavkaNacrta(jack, doza, 1)), s.stavke)
    }

    @Test fun nacrt_iskljucenoPiceNestaje() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jelen, komad)
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.size == 2 }

        podesavanja.postaviAktivanArtikal(jelen, false)
        val s = vm.cekaj { it.stavke.size == 1 }
        assertEquals(listOf(StavkaNacrta(jack, doza, 1)), s.stavke)
        assertEquals(listOf("Jack Daniel’s"), s.pica.map { it.a.naziv })

        vm.potvrdi()
        vm.cekaj { it.stavke.isEmpty() && !it.upisuje }
        assertEquals(1, broj("SELECT COUNT(*) FROM stavka_ture"))
    }

    @Test fun nacrt_greskaUpisaNeRusiIStanjeOstaje() = runTest {
        val vm = noviVm()
        vm.cekaj()
        sql("CREATE TRIGGER kvar_nacrt BEFORE INSERT ON nacrt_ture BEGIN SELECT RAISE(ABORT, 'kvar'); END")
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        cekajUslov { greskeUDnevniku() >= 2 }
        val s = vm.cekaj()
        assertEquals(vuleta, s.primalacId)
        assertEquals(listOf(StavkaNacrta(jack, doza, 1)), s.stavke)
        assertEquals("[]", ture.nacrt().stavke)

        sql("DROP TRIGGER kvar_nacrt")
        vm.dodaj(jelen, komad)
        cekajUslov { Nacrt.izJsona(ture.nacrt().stavke).size == 2 }
        val n = ture.nacrt()
        assertEquals(vuleta, n.primalacId)
        assertEquals(listOf(StavkaNacrta(jack, doza, 1), StavkaNacrta(jelen, komad, 1)), Nacrt.izJsona(n.stavke))
        assertEquals(kljuc0, n.kljuc)
    }

    // ---- filteri ----

    @Test fun pretraga_biraSveIFiltrira() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiKategoriju(pivo)
        val p = vm.cekaj { it.izabranaKategorija == pivo }
        assertEquals(listOf("Jelen pivo"), p.pica.map { it.a.naziv })

        vm.upit("jack")
        val s = vm.cekaj { it.upit == "jack" }
        assertNull(s.izabranaKategorija)
        assertEquals(listOf("Jack Daniel’s"), s.pica.map { it.a.naziv })
        assertTrue(s.imaPica)
    }

    @Test fun kategorije_samoSaAktivnimPicem() = runTest {
        podesavanja.sacuvajKategoriju(null, "Vino")
        val vm = noviVm()
        val s = vm.cekaj()
        assertEquals(listOf("Pivo", "Žestina"), s.kategorije.map { it.naziv })
        assertEquals(listOf("Jelen pivo", "Jack Daniel’s"), s.pica.map { it.a.naziv })
    }

    @Test fun izabranaKategorijaNestaneVracaSve() = runTest {
        assertEquals(Ishod.Ok, podesavanja.sacuvajArtikal(null, "Nikšićko pivo", pivo, listOf(komad)))
        val niksicko = podesavanja.artikli.first().first { it.a.naziv == "Nikšićko pivo" }.a.id
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiKategoriju(pivo)
        assertEquals(2, vm.cekaj { it.izabranaKategorija == pivo }.pica.size)

        podesavanja.postaviAktivanArtikal(jelen, false)
        podesavanja.postaviAktivanArtikal(niksicko, false)
        val s = vm.cekaj { it.kategorije.none { k -> k.id == pivo } }
        assertNull(s.izabranaKategorija)
        assertEquals(listOf("Jack Daniel’s"), s.pica.map { it.a.naziv })
    }

    @Test fun bezPica_imaPicaNetacno() = runTest {
        podesavanja.postaviAktivanArtikal(jelen, false)
        podesavanja.postaviAktivanArtikal(jack, false)
        val vm = noviVm()
        val s = vm.cekaj()
        assertFalse(s.imaPica)
        assertTrue(s.pica.isEmpty())
        assertTrue(s.kategorije.isEmpty())
    }

    @Test fun izaberiIstogPrimaocaOdznacava() = runTest {
        val vm = noviVm()
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        assertEquals(vuleta, vm.cekaj { it.primalacId != null }.primalacId)
        vm.izaberiPrimaoca(srdjan)
        assertEquals(srdjan, vm.cekaj { it.primalacId == srdjan }.primalacId)
        vm.izaberiPrimaoca(srdjan)
        assertNull(vm.cekaj { it.primalacId == null }.primalacId)
    }

    // ---- poništavanje ----

    @Test fun ponistiURoku_turaPrecrtanaIVanZbira() = runTest {
        val vm = noviVm()
        val dogadjaji = skupljaj(vm)
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == vuleta }
        vm.potvrdi()
        val prije = vm.cekaj { it.posljednje.size == 1 && it.zbirovi.containsKey(vuleta) }
        assertFalse(prije.posljednje.first().tura.ponistenaBezPina)
        val turaId = (dogadjaji.single() as UnosDogadjaj.Upisana).turaId

        vm.ponisti(turaId)
        val s = vm.cekaj { it.posljednje.firstOrNull()?.tura?.ponistenaBezPina == true }
        assertEquals(turaId, s.posljednje.first().tura.id)
        assertFalse(vm.cekaj { !it.zbirovi.containsKey(vuleta) }.zbirovi.containsKey(vuleta))
    }

    @Test fun ponisti_greskaBazeNeRusi() = runTest {
        val vm = noviVm()
        val dogadjaji = skupljaj(vm)
        vm.cekaj()
        vm.izaberiPrimaoca(vuleta)
        vm.dodaj(jack, doza)
        vm.cekaj { it.stavke.isNotEmpty() && it.primalacId == vuleta }
        vm.potvrdi()
        vm.cekaj { it.posljednje.size == 1 }
        val turaId = (dogadjaji.single() as UnosDogadjaj.Upisana).turaId

        sql("CREATE TRIGGER kvar BEFORE UPDATE ON tura BEGIN SELECT RAISE(ABORT, 'kvar'); END")
        val greske = greskeUDnevniku()
        vm.ponisti(turaId)
        cekajUslov { greskeUDnevniku() > greske }
        assertNull(db.nocDao().tura(turaId)?.ponistenaU)
        assertFalse(vm.cekaj().posljednje.single().tura.ponistenaBezPina)
    }
}
