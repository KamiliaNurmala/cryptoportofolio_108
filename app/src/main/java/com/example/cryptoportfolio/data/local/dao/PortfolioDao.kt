package com.example.cryptoportfolio.data.local.dao

import androidx.room.*
import com.example.cryptoportfolio.data.local.entity.Portfolio
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio WHERE userId = :userId ORDER BY coinName ASC")
    fun getAllPortfolio(userId: Int): Flow<List<Portfolio>>

    @Query("SELECT * FROM portfolio WHERE id = :id")
    fun getPortfolioById(id: Int): Flow<Portfolio?>

    @Query("SELECT * FROM portfolio WHERE coinId = :coinId AND userId = :userId")
    fun getPortfolioByCoinId(coinId: String, userId: Int): Flow<Portfolio?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portfolio: Portfolio)

    @Update
    suspend fun update(portfolio: Portfolio)

    @Delete
    suspend fun delete(portfolio: Portfolio)

    @Query("DELETE FROM portfolio WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: Int)
}