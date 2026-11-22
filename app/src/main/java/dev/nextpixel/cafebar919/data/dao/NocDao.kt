package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.nextpixel.cafebar919.data.NacrtTureE
import dev.nextpixel.cafebar919.data.NocE
import dev.nextpixel.cafebar919.data.NocPrimalacE
import dev.nextpixel.cafebar919.data.StavkaTureE
import dev.nextpixel.cafebar919.data.TuraE

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
}
