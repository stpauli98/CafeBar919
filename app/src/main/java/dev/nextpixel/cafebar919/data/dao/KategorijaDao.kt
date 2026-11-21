package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import dev.nextpixel.cafebar919.data.KategorijaE
import kotlinx.coroutines.flow.Flow

@Dao
interface KategorijaDao {
    @Query("SELECT * FROM kategorija ORDER BY redoslijed, id")
    fun sve(): Flow<List<KategorijaE>>

    @Insert suspend fun dodaj(k: KategorijaE): Long
    @Update suspend fun izmijeni(k: KategorijaE)
}
