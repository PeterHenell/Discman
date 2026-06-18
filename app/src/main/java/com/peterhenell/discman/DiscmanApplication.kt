package com.peterhenell.discman

import android.app.Application
import com.peterhenell.discman.debug.DataSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DiscmanApplication : Application() {

    @Inject
    lateinit var dataSeeder: DataSeeder

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            dataSeeder.seed()
        }
    }
}
