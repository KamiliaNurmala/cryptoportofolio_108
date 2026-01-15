package com.example.cryptoportfolio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cryptoportfolio.di.AppDataContainer
import com.example.cryptoportfolio.ui.navigation.AppNavigation
import com.example.cryptoportfolio.ui.theme.CryptoportofolioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = AppDataContainer(applicationContext)

        setContent {
            CryptoportofolioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(appContainer = appContainer)
                }
            }
        }
    }
}