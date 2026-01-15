package com.example.cryptoportfolio.data.remote.api

import com.example.cryptoportfolio.data.remote.model.MarketCoin
import com.example.cryptoportfolio.data.remote.model.CoinDetail
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApi {

    // Get simple price for multiple coins
    @GET("simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") currencies: String = "usd,idr",
        @Query("include_24hr_change") includeChange: Boolean = true
    ): Map<String, Map<String, Double>>

    // Get market data for top coins
    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<MarketCoin>

    // Get detailed coin info
    @GET("coins/{id}")
    suspend fun getCoinDetail(
        @Path("id") coinId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false
    ): CoinDetail

    // Search coins
    @GET("search")
    suspend fun searchCoins(
        @Query("query") query: String
    ): Map<String, Any>
}