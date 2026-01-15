package com.example.cryptoportfolio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val email: String,
    val password: String,  // hashed with SHA-256
    val createdAt: Long = System.currentTimeMillis()
)