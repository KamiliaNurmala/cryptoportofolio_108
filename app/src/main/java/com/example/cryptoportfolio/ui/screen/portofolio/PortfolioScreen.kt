package com.example.cryptoportfolio.ui.screen.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoportfolio.data.local.entity.Portfolio
import com.example.cryptoportfolio.data.local.entity.Transaction // Import Transaction
import com.example.cryptoportfolio.util.Constants
import com.example.cryptoportfolio.util.CurrencyFormatter
import com.example.cryptoportfolio.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Int) -> Unit,
    viewModel: PortfolioViewModel,
//    modifier: Modifier = Modifier
) {
    val portfolioUiState by viewModel.portfolioUiState.collectAsState()
    val portfolioWithPrices by viewModel.portfolioWithPrices.collectAsState()
    val transactionList by viewModel.transactionUiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog States Delete
    var showDeleteDialog by remember { mutableStateOf(false) }
    var portfolioToDelete by remember { mutableStateOf<Portfolio?>(null) }

    // Dialog States Transaction Delete
    var showDeleteTransactionDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Portfolio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Refresh button for manual price update (REQ-PORT-06)
                    IconButton(onClick = { viewModel.refreshPrices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Prices")
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 2. Add Tabs to switch views
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Holdings") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("History") }
                )
            }

            // 3. Show content based on Tab
            Box(modifier = Modifier.fillMaxSize()) {  // ADD THIS BOX WRAPPER
                if (selectedTab == 0) {
                    // === HOLDINGS VIEW WITH REAL-TIME PRICES ===
                    if (isLoading && portfolioWithPrices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (portfolioUiState.portfolioList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No portfolio items",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "Total Holdings: ${portfolioUiState.portfolioList.size} coins",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            items(portfolioWithPrices) { portfolioWithPrice ->
                                PortfolioCardWithPrice(
                                    portfolioWithPrice = portfolioWithPrice,
                                    onDelete = {
                                        portfolioToDelete = portfolioWithPrice.portfolio
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // === NEW HISTORY VIEW ===
                    if (transactionList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transaction history",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 80.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(transactionList) { transaction ->
                                TransactionCard(
                                    transaction = transaction,
                                    onDelete = {
                                        transactionToDelete = transaction
                                        showDeleteTransactionDialog = true
                                    },
                                    onEdit = {
                                        onNavigateToEditTransaction(transaction.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ... (Keep existing Delete Dialog code here) ...
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Portfolio Item") },
            text = {
                Text("Are you sure you want to delete ${portfolioToDelete?.coinSymbol} from your portfolio?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        portfolioToDelete?.let { viewModel.deletePortfolio(it) }
                        showDeleteDialog = false
                        portfolioToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteTransactionDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTransactionDialog = false },
            title = { Text("Delete History Log") },
            text = { Text("Remove this transaction record from history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        showDeleteTransactionDialog = false
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTransactionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Composable
fun PortfolioCard(
    portfolio: Portfolio,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = portfolio.coinName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = portfolio.coinSymbol,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Row to hold both Edit and Delete buttons
                Row {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatNumber(portfolio.amount, 8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Buy Price",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatUSD(portfolio.buyPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Investment",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatUSD(portfolio.amount * portfolio.buyPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Purchase Date",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateFormatter.formatDate(portfolio.buyDate, "dd MMM yyyy"),
                        fontSize = 14.sp
                    )
                }
            }

            if (portfolio.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${portfolio.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Transaction Card for History
@Composable
fun TransactionCard(
    transaction: Transaction,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val isBuy = transaction.type == Constants.TYPE_BUY
    val color = if (isBuy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val icon = if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    // Hitung Total Uang
    val totalValue = transaction.amount * transaction.price

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon (Panah)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.2f), MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Kolom Tengah (Nama Koin & Harga saat itu)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.coinSymbol,
                    fontWeight = FontWeight.Bold
                )
                // Menampilkan Tanggal
                Text(
                    text = DateFormatter.formatDate(transaction.date, "dd MMM, HH:mm"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Menampilkan Harga Per Koin (kecil saja)
                Text(
                    text = "@ ${CurrencyFormatter.formatUSD(transaction.price)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 3. Kolom Kanan (Jumlah Koin & Total Uang)
            Column(horizontalAlignment = Alignment.End) {
                // Jumlah Koin (+1.000 ETH)
                Text(
                    text = (if (isBuy) "+" else "-") + CurrencyFormatter.formatNumber(transaction.amount, 4),
                    fontWeight = FontWeight.Bold,
                    color = color
                )

                // Total Uang ($2,990.00) <-- INI YANG KAMU SARANKAN
                Text(
                    text = CurrencyFormatter.formatUSD(totalValue),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 4. Tombol Edit
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Log",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 5. Tombol Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete Log",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Portfolio Card with real-time prices and P/L (REQ-PORT-04)
 * Shows: coin name, symbol, amount, buy price, current price, current value, P/L with color
 */
@Composable
fun PortfolioCardWithPrice(
    portfolioWithPrice: PortfolioWithPrice,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portfolio = portfolioWithPrice.portfolio
    val isProfit = portfolioWithPrice.profitLoss >= 0
    val plColor = if (isProfit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Coin name & Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = portfolio.coinName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = portfolio.coinSymbol,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Amount & Current Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Amount",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatNumber(portfolio.amount, 8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Current Price",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatUSD(portfolioWithPrice.currentPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Buy Price (Average) & Current Value
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Avg Buy Price",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatUSD(portfolio.buyPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Current Value",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatUSD(portfolioWithPrice.currentValue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: Profit/Loss with color indicator (REQ-PORT-02)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profit/Loss",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = plColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${CurrencyFormatter.formatUSD(portfolioWithPrice.profitLoss)} (${CurrencyFormatter.formatPercentage(portfolioWithPrice.profitLossPercentage)})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = plColor
                    )
                }
            }
        }
    }
}