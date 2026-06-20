package com.peterhenell.discman.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.peterhenell.discman.data.dao.*
import com.peterhenell.discman.data.entities.*

@Database(
    entities = [
        Course::class,
        Hole::class,
        Player::class,
        Game::class,
        GamePlayer::class,
        GamePlayerHoleThrow::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DiscmanDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun holeDao(): HoleDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: DiscmanDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE games ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): DiscmanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiscmanDatabase::class.java,
                    "discman_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
