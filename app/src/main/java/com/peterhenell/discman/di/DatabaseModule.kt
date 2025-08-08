package com.peterhenell.discman.di

import android.content.Context
import androidx.room.Room
import com.peterhenell.discman.data.DiscmanDatabase
import com.peterhenell.discman.data.dao.*
import com.peterhenell.discman.data.repository.DataStorageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDiscmanDatabase(@ApplicationContext context: Context): DiscmanDatabase {
        return DiscmanDatabase.getDatabase(context)
    }

    @Provides
    fun provideCourseDao(database: DiscmanDatabase): CourseDao {
        return database.courseDao()
    }

    @Provides
    fun provideHoleDao(database: DiscmanDatabase): HoleDao {
        return database.holeDao()
    }

    @Provides
    fun providePlayerDao(database: DiscmanDatabase): PlayerDao {
        return database.playerDao()
    }

    @Provides
    fun provideGameDao(database: DiscmanDatabase): GameDao {
        return database.gameDao()
    }

    @Provides
    @Singleton
    fun provideDataStorageService(
        courseDao: CourseDao,
        holeDao: HoleDao,
        playerDao: PlayerDao,
        gameDao: GameDao
    ): DataStorageService {
        return DataStorageService(courseDao, holeDao, playerDao, gameDao)
    }
}
