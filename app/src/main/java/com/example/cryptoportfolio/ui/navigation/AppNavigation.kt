package com.example.cryptoportfolio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cryptoportfolio.di.AppContainer
import com.example.cryptoportfolio.ui.AddTransactionViewModelFactory
import com.example.cryptoportfolio.ui.AuthViewModelFactory
import com.example.cryptoportfolio.ui.CoinDetailViewModelFactory
import com.example.cryptoportfolio.ui.EditTransactionViewModelFactory
import com.example.cryptoportfolio.ui.HomeViewModelFactory
import com.example.cryptoportfolio.ui.MarketViewModelFactory
import com.example.cryptoportfolio.ui.PortfolioViewModelFactory
import com.example.cryptoportfolio.ui.screen.add_transaction.AddTransactionScreen
import com.example.cryptoportfolio.ui.screen.add_transaction.AddTransactionViewModel
import com.example.cryptoportfolio.ui.screen.auth.AuthViewModel
import com.example.cryptoportfolio.ui.screen.auth.LoginScreen
import com.example.cryptoportfolio.ui.screen.auth.RegisterScreen
import com.example.cryptoportfolio.ui.screen.detail.CoinDetailScreen
import com.example.cryptoportfolio.ui.screen.detail.CoinDetailViewModel
import com.example.cryptoportfolio.ui.screen.edit_transaction.EditTransactionScreen
// ↓↓↓ THIS IMPORT WAS MISSING ↓↓↓
import com.example.cryptoportfolio.ui.screen.edit_transaction.EditTransactionViewModel
import com.example.cryptoportfolio.ui.screen.home.HomeScreen
import com.example.cryptoportfolio.ui.screen.home.HomeViewModel
import com.example.cryptoportfolio.ui.screen.market.MarketScreen
import com.example.cryptoportfolio.ui.screen.market.MarketViewModel
import com.example.cryptoportfolio.ui.screen.portfolio.PortfolioScreen
import com.example.cryptoportfolio.ui.screen.portfolio.PortfolioViewModel
import com.example.cryptoportfolio.ui.screen.profile.ProfileScreen

@Composable
fun AppNavigation(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Check if user is logged in
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appContainer.authRepository)
    )

    val currentUserId by appContainer.authRepository.currentUserId.collectAsState(initial = null)

    val startDestination = if (currentUserId != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Main Screens
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    appContainer.portfolioRepository,
                    appContainer.cryptoRepository,
                    appContainer.authRepository
                )
            )

            HomeScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToPortfolio = {
                    navController.navigate(Screen.Portfolio.route)
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                viewModel = homeViewModel
            )
        }

        composable(Screen.Portfolio.route) {
            val portfolioViewModel: PortfolioViewModel = viewModel(
                factory = PortfolioViewModelFactory(
                    appContainer.portfolioRepository,
                    appContainer.cryptoRepository,
                    appContainer.authRepository
                )
            )

            PortfolioScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToEditTransaction = { transactionId ->
                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))
                },
                viewModel = portfolioViewModel
            )
        }

        composable(Screen.AddTransaction.route) {
            val addTransactionViewModel: AddTransactionViewModel = viewModel(
                factory = AddTransactionViewModelFactory(
                    appContainer.portfolioRepository,
                    appContainer.authRepository
                )
            )

            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.popBackStack()
                },
                viewModel = addTransactionViewModel
            )
        }

        composable(Screen.Market.route) {
            val marketViewModel: MarketViewModel = viewModel(
                factory = MarketViewModelFactory(appContainer.cryptoRepository)
            )

            MarketScreen(
                onNavigateBack = { navController.popBackStack() },
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                },
                viewModel = marketViewModel
            )
        }

        composable(Screen.Profile.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(appContainer.authRepository)
            )

            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true } // Clear back stack
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(
            route = Screen.CoinDetail.route,
            arguments = listOf(navArgument("coinId") { type = NavType.StringType })
        ) { backStackEntry ->
            val coinId = backStackEntry.arguments?.getString("coinId") ?: ""
            val coinDetailViewModel: CoinDetailViewModel = viewModel(
                factory = CoinDetailViewModelFactory(
                    appContainer.cryptoRepository,
                    coinId
                )
            )

            CoinDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = coinDetailViewModel
            )
        }

        composable(
            route = Screen.EditTransaction.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getInt("transactionId") ?: 0

            val viewModel: EditTransactionViewModel = viewModel(
                factory = EditTransactionViewModelFactory(
                    appContainer.portfolioRepository,
                    transactionId
                )
            )

            EditTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onUpdateSuccess = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}