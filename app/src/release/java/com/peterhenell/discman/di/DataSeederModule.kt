package com.peterhenell.discman.di

import com.peterhenell.discman.debug.DataSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSeederModule {

    @Provides
    @Singleton
    fun provideDataSeeder(): DataSeeder = object : DataSeeder {
        override suspend fun seed() { /* no-op in release */ }
    }
}

