package com.example.cryptoportfolio.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoportfolio.util.CurrencyFormatter

@Composable
fun PortfolioItem(
    coinSymbol: String,
    amount: Double,
    currentValue: Double,
    profitLoss: Double,
    profitLossPercentage: Double,
    modifier: Modifier = Modifier
) {
    val isProfit = profitLoss >= 0
    val plColor = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coin Info
            Column {
                Text(
                    text = coinSymbol.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${CurrencyFormatter.formatNumber(amount, 4)} coins",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Value Info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CurrencyFormatter.formatUSD(currentValue),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // P/L with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = plColor
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = CurrencyFormatter.formatPercentage(profitLossPercentage),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = plColor
                    )
                }
            }
        }
    }
}