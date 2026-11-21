package dev.nextpixel.cafebar919.repo

import androidx.room.withTransaction
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.ArtikalE
import dev.nextpixel.cafebar919.data.ArtikalMjeraE
import dev.nextpixel.cafebar919.data.KategorijaE
import dev.nextpixel.cafebar919.data.MjeraE
import dev.nextpixel.cafebar919.data.PostavkeE
import dev.nextpixel.cafebar919.data.RadnikE
import dev.nextpixel.cafebar919.data.RazlogE
import dev.nextpixel.cafebar919.data.Uloga
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Validacija
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

/** Jedino mjesto koje piše podatke podešavanja; sva poslovna pravila su ovdje. */
class PodesavanjaRepo(private val db: AppDatabase, private val clock: Clock) {
    private val kDao = db.kategorijaDao()
    private val mDao = db.mjeraDao()
    private val aDao = db.artikalDao()
    private val rDao = db.radnikDao()
    private val zDao = db.razlogDao()
    private val pDao = db.postavkeDao()
    private val nDao = db.nocDao()

    val kategorije: Flow<List<KategorijaPrikaz>> = combine(kDao.sve(), aDao.svi()) { kat, art ->
        kat.map { k -> KategorijaPrikaz(k, art.count { it.aktivan && it.kategorijaId == k.id }) }
    }

    val mjere: Flow<List<MjeraPrikaz>> = combine(mDao.sve(), aDao.svi(), aDao.sveMjere()) { mj, art, veze ->
        mj.map { m ->
            val ids = veze.filter { it.mjeraId == m.id }.map { it.artikalId }.toSet()
            MjeraPrikaz(m, art.filter { it.aktivan && it.id in ids }.map { it.naziv })
        }
    }

    val artikli: Flow<List<ArtikalPrikaz>> =
        combine(aDao.svi(), kDao.sve(), mDao.sve(), aDao.sveMjere()) { art, kat, mj, veze ->
            art.map { a ->
                ArtikalPrikaz(
                    a = a,
                    kategorija = kat.first { it.id == a.kategorijaId },
                    mjere = veze.filter { it.artikalId == a.id }.sortedBy { it.redoslijed }
                        .mapNotNull { v -> mj.firstOrNull { it.id == v.mjeraId } },
                )
            }
        }

    val radnici: Flow<List<RadnikE>> = rDao.svi()
    val razlozi: Flow<List<RazlogE>> = zDao.svi().map { l -> l.filter { it.aktivan } }
    val postavke: Flow<PostavkeE> = pDao.postavke()

    val brojaciMenija: Flow<BrojaciMenija> =
        combine(aDao.svi(), kDao.sve(), mDao.sve(), rDao.svi()) { art, kat, mj, rad ->
            BrojaciMenija(art.count { it.aktivan }, kat.count { it.aktivna }, mj.count { it.aktivna }, rad.count { it.aktivan })
        }

    private fun kljuc(s: String) = Validacija.kljucNaziva(s)
    private fun odbij(o: Odbijanje) = Ishod.Odbijeno(o)

    // ---- kategorije ----

    suspend fun sacuvajKategoriju(id: Long?, naziv: String): Ishod = db.withTransaction {
        val greska = Validacija.provjeriNaziv(naziv, Validacija.NAZIV_KATEGORIJE_MAX)
        if (greska is Ishod.Odbijeno) return@withTransaction greska
        val sve = kDao.jednom()
        val ime = Validacija.ocisti(naziv)
        if (sve.any { it.aktivna && it.id != id && kljuc(it.naziv) == kljuc(ime) }) return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
        if (id == null) {
            kDao.dodaj(KategorijaE(naziv = ime, redoslijed = (sve.maxOfOrNull { it.redoslijed } ?: -1) + 1))
        } else {
            kDao.izmijeni(sve.first { it.id == id }.copy(naziv = ime))
        }
        Ishod.Ok
    }

    suspend fun postaviAktivnaKategorija(id: Long, aktivna: Boolean): Ishod = db.withTransaction {
        val k = kDao.jednom().first { it.id == id }
        if (aktivna) {
            if (kDao.jednom().any { it.aktivna && it.id != id && kljuc(it.naziv) == kljuc(k.naziv) }) {
                return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
            }
        } else if (aDao.jednom().any { it.aktivan && it.kategorijaId == id }) {
            return@withTransaction odbij(Odbijanje.U_UPOTREBI)
        }
        kDao.izmijeni(k.copy(aktivna = aktivna))
        Ishod.Ok
    }

    suspend fun redoslijedKategorija(ids: List<Long>) = db.withTransaction {
        val sve = kDao.jednom().associateBy { it.id }
        ids.forEachIndexed { i, id -> sve[id]?.let { kDao.izmijeni(it.copy(redoslijed = i)) } }
    }

    // ---- mjere ----

    suspend fun sacuvajMjeru(id: Long?, naziv: String, natpis: String, uZbiru: String, litara: Double?): Ishod = db.withTransaction {
        val gNaziv = Validacija.provjeriNaziv(naziv, Validacija.NAZIV_MJERE_MAX)
        if (gNaziv is Ishod.Odbijeno) return@withTransaction gNaziv
        val nat = Validacija.ocisti(natpis)
        if (nat.isEmpty()) return@withTransaction odbij(Odbijanje.NATPIS_PRAZAN)
        if (nat.length > Validacija.NATPIS_MAX) return@withTransaction odbij(Odbijanje.NATPIS_PREDUG)
        val zbir = Validacija.ocisti(uZbiru)
        if (zbir.isEmpty()) return@withTransaction odbij(Odbijanje.U_ZBIRU_PRAZNO)
        if (zbir.length > Validacija.U_ZBIRU_MAX) return@withTransaction odbij(Odbijanje.U_ZBIRU_PREDUGO)
        val gLitre = Validacija.provjeriLitre(litara)
        if (gLitre is Ishod.Odbijeno) return@withTransaction gLitre
        val sve = mDao.jednom()
        val ime = Validacija.ocisti(naziv)
        if (sve.any { it.aktivna && it.id != id && kljuc(it.naziv) == kljuc(ime) }) return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
        if (id == null) {
            mDao.dodaj(MjeraE(naziv = ime, natpis = nat, uZbiru = zbir, litara = litara, redoslijed = (sve.maxOfOrNull { it.redoslijed } ?: -1) + 1))
        } else {
            mDao.izmijeni(sve.first { it.id == id }.copy(naziv = ime, natpis = nat, uZbiru = zbir, litara = litara))
        }
        Ishod.Ok
    }

    suspend fun postaviAktivnaMjera(id: Long, aktivna: Boolean): Ishod = db.withTransaction {
        val m = mDao.jednom().first { it.id == id }
        if (aktivna) {
            if (mDao.jednom().any { it.aktivna && it.id != id && kljuc(it.naziv) == kljuc(m.naziv) }) {
                return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
            }
        } else {
            val aktivniArtikli = aDao.jednom().filter { it.aktivan }.map { it.id }.toSet()
            if (aDao.sveMjereJednom().any { it.mjeraId == id && it.artikalId in aktivniArtikli }) {
                return@withTransaction odbij(Odbijanje.U_UPOTREBI)
            }
        }
        mDao.izmijeni(m.copy(aktivna = aktivna))
        Ishod.Ok
    }

    // ---- artikli ----

    suspend fun sacuvajArtikal(id: Long?, naziv: String, kategorijaId: Long?, mjereIds: List<Long>): Ishod = db.withTransaction {
        val gNaziv = Validacija.provjeriNaziv(naziv, Validacija.NAZIV_ARTIKLA_MAX)
        if (gNaziv is Ishod.Odbijeno) return@withTransaction gNaziv
        if (kategorijaId == null) return@withTransaction odbij(Odbijanje.BEZ_KATEGORIJE)
        val mjereBez = mjereIds.distinct()
        if (mjereBez.size !in 1..3) return@withTransaction odbij(Odbijanje.BROJ_MJERA)
        val ime = Validacija.ocisti(naziv)
        val svi = aDao.jednom()
        val postojeci = id?.let { i -> svi.first { it.id == i } }
        val bioAktivan = postojeci?.aktivan ?: true
        if (bioAktivan && svi.any { it.aktivan && it.id != id && kljuc(it.naziv) == kljuc(ime) }) {
            return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
        }
        if (bioAktivan) {
            if (!kDao.jednom().first { it.id == kategorijaId }.aktivna) return@withTransaction odbij(Odbijanje.KATEGORIJA_ISKLJUCENA)
            val mj = mDao.jednom().associateBy { it.id }
            if (mjereBez.any { mj[it]?.aktivna != true }) return@withTransaction odbij(Odbijanje.MJERA_ISKLJUCENA)
        }
        val aid = if (postojeci == null) {
            aDao.dodaj(ArtikalE(naziv = ime, kategorijaId = kategorijaId, redoslijed = (svi.maxOfOrNull { it.redoslijed } ?: -1) + 1))
        } else {
            aDao.izmijeni(postojeci.copy(naziv = ime, kategorijaId = kategorijaId))
            aDao.obrisiMjere(postojeci.id)
            postojeci.id
        }
        aDao.dodajMjere(mjereBez.mapIndexed { i, m -> ArtikalMjeraE(aid, m, i) })
        Ishod.Ok
    }

    suspend fun postaviAktivanArtikal(id: Long, aktivan: Boolean): Ishod = db.withTransaction {
        val svi = aDao.jednom()
        val a = svi.first { it.id == id }
        if (aktivan) {
            if (svi.any { it.aktivan && it.id != id && kljuc(it.naziv) == kljuc(a.naziv) }) return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
            if (!kDao.jednom().first { it.id == a.kategorijaId }.aktivna) return@withTransaction odbij(Odbijanje.KATEGORIJA_ISKLJUCENA)
            val neaktivneMjere = mDao.jednom().filter { !it.aktivna }.map { it.id }.toSet()
            if (aDao.sveMjereJednom().any { it.artikalId == id && it.mjeraId in neaktivneMjere }) {
                return@withTransaction odbij(Odbijanje.MJERA_ISKLJUCENA)
            }
        }
        aDao.izmijeni(a.copy(aktivan = aktivan))
        Ishod.Ok
    }

    suspend fun redoslijedArtikala(ids: List<Long>) = db.withTransaction {
        val svi = aDao.jednom().associateBy { it.id }
        ids.forEachIndexed { i, id -> svi[id]?.let { aDao.izmijeni(it.copy(redoslijed = i)) } }
    }

    /** Količine tog artikla u neponištenim turama aktivne noći, po mjeri; prazno kad nema aktivne noći. */
    suspend fun unosiUAktivnojNoci(artikalId: Long): List<Format.MjeraBroj> =
        nDao.stavkeAktivneNociZaArtikal(artikalId)
            .groupBy { it.mjeraId }
            .map { (_, s) -> Format.MjeraBroj(s.first().mjeraUZbiru, s.first().mjeraRedoslijed, s.sumOf { it.kolicina }) }
            .sortedBy { it.redoslijed }

    // ---- ljudi ----

    suspend fun sacuvajRadnika(id: Long?, ime: String, uloga: Uloga): Ishod = db.withTransaction {
        val g = Validacija.provjeriNaziv(ime, Validacija.IME_MAX)
        if (g is Ishod.Odbijeno) return@withTransaction g
        val cisto = Validacija.ocisti(ime)
        val svi = rDao.jednom()
        if (svi.any { it.aktivan && it.id != id && kljuc(it.ime) == kljuc(cisto) }) return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
        if (id == null) {
            rDao.dodaj(RadnikE(ime = cisto, uloga = uloga, redoslijed = (svi.maxOfOrNull { it.redoslijed } ?: -1) + 1))
        } else {
            rDao.izmijeni(svi.first { it.id == id }.copy(ime = cisto, uloga = uloga))
        }
        Ishod.Ok
    }

    /** Bez pravila u repozitoriju; potvrdu traži UI. */
    suspend fun postaviAktivanRadnik(id: Long, aktivan: Boolean) = db.withTransaction {
        rDao.izmijeni(rDao.jednom().first { it.id == id }.copy(aktivan = aktivan))
    }

    // ---- pravila rada ----

    private suspend fun postavi(vrijednost: Int, raspon: IntRange, korak: Int, izmjena: (PostavkeE, Int) -> PostavkeE): Ishod =
        db.withTransaction {
            if (vrijednost !in raspon || (vrijednost - raspon.first) % korak != 0) {
                return@withTransaction odbij(Odbijanje.VRIJEDNOST_VAN_OPSEGA)
            }
            pDao.izmijeni(izmjena(pDao.jedne(), vrijednost))
            Ishod.Ok
        }

    suspend fun postaviRokBezPina(s: Int): Ishod = postavi(s, 0..300, 10) { p, v -> p.copy(rokBezPinaS = v) }
    suspend fun postaviIstoriju(dana: Int): Ishod = postavi(dana, 7..90, 1) { p, v -> p.copy(istorijaDana = v) }
    suspend fun postaviZakljucavanje(s: Int): Ishod = postavi(s, 60..600, 60) { p, v -> p.copy(zakljucavanjeS = v) }

    suspend fun postaviIspomoc(dozvoljena: Boolean) = db.withTransaction {
        pDao.izmijeni(pDao.jedne().copy(ispomocDozvoljena = dozvoljena))
    }

    /** Zatvorene noći sa datumom prije (danas − dana); spec 7 #33. */
    suspend fun nociKojeBiNestale(dana: Int): Int =
        nDao.brojZatvorenihStarijihOd(LocalDate.now(clock).minusDays(dana.toLong()).toEpochDay())

    suspend fun dodajRazlog(tekst: String): Ishod = db.withTransaction {
        val g = Validacija.provjeriNaziv(tekst, Validacija.RAZLOG_MAX)
        if (g is Ishod.Odbijeno) return@withTransaction g
        val cisto = Validacija.ocisti(tekst)
        val svi = zDao.jednom()
        if (svi.any { it.aktivan && kljuc(it.tekst) == kljuc(cisto) }) return@withTransaction odbij(Odbijanje.NAZIV_POSTOJI)
        zDao.dodaj(RazlogE(tekst = cisto, redoslijed = (svi.maxOfOrNull { it.redoslijed } ?: -1) + 1))
        Ishod.Ok
    }

    suspend fun ukloniRazlog(id: Long) = db.withTransaction {
        zDao.jednom().firstOrNull { it.id == id }?.let { zDao.izmijeni(it.copy(aktivan = false)) }
    }
}
