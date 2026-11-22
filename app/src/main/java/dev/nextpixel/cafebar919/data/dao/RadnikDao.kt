package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.nextpixel.cafebar919.data.RadnikE
import kotlinx.coroutines.flow.Flow

@Dao
interface RadnikDao {
    @Query("SELECT * FROM radnik ORDER BY redoslijed, id")
    fun svi(): Flow<List<RadnikE>>

    @Insert suspend fun dodaj(r: RadnikE): Long
    @Update suspend fun izmijeni(r: RadnikE)

    @Query("SELECT * FROM radnik ORDER BY redoslijed, id")
    suspend fun jednom(): List<RadnikE>
}
