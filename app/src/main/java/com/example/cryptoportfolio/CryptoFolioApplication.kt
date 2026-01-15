package com.example.cryptoportfolio

import android.app.Application
import com.example.cryptoportfolio.di.AppContainer
import com.example.cryptoportfolio.di.AppDataContainer

class CryptoFolioApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}