package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.nextpixel.cafebar919.data.RazlogE
import kotlinx.coroutines.flow.Flow

@Dao
interface RazlogDao {
    @Query("SELECT * FROM razlog_ponistavanja ORDER BY redoslijed, id")
    fun svi(): Flow<List<RazlogE>>

    @Insert suspend fun dodaj(r: RazlogE): Long
    @Update suspend fun izmijeni(r: RazlogE)

    @Query("SELECT * FROM razlog_ponistavanja ORDER BY redoslijed, id")
    suspend fun jednom(): List<RazlogE>
}
