package com.downnotice.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.downnotice.mobile.ui.navigation.DownNoticeNavHost
import com.downnotice.mobile.ui.theme.DownNoticeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DownNoticeTheme {
                DownNoticeNavHost()
            }
        }
    }
}
