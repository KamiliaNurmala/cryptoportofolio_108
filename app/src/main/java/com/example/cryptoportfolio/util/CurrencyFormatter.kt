package com.example.cryptoportfolio.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    fun formatUSD(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(amount)
    }

    fun formatIDR(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount)
    }

    fun formatNumber(number: Double, decimals: Int = 2): String {
        return String.format("%.${decimals}f", number)
    }

    fun formatPercentage(percentage: Double): String {
        val sign = if (percentage >= 0) "+" else ""
        return "$sign${String.format("%.2f", percentage)}%"
    }
}