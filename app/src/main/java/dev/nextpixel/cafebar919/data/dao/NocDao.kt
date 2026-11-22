package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.nextpixel.cafebar919.data.NacrtTureE
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.NocPrimalacE
import dev.nextpixel.cafebar919.data.StavkaPrimaoca
import dev.nextpixel.cafebar919.data.StavkaTureE
import dev.nextpixel.cafebar919.data.TuraE
import dev.nextpixel.cafebar919.data.TuraSaStavkama
import kotlinx.coroutines.flow.Flow

/** Upisi za noć, primaoce, ture i nacrt; čitanja dodaju Taskovi koji ih koriste. */
@Dao
interface NocDao {
    @Insert suspend fun dodajNoc(n: NocE): Long
    @Insert suspend fun dodajPrimaoce(p: List<NocPrimalacE>): List<Long>
    @Insert suspend fun dodajTuru(t: TuraE): Long
    @Insert suspend fun dodajStavke(s: List<StavkaTureE>)
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE) suspend fun upisiNacrt(n: NacrtTureE)

    /** Stavke neponištenih tura aktivne noći (`zatvorena_u` je null) za jedan artikal. */
    @Query(
        "SELECT s.* FROM stavka_ture s JOIN tura t ON t.id = s.tura_id JOIN noc n ON n.id = t.noc_id " +
            "WHERE n.zatvorena_u IS NULL AND t.ponistena_u IS NULL AND s.artikal_id = :artikalId"
    )
    suspend fun stavkeAktivneNociZaArtikal(artikalId: Long): List<StavkaTureE>

    @Query("SELECT COUNT(*) FROM noc WHERE zatvorena_u IS NOT NULL AND datum < :granica")
    suspend fun brojZatvorenihStarijihOd(granica: Long): Int

    @Query("SELECT * FROM noc WHERE zatvorena_u IS NULL ORDER BY id DESC LIMIT 1")
    fun aktivna(): Flow<NocE?>

    @Query("SELECT * FROM noc WHERE zatvorena_u IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun aktivnaJednom(): NocE?

    @Query("SELECT * FROM noc_primalac WHERE noc_id = :nocId ORDER BY redoslijed, id")
    fun primaoci(nocId: Long): Flow<List<NocPrimalacE>>

    @Query("SELECT * FROM noc_primalac WHERE noc_id = :nocId ORDER BY redoslijed, id")
    suspend fun primaociJednom(nocId: Long): List<NocPrimalacE>

    @Query("SELECT * FROM nacrt_ture WHERE id = 1")
    suspend fun nacrt(): NacrtTureE?

    @Query("SELECT * FROM tura WHERE kljuc = :kljuc")
    suspend fun turaPoKljucu(kljuc: String): TuraE?

    @Query("SELECT * FROM tura WHERE id = :id")
    suspend fun tura(id: Long): TuraE?

    @Update suspend fun izmijeniTuru(t: TuraE)

    @Query("SELECT COUNT(*) FROM stavka_ture WHERE tura_id = :turaId")
    suspend fun brojStavki(turaId: Long): Int

    @Transaction
    @Query("SELECT * FROM tura WHERE noc_id = :nocId ORDER BY id DESC LIMIT :n")
    fun posljednjeTure(nocId: Long, n: Int): Flow<List<TuraSaStavkama>>

    /** Stavke neponištenih tura jedne noći, za zbirove po primaocu. */
    @Query(
        "SELECT t.primalac_id AS primalacId, s.mjera_u_zbiru AS uZbiru, s.mjera_redoslijed AS mjeraRedoslijed, s.kolicina AS kolicina " +
            "FROM stavka_ture s JOIN tura t ON t.id = s.tura_id " +
            "WHERE t.noc_id = :nocId AND t.ponistena_u IS NULL"
    )
    fun stavkePoPrimaocu(nocId: Long): Flow<List<StavkaPrimaoca>>
}
