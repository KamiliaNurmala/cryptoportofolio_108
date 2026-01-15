package com.example.cryptoportfolio.ui.screen.edit_transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.local.entity.Transaction
import com.example.cryptoportfolio.data.repository.PortfolioRepository
import com.example.cryptoportfolio.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditTransactionViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val transactionId: Int
) : ViewModel() {

    // Form State
    var amount by mutableStateOf("")
    var price by mutableStateOf("")
    var notes by mutableStateOf("")
    var type by mutableStateOf(Constants.TYPE_BUY)

    // Store original data to know which coin to recalculate
    private var originalTransaction: Transaction? = null

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    init {
        loadTransaction()
    }

    // 1. Load data into the form
    private fun loadTransaction() {
        viewModelScope.launch {
            val transaction = portfolioRepository.getTransactionById(transactionId)
            if (transaction != null) {
                originalTransaction = transaction
                amount = transaction.amount.toString()
                price = transaction.price.toString()
                notes = transaction.notes
                type = transaction.type
            } else {
                _updateState.value = UpdateState.Error("Transaction not found")
            }
        }
    }

    // 2. Save Button Logic
    fun saveChanges() {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading

            try {
                val original = originalTransaction ?: return@launch

                // Validate inputs
                val newAmount = amount.toDoubleOrNull()
                val newPrice = price.toDoubleOrNull()

                if (newAmount == null || newAmount <= 0) {
                    _updateState.value = UpdateState.Error("Invalid amount")
                    return@launch
                }

                if (newPrice == null || newPrice < 0) {
                    _updateState.value = UpdateState.Error("Invalid price")
                    return@launch
                }

                // Update the Transaction in Database
                val updatedTransaction = original.copy(
                    amount = newAmount,
                    price = newPrice,
                    type = type,
                    notes = notes
                )
                portfolioRepository.updateTransaction(updatedTransaction)

                // Recalculate Portfolio (The Math Part)
                recalculatePortfolio(original.userId, original.coinId, original.coinSymbol, original.coinName)

                _updateState.value = UpdateState.Success
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Error updating")
            }
        }
    }

    // 3. Recalculate Portfolio Logic
    private suspend fun recalculatePortfolio(userId: Int, coinId: String, symbol: String, name: String) {
        val allTransactions = portfolioRepository.getTransactionsByCoinId(coinId, userId).first()

        // REPLAY HISTORY LOGIC
        val sortedTransactions = allTransactions.sortedBy { it.date }

        var currentAmount = 0.0
        var currentAvgPrice = 0.0

        sortedTransactions.forEach { tx ->
            if (tx.type == Constants.TYPE_BUY) {
                val totalCost = (currentAmount * currentAvgPrice) + (tx.amount * tx.price)
                currentAmount += tx.amount
                if (currentAmount > 0) {
                    currentAvgPrice = totalCost / currentAmount
                }
            } else {
                currentAmount -= tx.amount
            }
        }

        val existingPortfolio = portfolioRepository.getPortfolioByCoinId(coinId, userId).first()

        if (currentAmount <= 0.00000001) {
            if (existingPortfolio != null) {
                portfolioRepository.deletePortfolio(existingPortfolio)
            }
        } else {
            if (existingPortfolio != null) {
                portfolioRepository.updatePortfolio(
                    existingPortfolio.copy(
                        amount = currentAmount,
                        buyPrice = currentAvgPrice
                    )
                )
            } else {
                val newPortfolio = Portfolio(
                    userId = userId, coinId = coinId, coinSymbol = symbol,
                    coinName = name, amount = currentAmount, buyPrice = currentAvgPrice,
                    buyDate = System.currentTimeMillis(), notes = "Updated"
                )
                portfolioRepository.insertPortfolio(newPortfolio)
            }
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Loading : UpdateState()
    object Success : UpdateState()
    data class Error(val message: String) : UpdateState()
}