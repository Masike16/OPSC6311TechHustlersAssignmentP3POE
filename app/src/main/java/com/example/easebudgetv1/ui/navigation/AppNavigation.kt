package com.example.easebudgetv1.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.easebudgetv1.ui.screens.*
import com.example.easebudgetv1.utils.SessionManager
import com.example.easebudgetv1.viewmodel.AuthViewModel

/* 
 * this file handles all the navigation routs for the app.
 * basically uses the NavHost to swap screens based on where the user clicks
 * 
 * References:
 * Google (2024) 'Navigation with Compose', Android Developers. Available at: https://developer.android.com/jetpack/compose/navigation (Accessed: 25 May 2024)
 * 
 * we used a Scaffold here to show the bottom bar only on the main screens to keep it clean.
 */

sealed class Screen(val route: String, val title: String) {
    object Auth : Screen("auth", "Welcome")
    object Guide : Screen("guide", "Guide")
    object AdminLogin : Screen("admin_login", "Admin")
    object AdminDashboard : Screen("admin_dashboard", "Console")
    object Dashboard : Screen("dashboard", "Home")
    object Transactions : Screen("transactions", "Transactions")
    object Budget : Screen("budget", "Budget")
    object Goals : Screen("goals", "Rewards")
    object Reports : Screen("reports", "Analytics")
    object Settings : Screen("settings", "Settings")
    object SharedAccounts : Screen("shared_accounts", "Family")
}

@Composable
fun AppNavigation(
    sessionManager: SessionManager, // Injected from MainActivity
    startDestination: String = Screen.Auth.route
) {
    val navController = rememberNavController()
    var currentUserId by rememberSaveable { mutableLongStateOf(0L) }
    var currentUserEmail by rememberSaveable { mutableStateOf("") }
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val bottomNavScreens = remember {
        listOf(
            Screen.Dashboard,
            Screen.Transactions,
            Screen.Budget,
            Screen.Goals,
            Screen.Reports
        )
    }
    val bottomNavRoutes = remember { bottomNavScreens.map { it.route }.toSet() }
    
    // only show the bottom bar if the user is logged in and on a main screen
    val showBottomBar by remember(currentDestination, currentUserId) {
        derivedStateOf { 
            currentDestination?.route in bottomNavRoutes && currentUserId != 0L 
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                val icon = when (screen) {
                                    Screen.Dashboard -> Icons.Default.Home
                                    Screen.Transactions -> Icons.AutoMirrored.Filled.ListAlt
                                    Screen.Budget -> Icons.Default.AccountBalanceWallet
                                    Screen.Goals -> Icons.Default.Stars
                                    Screen.Reports -> Icons.Default.BarChart
                                    else -> Icons.Default.Home
                                }
                                Icon(
                                    imageVector = icon,
                                    modifier = Modifier.size(24.dp),
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = { userId: Long, email: String -> 
                        currentUserId = userId
                        currentUserEmail = email
                        if (sessionManager.isHelpDisabled(userId)) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Guide.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        }
                    }, 
                    onAdminLoginClick = { navController.navigate(Screen.AdminLogin.route) }
                )
            }

            composable(Screen.Guide.route) {
                val authViewModel: AuthViewModel = hiltViewModel()
                GuideScreen(
                    userId = currentUserId,
                    onDismiss = { showAgain: Boolean ->
                        // Persist preference to both Session and Database
                        authViewModel.updateGuidePreference(currentUserId, showAgain)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Guide.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.AdminLogin.route) {
                AdminLoginScreen(
                    onAdminLoginSuccess = {
                        // Navigate to Dashboard but keep Auth in backstack so we can go back
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.AdminLogin.route) { inclusive = true }
                        }
                    },
                    onBackClick = { 
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(onBackClick = { 
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                })
            }
            
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    userId = currentUserId,
                    onAddTransactionClick = { 
                        navController.navigate(Screen.Transactions.route)
                    },
                    onSharedAccountsClick = { navController.navigate(Screen.SharedAccounts.route) },
                    onProfileClick = { navController.navigate(Screen.Settings.route) },
                    onSeeAllTransactionsClick = { navController.navigate(Screen.Transactions.route) }
                )
            }
            
            composable(Screen.Transactions.route) {
                TransactionsScreen(userId = currentUserId)
            }
            
            composable(Screen.Budget.route) {
                BudgetScreen(userId = currentUserId)
            }
            
            composable(Screen.Goals.route) {
                GoalsScreen(userId = currentUserId)
            }
            
            composable(Screen.Reports.route) {
                ReportsScreen(userId = currentUserId)
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    userId = currentUserId,
                    onLogout = {
                        sessionManager.logout()
                        currentUserId = 0L
                        currentUserEmail = ""
                        navController.navigate(Screen.Auth.route) {
                            // Clear entire backstack to prevent back navigation after logout
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onShowGuide = { navController.navigate(Screen.Guide.route) }
                )
            }
            
            composable(Screen.SharedAccounts.route) {
                SharedAccountsScreen(userId = currentUserId, userEmail = currentUserEmail)
            }
        }
    }
}
