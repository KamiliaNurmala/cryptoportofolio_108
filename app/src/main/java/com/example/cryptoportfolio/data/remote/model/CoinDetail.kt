package com.example.cryptoportfolio.data.remote.model

import com.google.gson.annotations.SerializedName

data class CoinDetail(
    @SerializedName("id")
    val id: String,

    @SerializedName("symbol")
    val symbol: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: CoinImage?,

    @SerializedName("market_data")
    val marketData: MarketData?
)

data class CoinImage(
    @SerializedName("large")
    val large: String?
)

data class MarketData(
    @SerializedName("current_price")
    val currentPrice: Map<String, Double>?,

    @SerializedName("market_cap")
    val marketCap: Map<String, Double>?,

    @SerializedName("total_volume")
    val totalVolume: Map<String, Double>?,

    @SerializedName("price_change_percentage_24h")
    val priceChange24h: Double?
)