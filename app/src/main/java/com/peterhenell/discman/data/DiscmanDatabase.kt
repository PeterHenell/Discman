package com.peterhenell.discman.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
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

        fun getDatabase(context: Context): DiscmanDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiscmanDatabase::class.java,
                    "discman_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
