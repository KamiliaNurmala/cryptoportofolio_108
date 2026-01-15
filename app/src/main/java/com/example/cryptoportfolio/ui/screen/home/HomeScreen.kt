package com.example.cryptoportfolio.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoportfolio.util.CurrencyFormatter
import com.example.cryptoportfolio.ui.components.PortfolioItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel,
//    modifier: Modifier = Modifier
) {
    val homeUiState by viewModel.homeUiState.collectAsState()
    val portfolioWithPrices by viewModel.portfolioWithPrices.collectAsState()
    val totalValue by viewModel.totalValue.collectAsState()
    val totalProfitLoss by viewModel.totalProfitLoss.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load prices on first composition
    LaunchedEffect(homeUiState.portfolioList) {
        viewModel.refreshPrices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CryptoFolio") },
                actions = {
                    // Refresh button for manual price update (REQ-PORT-06)
                    IconButton(onClick = { viewModel.refreshPrices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Prices")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToPortfolio,
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Portfolio") },
                    label = { Text("Portfolio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToMarket,
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Market") },
                    label = { Text("Market") }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                // Total Value Card
                item {
                    TotalValueCard(
                        totalValue = totalValue,
                        totalProfitLoss = totalProfitLoss
                    )
                }

                // Portfolio Section Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Portfolio",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToPortfolio) {
                            Text("See All")
                        }
                    }
                }

                // Portfolio Items
                if (portfolioWithPrices.isEmpty()) {
                    item {
                        EmptyPortfolioCard(onAddTransaction = onNavigateToAddTransaction)
                    }
                } else {
                    items(portfolioWithPrices.take(5)) { portfolioWithPrice ->
                        PortfolioItem(
                            coinSymbol = portfolioWithPrice.portfolio.coinSymbol,
                            amount = portfolioWithPrice.portfolio.amount,
                            currentValue = portfolioWithPrice.currentValue,
                            profitLoss = portfolioWithPrice.profitLoss,
                            profitLossPercentage = portfolioWithPrice.profitLossPercentage,
                            modifier = Modifier.clickable { onNavigateToPortfolio() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TotalValueCard(
    totalValue: Double,
    totalProfitLoss: Double,
    modifier: Modifier = Modifier
) {
    val idrRate = 15800.0  // Approximate USD to IDR rate

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Total Portfolio Value",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))

            // USD (Primary)
            Text(
                text = CurrencyFormatter.formatUSD(totalValue),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // IDR (Secondary) - REQ-CALC-08
            Text(
                text = CurrencyFormatter.formatIDR(totalValue * idrRate),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            // Profit/Loss Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (totalProfitLoss >= 0) Icons.Default.TrendingUp
                    else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = if (totalProfitLoss >= 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))

                // Show P/L in both currencies
                Column {
                    Text(
                        text = CurrencyFormatter.formatUSD(totalProfitLoss),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (totalProfitLoss >= 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = CurrencyFormatter.formatIDR(totalProfitLoss * idrRate),
                        fontSize = 12.sp,
                        color = if (totalProfitLoss >= 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Disclaimer (5.2 Safety Requirements)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Prices are for reference only",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyPortfolioCard(
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No portfolio yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start by adding your first transaction",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddTransaction) {
                Text("Add Transaction")
            }
        }
    }
}