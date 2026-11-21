package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.nextpixel.cafebar919.data.ArtikalE
import dev.nextpixel.cafebar919.data.ArtikalMjeraE
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtikalDao {
    @Query("SELECT * FROM artikal ORDER BY redoslijed, id")
    fun svi(): Flow<List<ArtikalE>>

    @Query("SELECT * FROM artikal_mjera ORDER BY artikal_id, redoslijed")
    fun sveMjere(): Flow<List<ArtikalMjeraE>>

    @Insert suspend fun dodaj(a: ArtikalE): Long
    @Update suspend fun izmijeni(a: ArtikalE)
    @Insert suspend fun dodajMjere(m: List<ArtikalMjeraE>)

    @Query("SELECT * FROM artikal ORDER BY redoslijed, id")
    suspend fun jednom(): List<ArtikalE>

    @Query("SELECT * FROM artikal_mjera ORDER BY artikal_id, redoslijed")
    suspend fun sveMjereJednom(): List<ArtikalMjeraE>

    @Query("DELETE FROM artikal_mjera WHERE artikal_id = :artikalId")
    suspend fun obrisiMjere(artikalId: Long)
}
