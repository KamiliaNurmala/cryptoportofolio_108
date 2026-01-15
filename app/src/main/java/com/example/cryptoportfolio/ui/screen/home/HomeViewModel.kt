package com.example.cryptoportfolio.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.repository.AuthRepository
import com.example.cryptoportfolio.data.repository.CryptoRepository
import com.example.cryptoportfolio.data.repository.PortfolioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val cryptoRepository: CryptoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUserId = MutableStateFlow<Int?>(null)

    init {
        viewModelScope.launch {
            authRepository.currentUserId.collect { userId ->
                _currentUserId.value = userId
            }
        }
    }

    val homeUiState: StateFlow<HomeUiState> = _currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            portfolioRepository.getAllPortfolio(userId)
        }
        .map { portfolios ->
            HomeUiState(portfolioList = portfolios)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = HomeUiState()
        )

    private val _portfolioWithPrices = MutableStateFlow<List<PortfolioWithPrice>>(emptyList())
    val portfolioWithPrices: StateFlow<List<PortfolioWithPrice>> = _portfolioWithPrices.asStateFlow()

    private val _totalValue = MutableStateFlow(0.0)
    val totalValue: StateFlow<Double> = _totalValue.asStateFlow()

    private val _totalProfitLoss = MutableStateFlow(0.0)
    val totalProfitLoss: StateFlow<Double> = _totalProfitLoss.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun refreshPrices() {
        viewModelScope.launch {
            val portfolios = homeUiState.value.portfolioList
            if (portfolios.isEmpty()) {
                _portfolioWithPrices.value = emptyList() // Clears the list
                _totalValue.value = 0.0                  // Resets total
                _totalProfitLoss.value = 0.0             // Resets P/L
                return@launch
            }

            _isLoading.value = true

            val coinIds = portfolios.joinToString(",") { it.coinId }

            cryptoRepository.getSimplePrices(coinIds).collect { pricesMap ->
                val portfolioWithPricesList = portfolios.map { portfolio ->
                    val priceData = pricesMap[portfolio.coinId]
                    val currentPrice = priceData?.get("usd") ?: 0.0
                    val currentValue = portfolio.amount * currentPrice
                    val investment = portfolio.amount * portfolio.buyPrice
                    val profitLoss = currentValue - investment
                    val profitLossPercentage = if (investment > 0) {
                        (profitLoss / investment) * 100
                    } else 0.0

                    PortfolioWithPrice(
                        portfolio = portfolio,
                        currentPrice = currentPrice,
                        currentValue = currentValue,
                        profitLoss = profitLoss,
                        profitLossPercentage = profitLossPercentage
                    )
                }

                _portfolioWithPrices.value = portfolioWithPricesList
                _totalValue.value = portfolioWithPricesList.sumOf { it.currentValue }
                _totalProfitLoss.value = portfolioWithPricesList.sumOf { it.profitLoss }
                _isLoading.value = false
            }
        }
    }

    
}

data class HomeUiState(
    val portfolioList: List<Portfolio> = emptyList()
)

data class PortfolioWithPrice(
    val portfolio: Portfolio,
    val currentPrice: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val profitLossPercentage: Double
)