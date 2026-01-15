package com.example.cryptoportfolio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cryptoportfolio.data.local.dao.PortfolioDao
import com.example.cryptoportfolio.data.local.dao.TransactionDao
import com.example.cryptoportfolio.data.local.dao.UserDao
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.local.entity.Transaction
import com.example.cryptoportfolio.data.local.entity.User

@Database(
    entities = [User::class, Portfolio::class, Transaction::class],
    version = 2,  // ← UBAH DARI 1 JADI 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crypto_portfolio_database"
                )
                    .fallbackToDestructiveMigration()  // ← Ini akan drop & recreate database
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}