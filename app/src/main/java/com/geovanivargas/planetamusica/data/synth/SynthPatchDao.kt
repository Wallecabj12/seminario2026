package com.geovanivargas.planetamusica.data.synth

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SynthPatchDao {
    @Query("SELECT * FROM synth_patches ORDER BY isFactory DESC, name ASC")
    fun getAllPatches(): Flow<List<SynthPatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patch: SynthPatchEntity): Long

    @Query("DELETE FROM synth_patches WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE synth_patches SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("SELECT COUNT(*) FROM synth_patches WHERE isFactory = 1")
    suspend fun factoryCount(): Int
}
