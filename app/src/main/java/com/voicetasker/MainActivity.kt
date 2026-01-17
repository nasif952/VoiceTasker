package com.voicetasker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.voicetasker.ui.VoiceTaskerApp
import com.voicetasker.ui.theme.VoiceTaskerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for VoiceTasker.
 *
 * This is the entry point of the application. It uses Compose for the UI
 * and handles the overall app navigation and theming.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceTaskerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VoiceTaskerApp()
                }
            }
        }
    }
}
