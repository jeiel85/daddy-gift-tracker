package com.jeiel.daddygifttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeiel.daddygifttracker.ui.screens.MainScreen
import com.jeiel.daddygifttracker.ui.theme.GyeongjosaTheme
import com.jeiel.daddygifttracker.viewmodel.GyeongjosaViewModel
import com.jeiel.daddygifttracker.notification.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize notifications channel at startup
        NotificationHelper.createNotificationChannel(this)

        enableEdgeToEdge()
        setContent {
            GyeongjosaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: GyeongjosaViewModel = viewModel()
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

