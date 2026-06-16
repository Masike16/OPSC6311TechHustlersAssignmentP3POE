package com.example.easebudgetv1.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.easebudgetv1.data.database.entities.AdminActionLog
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.utils.CurrencyFormatter
import com.example.easebudgetv1.utils.DateUtils
import com.example.easebudgetv1.viewmodel.AdminUserDetails
import com.example.easebudgetv1.viewmodel.AdminViewModel
import com.example.easebudgetv1.viewmodel.PlatformStats

/* 
 * This screen is for the admin dashboard where they can see stats and manage users
 * 
 * basically just a big list of users and some logs at the bottom. we use lazycolumn to keep it performant
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagedUsers = viewModel.usersPaged.collectAsLazyPagingItems()
    val pagedLogs = viewModel.adminLogsPaged.collectAsLazyPagingItems()
    
    var showUserDetails by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    // Handle system back button to exit user details if its open
    BackHandler(enabled = showUserDetails) {
        viewModel.deselectUser()
        showUserDetails = false
    }
    
    // Explicitly handle back navigation to login screen to prevent app exit
    BackHandler(enabled = !showUserDetails) {
        onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (showUserDetails && uiState.userDetails?.user != null) 
                            "User: ${uiState.userDetails?.user?.username}" 
                        else "System Console", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showUserDetails) {
                            viewModel.deselectUser()
                            showUserDetails = false
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (showUserDetails && uiState.userDetails != null) {
                ModernUserDetailsPanel(
                    userDetails = uiState.userDetails!!,
                    onBack = { 
                        viewModel.deselectUser()
                        showUserDetails = false 
                    },
                    onChangePassword = { showPasswordDialog = true },
                    onDeactivate = { viewModel.deactivateAccount(uiState.userDetails!!.user?.id ?: 0L) },
                    onActivate = { viewModel.activateAccount(uiState.userDetails!!.user?.id ?: 0L) },
                    onDelete = { viewModel.deleteAccount(uiState.userDetails!!.user?.id ?: 0L) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item(key = "platform_stats") {
                        ModernPlatformStatsCard(uiState.platformStats)
                    }
                    
                    item(key = "users_header") {
                        Text(
                            text = "Registered Users",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    // CRITICAL FIX: Custom keying to prevent placeholder collision between lists
                    items(
                        count = pagedUsers.itemCount,
                        key = { index -> 
                            pagedUsers.peek(index)?.let { "user_${it.id}" } ?: "user_placeholder_$index"
                        }
                    ) { index ->
                        pagedUsers[index]?.let { user ->
                            ModernUserCard(user = user, onClick = { 
                                viewModel.selectUser(user.id)
                                showUserDetails = true
                            })
                        }
                    }

                    item(key = "logs_header") {
                        Text(
                            text = "Security Logs",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    // CRITICAL FIX: Custom keying to prevent placeholder collision between lists
                    items(
                        count = pagedLogs.itemCount,
                        key = { index -> 
                            pagedLogs.peek(index)?.let { "log_${it.id}" } ?: "log_placeholder_$index"
                        }
                    ) { index ->
                        pagedLogs[index]?.let { log ->
                            ModernAdminLogItem(log)
                        }
                    }

                    if (pagedUsers.loadState.append is LoadState.Loading || pagedLogs.loadState.append is LoadState.Loading) {
                        item(key = "loading_indicator") {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }
    
    if (showPasswordDialog && uiState.selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Reset User Password", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.changeUserPassword(uiState.selectedUser!!.id, newPassword)
                        newPassword = ""
                        showPasswordDialog = false
                    }
                ) { Text("Update Password") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ModernPlatformStatsCard(stats: PlatformStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Network Health", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AdminStatItem("Users", stats.totalUsers.toString(), Icons.Default.People, Modifier.weight(1f))
                AdminStatItem("Volume", CurrencyFormatter.format(stats.totalMoneyTracked), Icons.Default.AccountBalanceWallet, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AdminStatItem("Avg/User", CurrencyFormatter.format(stats.avgSpendingPerUser), Icons.Default.QueryStats, Modifier.weight(1f))
                AdminStatItem("Badges", stats.totalBadgesEarned.toString(), Icons.Default.EmojiEvents, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun AdminStatItem(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun ModernUserCard(user: User, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(if (user.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (user.isActive) Icons.Default.Person else Icons.Default.PersonOff,
                    null,
                    tint = if (user.isActive) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.username, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (user.isAdmin) {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = CircleShape) {
                    Text("ADMIN", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Composable
fun ModernAdminLogItem(log: AdminActionLog) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(log.actionType, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                Text(DateUtils.formatDateTime(log.timestamp), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(log.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ModernUserDetailsPanel(
    userDetails: AdminUserDetails,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onDeactivate: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val user = userDetails.user ?: return
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminDetailRow("Status", if (user.isActive) "Active" else "Deactivated", if (user.isActive) Color(0xFF2E7D32) else Color(0xFFC62828))
                AdminDetailRow("Join Date", DateUtils.formatDate(user.createdAt))
                AdminDetailRow("Current Balance", CurrencyFormatter.format(userDetails.currentBalance))
                AdminDetailRow("Month Spending", CurrencyFormatter.format(userDetails.monthlySpending))
            }
        }
        
        Text("Management Actions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(8.dp)) {
                TextButton(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LockReset, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Reset Password", modifier = Modifier.weight(1f))
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                if (user.isActive) {
                    TextButton(onClick = onDeactivate, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFFA500))) {
                        Icon(Icons.Default.Block, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Suspend Access", modifier = Modifier.weight(1f))
                    }
                } else {
                    TextButton(onClick = onActivate, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF2E7D32))) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Restore Access", modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.DeleteForever, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Purge Account Data", modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AdminDetailRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}
