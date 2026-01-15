package com.example.cryptoportfolio.data.repository

import com.example.cryptoportfolio.data.remote.RetrofitClient
import com.example.cryptoportfolio.data.remote.model.CoinDetail
import com.example.cryptoportfolio.data.remote.model.MarketCoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CryptoRepository {

    private val api = RetrofitClient.api

    fun getSimplePrices(coinIds: String): Flow<Map<String, Map<String, Double>>> = flow {
        try {
            val response = api.getSimplePrices(coinIds)
            emit(response)
        } catch (e: Exception) {
            emit(emptyMap())
        }
    }

    fun getMarkets(): Flow<List<MarketCoin>> = flow {
        try {
            val response = api.getMarkets()
            emit(response)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    fun getCoinDetail(coinId: String): Flow<CoinDetail?> = flow {
        try {
            val response = api.getCoinDetail(coinId)
            emit(response)
        } catch (e: Exception) {
            emit(null)
        }
    }
}