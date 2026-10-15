package com.geovanivargas.planetamusica.data.presets

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresetPads(pads: List<PresetPadEntity>)

    @Transaction
    @Query("SELECT * FROM presets ORDER BY createdAt DESC")
    fun getAllPresetsWithPads(): Flow<List<PresetWithPads>>

    @Transaction
    @Query("SELECT * FROM presets WHERE id = :presetId")
    suspend fun getPresetWithPadsById(presetId: Long): PresetWithPads?

    @Query("UPDATE presets SET name = :newName WHERE id = :presetId")
    suspend fun renamePreset(presetId: Long, newName: String)

    @Query("DELETE FROM presets WHERE id = :presetId")
    suspend fun deletePresetById(presetId: Long)
}
