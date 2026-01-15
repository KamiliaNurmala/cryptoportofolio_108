package com.example.cryptoportfolio.di

import android.content.Context
import com.example.cryptoportfolio.data.local.AppDatabase
import com.example.cryptoportfolio.data.local.UserPreferences
import com.example.cryptoportfolio.data.repository.AuthRepository
import com.example.cryptoportfolio.data.repository.CryptoRepository
import com.example.cryptoportfolio.data.repository.PortfolioRepository

interface AppContainer {
    val cryptoRepository: CryptoRepository
    val portfolioRepository: PortfolioRepository
    val authRepository: AuthRepository  // ← TAMBAH
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val database = AppDatabase.getDatabase(context)
    private val userPreferences = UserPreferences(context)  // ← TAMBAH

    override val cryptoRepository: CryptoRepository by lazy {
        CryptoRepository()
    }

    override val portfolioRepository: PortfolioRepository by lazy {
        PortfolioRepository(
            portfolioDao = database.portfolioDao(),
            transactionDao = database.transactionDao()
        )
    }

    override val authRepository: AuthRepository by lazy {  // ← TAMBAH
        AuthRepository(
            userDao = database.userDao(),
            userPreferences = userPreferences
        )
    }
}