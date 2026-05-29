package com.example.tallybook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tallybook.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddTransaction : Screen("add_transaction")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object MonthlyHistory : Screen("monthly_history")
}

@Composable
fun TallyBookNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToMonthlyHistory = {
                    navController.navigate(Screen.MonthlyHistory.route)
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.MonthlyHistory.route) {
            MonthlyHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}