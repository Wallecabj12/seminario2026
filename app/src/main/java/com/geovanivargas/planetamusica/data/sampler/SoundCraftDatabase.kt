package com.geovanivargas.planetamusica.data.sampler

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PadEntity::class], version = 1, exportSchema = false)
abstract class SoundCraftDatabase : RoomDatabase() {

    abstract fun padDao(): PadDao

    companion object {
        @Volatile
        private var INSTANCE: SoundCraftDatabase? = null

        fun getInstance(context: Context): SoundCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SoundCraftDatabase::class.java,
                    "soundcraft_db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
