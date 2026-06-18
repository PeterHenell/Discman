package com.peterhenell.discman.di

import com.peterhenell.discman.debug.DataSeeder
import com.peterhenell.discman.debug.DebugDataSeeder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSeederModule {

    @Binds
    @Singleton
    abstract fun bindDataSeeder(debugDataSeeder: DebugDataSeeder): DataSeeder
}

