package com.example.cryptoportfolio.data.repository

import com.example.cryptoportfolio.data.local.dao.PortfolioDao
import com.example.cryptoportfolio.data.local.dao.TransactionDao
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow

class PortfolioRepository(
    private val portfolioDao: PortfolioDao,
    private val transactionDao: TransactionDao
) {

    // ============================================
    // PORTFOLIO OPERATIONS (CRUD)
    // ============================================

    fun getAllPortfolio(userId: Int): Flow<List<Portfolio>> =
        portfolioDao.getAllPortfolio(userId)

//    fun getPortfolioById(id: Int): Flow<Portfolio?> =
//        portfolioDao.getPortfolioById(id)

    fun getPortfolioByCoinId(coinId: String, userId: Int): Flow<Portfolio?> =
        portfolioDao.getPortfolioByCoinId(coinId, userId)

    suspend fun insertPortfolio(portfolio: Portfolio) =
        portfolioDao.insert(portfolio)

    suspend fun updatePortfolio(portfolio: Portfolio) =
        portfolioDao.update(portfolio)

    suspend fun deletePortfolio(portfolio: Portfolio) =
        portfolioDao.delete(portfolio)

    suspend fun deleteAllPortfolioByUser(userId: Int) =
        portfolioDao.deleteAllByUser(userId)

    // ============================================
    // TRANSACTION OPERATIONS (CRUD)
    // ============================================

    // READ (All History)
    fun getAllTransactions(userId: Int): Flow<List<Transaction>> =
        transactionDao.getAllTransactions(userId)

    // READ (Specific Coin History - used for recalculation logic)
    fun getTransactionsByCoinId(coinId: String, userId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByCoinId(coinId, userId)

    // READ (Single Transaction - used for Edit Screen)
    suspend fun getTransactionById(id: Int): Transaction? =
        transactionDao.getTransactionById(id)

    // CREATE
    suspend fun insertTransaction(transaction: Transaction) =
        transactionDao.insert(transaction)

    // UPDATE (For Edit Feature)
    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction)

    // DELETE (Single)
    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.delete(transaction)

}