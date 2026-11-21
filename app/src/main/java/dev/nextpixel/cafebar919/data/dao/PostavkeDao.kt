package dev.nextpixel.cafebar919.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import dev.nextpixel.cafebar919.data.PostavkeE
import kotlinx.coroutines.flow.Flow

@Dao
interface PostavkeDao {
    @Query("SELECT * FROM postavke WHERE id = 1")
    fun postavke(): Flow<PostavkeE>

    @Query("SELECT * FROM postavke WHERE id = 1")
    suspend fun jedne(): PostavkeE

    @Update suspend fun izmijeni(p: PostavkeE)
}
