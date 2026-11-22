package dev.nextpixel.cafebar919.repo

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.room.withTransaction
import dev.nextpixel.cafebar919.data.AppDatabase
import dev.nextpixel.cafebar919.data.ArtikalE
import dev.nextpixel.cafebar919.data.ArtikalMjeraE
import dev.nextpixel.cafebar919.data.KategorijaE
import dev.nextpixel.cafebar919.data.MjeraE
import dev.nextpixel.cafebar919.data.NacrtTureE
import dev.nextpixel.cafebar919.data.StavkaTureE
import dev.nextpixel.cafebar919.data.TuraE
import dev.nextpixel.cafebar919.data.TuraSaStavkama
import dev.nextpixel.cafebar919.domain.Format
import dev.nextpixel.cafebar919.domain.Ishod
import dev.nextpixel.cafebar919.domain.Nacrt
import dev.nextpixel.cafebar919.domain.Odbijanje
import dev.nextpixel.cafebar919.domain.Rok
import dev.nextpixel.cafebar919.domain.StavkaNacrta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

sealed interface Potvrda {
    data class Upisana(val turaId: Long, val ime: String, val brojStavki: Int, val noviKljuc: String) : Potvrda
    data class Odbijena(val razlog: Odbijanje) : Potvrda
    data object NijeUpisana : Potvrda   // UPIS-01: izuzetak baze, nacrt ostaje
}

/** Unos ture: katalog za S1, nacrt koji preživljava restart, potvrda i poništavanje bez PIN-a u roku. */
class TuraRepo(private val db: AppDatabase, private val clock: Clock) {
    private val dao = db.nocDao()
    private val aDao = db.artikalDao()
    private val kDao = db.kategorijaDao()
    private val mDao = db.mjeraDao()
    private val pDao = db.postavkeDao()

    val katalog: Flow<List<ArtikalPrikaz>> =
        combine(aDao.svi(), kDao.sve(), mDao.sve(), aDao.sveMjere()) { art, kat, mj, veze -> sastavi(art, kat, mj, veze) }

    val rokBezPina: Flow<Int> = pDao.postavke().map { it.rokBezPinaS }

    suspend fun rokBezPinaS(): Int = pDao.jedne().rokBezPinaS

    /** Trenutni nacrt; ako ga nema, upisuje prazan sa novim ključem. */
    suspend fun nacrt(): NacrtTureE = db.withTransaction { dao.nacrt() ?: prazanNacrt() }

    /** Zakasnio upis sa ključem već potvrđene ture se ignoriše, da ne vrati upisane stavke u nacrt. */
    suspend fun sacuvajNacrt(kljuc: String, primalacId: Long?, stavke: List<StavkaNacrta>) = db.withTransaction {
        if (dao.turaPoKljucu(kljuc) != null) return@withTransaction
        dao.upisiNacrt(NacrtTureE(id = 1, primalacId = primalacId, kljuc = kljuc, stavke = Nacrt.uJson(stavke), izmijenjenU = clock.millis()))
    }

    /**
     * Tura, stavke i novi prazan nacrt upisuju se u jednoj transakciji; isti `kljuc` daje jednu turu.
     * Izuzetak se hvata izvan transakcije, pa je ona već vraćena i nacrt u bazi ostaje kakav je bio (UPIS-01).
     */
    suspend fun potvrdi(kljuc: String, primalacId: Long?, stavke: List<StavkaNacrta>): Potvrda = try {
        db.withTransaction { upisi(kljuc, primalacId, stavke) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: SQLiteConstraintException) {
        // Ista tura upisana mimo ove transakcije; drugo ograničenje (ili kvar) nema turu sa ovim ključem.
        postojecaPoslijeKvara(kljuc, e)
    } catch (e: Exception) {
        Log.e(TAG, "Tura nije upisana", e)
        Potvrda.NijeUpisana
    }

    suspend fun ponistiBezPina(turaId: Long): Ishod = db.withTransaction {
        val t = dao.tura(turaId)
        if (t == null || dao.aktivnaJednom()?.id != t.nocId) return@withTransaction Ishod.Odbijeno(Odbijanje.NEMA_NOCI)
        if (t.ponistenaU != null) return@withTransaction Ishod.Odbijeno(Odbijanje.VEC_PONISTENA)
        val sada = clock.millis()
        if (Rok.preostaloSekundi(t.potvrdjenaU, sada, pDao.jedne().rokBezPinaS) == 0) {
            return@withTransaction Ishod.Odbijeno(Odbijanje.ROK_ISTEKAO)
        }
        dao.izmijeniTuru(t.copy(ponistenaU = sada, ponistenaBezPina = true))
        Ishod.Ok
    }

    /** Posljednjih 10 tura noći (spec 7 #32), najnovija prva; stavke redom unosa. */
    fun posljednje(nocId: Long): Flow<List<TuraSaStavkama>> =
        dao.posljednjeTure(nocId, POSLJEDNJIH).map { l -> l.map { it.copy(stavke = it.stavke.sortedBy { s -> s.id }) } }

    /** Zbir mjera neponištenih tura po primaocu (isti tekst kao `Obracun.zbirMjera`); primalac bez tura nema ključ. */
    fun zbiroviPoPrimaocu(nocId: Long): Flow<Map<Long, String>> =
        dao.stavkePoPrimaocu(nocId).map { l ->
            l.groupBy { it.primalacId }.mapValues { (_, s) -> Format.zbirMjera(s.map { Format.MjeraBroj(it.uZbiru, it.mjeraRedoslijed, it.kolicina) }) }
        }

    private suspend fun upisi(kljuc: String, primalacId: Long?, stavke: List<StavkaNacrta>): Potvrda {
        dao.turaPoKljucu(kljuc)?.let { return upisana(it) }
        val noc = dao.aktivnaJednom() ?: return Potvrda.Odbijena(Odbijanje.NEMA_NOCI)
        val primalac = primalacId?.let { id -> dao.primaociJednom(noc.id).firstOrNull { it.id == id } }
            ?: return Potvrda.Odbijena(Odbijanje.BEZ_PRIMAOCA)
        val katalog = sastavi(aDao.jednom(), kDao.jednom(), mDao.jednom(), aDao.sveMjereJednom())
        val vazece = vazeceStavke(stavke, katalog)
        if (vazece.isEmpty()) return Potvrda.Odbijena(Odbijanje.PRAZNA_TURA)

        val sada = clock.millis()
        val turaId = dao.dodajTuru(TuraE(nocId = noc.id, primalacId = primalac.id, potvrdjenaU = sada, kljuc = kljuc))
        val poArtiklu = katalog.associateBy { it.a.id }
        dao.dodajStavke(vazece.map { s ->
            val p = poArtiklu.getValue(s.artikalId)
            val m = p.mjere.first { it.id == s.mjeraId }
            StavkaTureE(
                turaId = turaId,
                artikalId = p.a.id, artikalNaziv = p.a.naziv, artikalRedoslijed = p.a.redoslijed,
                kategorijaId = p.kategorija.id, kategorijaNaziv = p.kategorija.naziv, kategorijaRedoslijed = p.kategorija.redoslijed,
                mjeraId = m.id, mjeraNaziv = m.naziv, mjeraNatpis = m.natpis, mjeraUZbiru = m.uZbiru, mjeraRedoslijed = m.redoslijed,
                litara = m.litara, kolicina = s.kolicina,
            )
        })
        val novi = prazanNacrt()
        return Potvrda.Upisana(turaId, primalac.ime, vazece.size, novi.kljuc)
    }

    /** Već upisana tura; nacrt koji je ostao na njenom ključu dobija novi, da sljedeća tura ne bi bila progutana. */
    private suspend fun upisana(t: TuraE): Potvrda.Upisana {
        val n = dao.nacrt()
        val noviKljuc = if (n == null || n.kljuc == t.kljuc) prazanNacrt().kljuc else n.kljuc
        val ime = dao.primaociJednom(t.nocId).firstOrNull { it.id == t.primalacId }?.ime.orEmpty()
        return Potvrda.Upisana(t.id, ime, dao.brojStavki(t.id), noviKljuc)
    }

    private suspend fun postojecaPoslijeKvara(kljuc: String, uzrok: SQLiteConstraintException): Potvrda = try {
        db.withTransaction { dao.turaPoKljucu(kljuc)?.let { upisana(it) } }
            ?: Potvrda.NijeUpisana.also { Log.e(TAG, "Tura nije upisana", uzrok) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        uzrok.addSuppressed(e)
        Log.e(TAG, "Tura nije upisana", uzrok)
        Potvrda.NijeUpisana
    }

    private suspend fun prazanNacrt(): NacrtTureE =
        NacrtTureE(id = 1, primalacId = null, kljuc = UUID.randomUUID().toString(), stavke = "[]", izmijenjenU = clock.millis())
            .also { dao.upisiNacrt(it) }

    companion object {
        private const val TAG = "CafeBar919"
        private const val POSLJEDNJIH = 10

        /** Zadržava stavke čiji par artikal–mjera postoji u katalogu, redom kao u nacrtu. */
        fun vazeceStavke(stavke: List<StavkaNacrta>, katalog: List<ArtikalPrikaz>): List<StavkaNacrta> {
            val parovi = katalog.flatMapTo(HashSet()) { p -> p.mjere.map { p.a.id to it.id } }
            return stavke.filter { (it.artikalId to it.mjeraId) in parovi }
        }

        /** Aktivna pića aktivne kategorije sa aktivnim mjerama redom iz `artikal_mjera`; piće bez mjere se izostavlja. */
        private fun sastavi(art: List<ArtikalE>, kat: List<KategorijaE>, mj: List<MjeraE>, veze: List<ArtikalMjeraE>): List<ArtikalPrikaz> {
            val kategorije = kat.filter { it.aktivna }.associateBy { it.id }
            val mjere = mj.filter { it.aktivna }.associateBy { it.id }
            val poArtiklu = veze.groupBy { it.artikalId }
            return art.asSequence()
                .filter { it.aktivan }
                .sortedWith(compareBy({ it.redoslijed }, { it.id }))
                .mapNotNull { a ->
                    val k = kategorije[a.kategorijaId] ?: return@mapNotNull null
                    val m = poArtiklu[a.id].orEmpty().sortedBy { it.redoslijed }.mapNotNull { mjere[it.mjeraId] }
                    if (m.isEmpty()) null else ArtikalPrikaz(a, k, m)
                }
                .toList()
        }
    }
}
