package com.example.cryptoportfolio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,              // ← Foreign key ke User
    val coinId: String,
    val coinSymbol: String,
    val coinName: String,
    val type: String,             // "BUY" or "SELL"
    val amount: Double,
    val price: Double,
    val date: Long,
    val notes: String = ""
)