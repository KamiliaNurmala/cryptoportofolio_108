package com.example.cryptoportfolio.ui.screen.add_transaction

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptoportfolio.util.Constants
import com.example.cryptoportfolio.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: AddTransactionViewModel,
    modifier: Modifier = Modifier
) {
    val saveState by viewModel.saveState.collectAsState()
    var showCoinPicker by remember { mutableStateOf(false) }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                onSaveSuccess()
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Select Coin Button
            OutlinedButton(
                onClick = { showCoinPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.coinName.isBlank()) "Select Coin"
                    else "${viewModel.coinName} (${viewModel.coinSymbol})"
                )
            }

            // Transaction Type
            Text(
                text = "Transaction Type",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = viewModel.transactionType == Constants.TYPE_BUY,
                    onClick = { viewModel.updateTransactionType(Constants.TYPE_BUY) },
                    label = { Text("Buy") },
                    leadingIcon = if (viewModel.transactionType == Constants.TYPE_BUY) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = viewModel.transactionType == Constants.TYPE_SELL,
                    onClick = { viewModel.updateTransactionType(Constants.TYPE_SELL) },
                    label = { Text("Sell") },
                    leadingIcon = if (viewModel.transactionType == Constants.TYPE_SELL) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }

            // Amount
            OutlinedTextField(
                value = viewModel.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("Amount") },
                leadingIcon = {
                    Icon(Icons.Default.Numbers, contentDescription = "Amount")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("How many coins") }
            )

            // Buy Price
            OutlinedTextField(
                value = viewModel.buyPrice,
                onValueChange = viewModel::updateBuyPrice,
                label = { Text("Price (USD)") },
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = "Price")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Price per coin in USD") }
            )

            // Date
            OutlinedTextField(
                value = DateFormatter.formatDate(viewModel.buyDate),
                onValueChange = {},
                label = { Text("Date") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date")
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes (Optional)") },
                leadingIcon = {
                    Icon(Icons.Default.Notes, contentDescription = "Notes")
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            // Error Message
            if (saveState is SaveState.Error) {
                Text(
                    text = (saveState as SaveState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = saveState !is SaveState.Loading
            ) {
                if (saveState is SaveState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Transaction", fontSize = 16.sp)
                }
            }
        }
    }

    // Coin Picker Dialog (Simple version - you can enhance this)
    if (showCoinPicker) {
        CoinPickerDialog(
            onDismiss = { showCoinPicker = false },
            onCoinSelected = { coinId, symbol, name ->
                viewModel.updateCoin(coinId, symbol, name)
                showCoinPicker = false
            }
        )
    }
}

@Composable
fun CoinPickerDialog(
    onDismiss: () -> Unit,
    onCoinSelected: (String, String, String) -> Unit
) {
    // Simple coin picker - top 10 coins
    val popularCoins = listOf(
        Triple("bitcoin", "BTC", "Bitcoin"),
        Triple("ethereum", "ETH", "Ethereum"),
        Triple("binancecoin", "BNB", "BNB"),
        Triple("cardano", "ADA", "Cardano"),
        Triple("solana", "SOL", "Solana"),
        Triple("ripple", "XRP", "XRP"),
        Triple("polkadot", "DOT", "Polkadot"),
        Triple("dogecoin", "DOGE", "Dogecoin"),
        Triple("avalanche-2", "AVAX", "Avalanche"),
        Triple("chainlink", "LINK", "Chainlink")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Coin") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                popularCoins.forEach { (id, symbol, name) ->
                    TextButton(
                        onClick = { onCoinSelected(id, symbol, name) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$name ($symbol)")
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}