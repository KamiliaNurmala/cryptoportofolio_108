package com.example.cryptoportfolio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cryptoportfolio.data.repository.AuthRepository
import com.example.cryptoportfolio.data.repository.CryptoRepository
import com.example.cryptoportfolio.data.repository.PortfolioRepository
import com.example.cryptoportfolio.ui.screen.add_transaction.AddTransactionViewModel
import com.example.cryptoportfolio.ui.screen.auth.AuthViewModel
import com.example.cryptoportfolio.ui.screen.detail.CoinDetailViewModel
import com.example.cryptoportfolio.ui.screen.edit_transaction.EditTransactionViewModel
import com.example.cryptoportfolio.ui.screen.home.HomeViewModel
import com.example.cryptoportfolio.ui.screen.market.MarketViewModel
import com.example.cryptoportfolio.ui.screen.portfolio.PortfolioViewModel

class AuthViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HomeViewModelFactory(
    private val portfolioRepository: PortfolioRepository,
    private val cryptoRepository: CryptoRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(portfolioRepository, cryptoRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class PortfolioViewModelFactory(
    private val portfolioRepository: PortfolioRepository,
    private val cryptoRepository: CryptoRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PortfolioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PortfolioViewModel(portfolioRepository, cryptoRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AddTransactionViewModelFactory(
    private val portfolioRepository: PortfolioRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddTransactionViewModel(portfolioRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MarketViewModelFactory(
    private val cryptoRepository: CryptoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketViewModel(cryptoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CoinDetailViewModelFactory(
    private val cryptoRepository: CryptoRepository,
    private val coinId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoinDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoinDetailViewModel(cryptoRepository, coinId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class EditTransactionViewModelFactory(
    private val repository: PortfolioRepository,
    private val transactionId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditTransactionViewModel(repository, transactionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}