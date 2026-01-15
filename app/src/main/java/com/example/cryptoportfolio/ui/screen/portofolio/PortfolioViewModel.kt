package com.example.cryptoportfolio.ui.screen.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.local.entity.Transaction
import com.example.cryptoportfolio.data.repository.AuthRepository
import com.example.cryptoportfolio.data.repository.CryptoRepository
import com.example.cryptoportfolio.data.repository.PortfolioRepository
import com.example.cryptoportfolio.util.Constants
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val cryptoRepository: CryptoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // 1. Existing Portfolio State
    val portfolioUiState: StateFlow<PortfolioUiState> = authRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            portfolioRepository.getAllPortfolio(userId)
        }
        .map { portfolios ->
            PortfolioUiState(portfolioList = portfolios)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = PortfolioUiState()
        )

    // 2. Transaction History State
    val transactionUiState: StateFlow<List<Transaction>> = authRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { userId ->
            portfolioRepository.getAllTransactions(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // 3. Portfolio with real-time prices (REQ-PORT-04)
    private val _portfolioWithPrices = MutableStateFlow<List<PortfolioWithPrice>>(emptyList())
    val portfolioWithPrices: StateFlow<List<PortfolioWithPrice>> = _portfolioWithPrices.asStateFlow()

    // 4. Loading state for refresh
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 5. Flag to prevent concurrent refresh calls
    private var isRefreshing = false
    private var hasInitiallyLoaded = false

    // 6. Auto-refresh once when portfolio first loads
    init {
        viewModelScope.launch {
            portfolioUiState.collect { state ->
                if (state.portfolioList.isNotEmpty() && !hasInitiallyLoaded) {
                    hasInitiallyLoaded = true
                    refreshPrices()
                } else if (state.portfolioList.isEmpty()) {
                    _portfolioWithPrices.value = emptyList()
                }
            }
        }
    }

    /**
     * Fetch current prices from CoinGecko API and calculate P/L (REQ-PORT-05, REQ-PORT-06)
     */
    fun refreshPrices() {
        // Prevent concurrent refresh calls (avoid API rate limiting)
        if (isRefreshing) return
        isRefreshing = true

        viewModelScope.launch {
            _isLoading.value = true

            // Get current userId
            val userId = authRepository.currentUserId.first()
            if (userId == null) {
                _isLoading.value = false
                isRefreshing = false
                return@launch
            }

            // Get fresh portfolio data from database (not from stale StateFlow)
            val portfolios = portfolioRepository.getAllPortfolio(userId).first()
            
            if (portfolios.isEmpty()) {
                _portfolioWithPrices.value = emptyList()
                _isLoading.value = false
                isRefreshing = false
                return@launch
            }

            val coinIds = portfolios.joinToString(",") { it.coinId }

            try {
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
                    _isLoading.value = false
                    isRefreshing = false
                }
            } catch (e: Exception) {
                // If API fails, still show portfolio with 0 prices (graceful degradation)
                _portfolioWithPrices.value = portfolios.map { portfolio ->
                    PortfolioWithPrice(
                        portfolio = portfolio,
                        currentPrice = 0.0,
                        currentValue = 0.0,
                        profitLoss = 0.0,
                        profitLossPercentage = 0.0
                    )
                }
                _isLoading.value = false
                isRefreshing = false
            }
        }
    }

    fun deletePortfolio(portfolio: Portfolio) {
        viewModelScope.launch {
            portfolioRepository.deletePortfolio(portfolio)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            // Store transaction info before deleting
            val userId = transaction.userId
            val coinId = transaction.coinId
            val coinSymbol = transaction.coinSymbol
            val coinName = transaction.coinName

            // Delete the transaction first
            portfolioRepository.deleteTransaction(transaction)

            // Then recalculate portfolio for this coin (REQ-TRANS-11)
            recalculatePortfolio(userId, coinId, coinSymbol, coinName)
        }
    }

    /**
     * Recalculates portfolio based on remaining transaction history.
     * Replays all transactions chronologically to compute correct amount & avg price.
     */
    private suspend fun recalculatePortfolio(userId: Int, coinId: String, symbol: String, name: String) {
        // Get all remaining transactions for this coin
        val allTransactions = portfolioRepository.getTransactionsByCoinId(coinId, userId).first()

        // Sort by date (oldest first) to replay history correctly
        val sortedTransactions = allTransactions.sortedBy { it.date }

        var currentAmount = 0.0
        var currentAvgPrice = 0.0

        // Replay all transactions
        sortedTransactions.forEach { tx ->
            if (tx.type == Constants.TYPE_BUY) {
                // Weighted average calculation for BUY
                val totalCost = (currentAmount * currentAvgPrice) + (tx.amount * tx.price)
                currentAmount += tx.amount
                if (currentAmount > 0) {
                    currentAvgPrice = totalCost / currentAmount
                }
            } else {
                // SELL: Only reduce amount, don't change avg price
                currentAmount -= tx.amount
            }
        }

        // Update or delete portfolio based on remaining amount
        val existingPortfolio = portfolioRepository.getPortfolioByCoinId(coinId, userId).first()

        if (currentAmount <= 0.00000001) {
            // No holdings left, delete portfolio entry
            if (existingPortfolio != null) {
                portfolioRepository.deletePortfolio(existingPortfolio)
            }
        } else {
            // Update portfolio with recalculated values
            if (existingPortfolio != null) {
                portfolioRepository.updatePortfolio(
                    existingPortfolio.copy(
                        amount = currentAmount,
                        buyPrice = currentAvgPrice
                    )
                )
            } else {
                // Edge case: portfolio was deleted but transactions exist
                val newPortfolio = Portfolio(
                    userId = userId,
                    coinId = coinId,
                    coinSymbol = symbol,
                    coinName = name,
                    amount = currentAmount,
                    buyPrice = currentAvgPrice,
                    buyDate = System.currentTimeMillis(),
                    notes = "Recalculated from history"
                )
                portfolioRepository.insertPortfolio(newPortfolio)
            }
        }
    }
}

data class PortfolioUiState(
    val portfolioList: List<Portfolio> = emptyList()
)

/**
 * Portfolio item with real-time price data (REQ-PORT-04)
 */
data class PortfolioWithPrice(
    val portfolio: Portfolio,
    val currentPrice: Double,
    val currentValue: Double,
    val profitLoss: Double,
    val profitLossPercentage: Double
)