package com.geovanivargas.planetamusica.data.sampler

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.geovanivargas.planetamusica.data.presets.PresetDao
import com.geovanivargas.planetamusica.data.presets.PresetEntity
import com.geovanivargas.planetamusica.data.presets.PresetPadEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE pads ADD COLUMN volume REAL NOT NULL DEFAULT 1.0")
        db.execSQL("ALTER TABLE pads ADD COLUMN isLoop INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE pads ADD COLUMN rate REAL NOT NULL DEFAULT 1.0")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `presets` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `userId` TEXT,
                `createdAt` INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `preset_pads` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `presetId` INTEGER NOT NULL,
                `padPosition` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `filePath` TEXT,
                `category` TEXT NOT NULL,
                `volume` REAL NOT NULL DEFAULT 1.0,
                `isLoop` INTEGER NOT NULL DEFAULT 0,
                `rate` REAL NOT NULL DEFAULT 1.0,
                FOREIGN KEY(`presetId`) REFERENCES `presets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_preset_pads_presetId` ON `preset_pads` (`presetId`)")
    }
}

@Database(
    entities = [PadEntity::class, PresetEntity::class, PresetPadEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class SoundCraftDatabase : RoomDatabase() {

    abstract fun padDao(): PadDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: SoundCraftDatabase? = null

        fun getInstance(context: Context): SoundCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SoundCraftDatabase::class.java,
                    "soundcraft_db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
