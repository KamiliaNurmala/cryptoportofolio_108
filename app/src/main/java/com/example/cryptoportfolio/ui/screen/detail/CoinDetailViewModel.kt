package com.example.cryptoportfolio.ui.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoportfolio.data.remote.model.CoinDetail
import com.example.cryptoportfolio.data.repository.CryptoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoinDetailViewModel(
    private val cryptoRepository: CryptoRepository,
    private val coinId: String
) : ViewModel() {

    private val _coinDetail = MutableStateFlow<CoinDetail?>(null)
    val coinDetail: StateFlow<CoinDetail?> = _coinDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCoinDetail()
    }

    fun loadCoinDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            cryptoRepository.getCoinDetail(coinId).collect { detail ->
                _coinDetail.value = detail
                _isLoading.value = false
            }
        }
    }
}