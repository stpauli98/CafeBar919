package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.nextpixel.cafebar919.data.MjeraE
import kotlinx.coroutines.flow.Flow

@Dao
interface MjeraDao {
    @Query("SELECT * FROM mjera ORDER BY redoslijed, id")
    fun sve(): Flow<List<MjeraE>>

    @Insert suspend fun dodaj(m: MjeraE): Long
    @Update suspend fun izmijeni(m: MjeraE)
}
