package com.peterhenell.discman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.peterhenell.discman.ui.theme.DiscmanTheme
import com.peterhenell.discman.ui.DiscmanApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiscmanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DiscmanApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
