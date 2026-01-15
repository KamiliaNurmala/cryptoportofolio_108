package com.example.cryptoportfolio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio")
data class Portfolio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,              // ← Foreign key ke User
    val coinId: String,
    val coinSymbol: String,
    val coinName: String,
    val amount: Double,
    val buyPrice: Double,
    val buyDate: Long,
    val notes: String = ""
)