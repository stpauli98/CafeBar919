package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
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
}
