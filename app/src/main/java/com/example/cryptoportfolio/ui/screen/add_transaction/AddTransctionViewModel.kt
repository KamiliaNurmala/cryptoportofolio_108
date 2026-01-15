package com.example.cryptoportfolio.ui.screen.add_transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.local.entity.Transaction
import com.example.cryptoportfolio.data.repository.AuthRepository
import com.example.cryptoportfolio.data.repository.PortfolioRepository
import com.example.cryptoportfolio.util.Constants
import com.example.cryptoportfolio.util.DateFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val portfolioRepository: PortfolioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var coinId by mutableStateOf("")
        private set
    var coinSymbol by mutableStateOf("")
        private set
    var coinName by mutableStateOf("")
        private set
    var amount by mutableStateOf("")
        private set
    var buyPrice by mutableStateOf("")
        private set
    var transactionType by mutableStateOf(Constants.TYPE_BUY)
        private set
    var notes by mutableStateOf("")
        private set
    var buyDate by mutableStateOf(DateFormatter.getCurrentTimestamp())
        private set

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun updateCoin(id: String, symbol: String, name: String) {
        coinId = id
        coinSymbol = symbol
        coinName = name
    }

    fun updateAmount(newAmount: String) {
        amount = newAmount
    }

    fun updateBuyPrice(newPrice: String) {
        buyPrice = newPrice
    }

    fun updateTransactionType(type: String) {
        transactionType = type
    }

    fun updateNotes(newNotes: String) {
        notes = newNotes
    }

    fun updateBuyDate(timestamp: Long) {
        buyDate = timestamp
    }

    fun saveTransaction() {
        viewModelScope.launch {
            if (!validateInput()) return@launch

            _saveState.value = SaveState.Loading

            try {
                val userId = authRepository.currentUserId.first() ?: run {
                    _saveState.value = SaveState.Error("User not logged in")
                    return@launch
                }

                val amountDouble = amount.toDouble()
                val priceDouble = buyPrice.toDouble()

                // --- 1. VALIDASI SALDO SELL (Tetap Wajib Ada) ---
                if (transactionType == Constants.TYPE_SELL) {
                    val existingPortfolio = portfolioRepository.getPortfolioByCoinId(coinId, userId).first()

                    if (existingPortfolio == null) {
                        _saveState.value = SaveState.Error("You don't own any $coinSymbol to sell!")
                        return@launch
                    }
                    if (existingPortfolio.amount < amountDouble) {
                        _saveState.value = SaveState.Error("Insufficient balance! You only have ${existingPortfolio.amount} $coinSymbol")
                        return@launch
                    }
                }

                // --- 2. SIMPAN TRANSAKSI ---
                val transaction = Transaction(
                    userId = userId,
                    coinId = coinId,
                    coinSymbol = coinSymbol,
                    coinName = coinName,
                    type = transactionType,
                    amount = amountDouble,
                    price = priceDouble,
                    date = buyDate,
                    notes = notes
                )
                portfolioRepository.insertTransaction(transaction)

                // --- 3. RECALCULATE (Self-Correcting Ledger) ---
                recalculatePortfolio(userId, coinId, coinSymbol, coinName)

                _saveState.value = SaveState.Success
                clearFields()

            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save")
            }
        }
    }

    private suspend fun recalculatePortfolio(userId: Int, coinId: String, symbol: String, name: String) {
        // Ambil semua history
        val allTransactions = portfolioRepository.getTransactionsByCoinId(coinId, userId).first()

        // PENTING: Urutkan dari Terlama ke Terbaru (Replay History)
        val sortedTransactions = allTransactions.sortedBy { it.date }

        var currentAmount = 0.0
        var currentAvgPrice = 0.0

        // 1. HITUNG DULU SEMUANYA (LOOPING)
        sortedTransactions.forEach { tx ->
            if (tx.type == Constants.TYPE_BUY) {
                // Rumus Weighted Average
                val totalCost = (currentAmount * currentAvgPrice) + (tx.amount * tx.price)
                currentAmount += tx.amount

                if (currentAmount > 0) {
                    currentAvgPrice = totalCost / currentAmount
                }
            } else {
                // SELL: Cuma kurangi jumlah, Harga rata-rata JANGAN berubah
                // (Ini sekarang sudah di luar blok IF BUY, jadi BENAR)
                currentAmount -= tx.amount
            }
        }
        // <-- Loop selesai di sini.

        // 2. BARU SIMPAN KE DATABASE (SETELAH LOOP SELESAI)
        val existingPortfolio = portfolioRepository.getPortfolioByCoinId(coinId, userId).first()

        // Handle sisa 0 (kompensasi floating point error)
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
                    buyDate = System.currentTimeMillis(), notes = "Calculated from History"
                )
                portfolioRepository.insertPortfolio(newPortfolio)
            }
        }
    }

    private fun validateInput(): Boolean {
        if (coinId.isBlank()) {
            _saveState.value = SaveState.Error("Please select a coin")
            return false
        }
        if (amount.isBlank() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            _saveState.value = SaveState.Error("Please enter valid amount")
            return false
        }
        if (buyPrice.isBlank() || buyPrice.toDoubleOrNull() == null || buyPrice.toDouble() <= 0) {
            _saveState.value = SaveState.Error("Please enter valid price")
            return false
        }
        return true
    }

    private fun clearFields() {
        coinId = ""
        coinSymbol = ""
        coinName = ""
        amount = ""
        buyPrice = ""
        transactionType = Constants.TYPE_BUY
        notes = ""
        buyDate = DateFormatter.getCurrentTimestamp()
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}