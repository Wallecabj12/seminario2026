package com.geovanivargas.planetamusica.data.sampler

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PadDao {

    @Query("SELECT * FROM pads ORDER BY position ASC")
    fun getAllPads(): Flow<List<PadEntity>>

    @Query("SELECT * FROM pads WHERE id = :id")
    suspend fun getPadById(id: Int): PadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pads: List<PadEntity>)

    @Update
    suspend fun updatePad(pad: PadEntity)

    @Query("SELECT COUNT(*) FROM pads")
    suspend fun getPadCount(): Int
}
