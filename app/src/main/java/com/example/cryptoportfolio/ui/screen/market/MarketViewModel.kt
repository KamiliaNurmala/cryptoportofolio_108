package com.example.cryptoportfolio.ui.screen.market

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.remote.model.MarketCoin
import com.example.cryptoportfolio.data.repository.CryptoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarketViewModel(
    private val cryptoRepository: CryptoRepository
) : ViewModel() {

    private val _marketCoins = MutableStateFlow<List<MarketCoin>>(emptyList())

    private val _filteredCoins = MutableStateFlow<List<MarketCoin>>(emptyList())
    val filteredCoins: StateFlow<List<MarketCoin>> = _filteredCoins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    init {
        loadMarketData()
    }

    fun loadMarketData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            cryptoRepository.getMarkets().collect { coins ->
                _marketCoins.value = coins
                _filteredCoins.value = coins
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        filterCoins()
    }

    private fun filterCoins() {
        _filteredCoins.value = if (searchQuery.isBlank()) {
            _marketCoins.value
        } else {
            _marketCoins.value.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||  // ✅ Add ignoreCase
                        it.symbol.contains(searchQuery, ignoreCase = true)
            }
        }
    }
}