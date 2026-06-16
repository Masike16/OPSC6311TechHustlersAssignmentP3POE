/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.easebudgetv1.data.database.entities.User
import com.example.easebudgetv1.viewmodel.SettingsViewModel

/*
 * This screen is for all the user preferences and account stuff.
 * we have options for themes, notifications and data export.
 * 
 * References:
 * Google (2024) 'Settings guide', Android Developers. Available at: https://developer.android.com/develop/ui/views/components/settings (Accessed: 24 May 2024)
 * Material Design (2024) 'Lists: Controls', Material 3. Available at: https://m3.material.io/components/lists/guidelines#8674996b-0a7b-4028-8742-1264c74a008e (Accessed: 26 May 2024)
 * 
 * basically we used standard list items but made them look a bit more modern with surfaces and icons.
 * the csv export is a cool feature we added to help users keep their records outside the app.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userId: Long,
    onLogout: () -> Unit,
    onShowGuide: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(userId) {
        viewModel.loadUserData(userId)
    }

    // handle csv export by sharing the file string.
    LaunchedEffect(uiState.exportData) {
        uiState.exportData?.let { csvData ->
            shareCsvFile(context, csvData)
            viewModel.clearExportData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ModernProfileHeader(
                    user = uiState.currentUser,
                    onEditProfile = { showEditProfileDialog = true }
                )
                
                SettingsSection(title = "Information & Help") {
                    ModernSettingItem(
                        title = "How to Use EasEBudget",
                        subtitle = "Tips, tricks & earning badges",
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        onClick = onShowGuide
                    )
                    
                    ModernSettingItem(
                        title = "Help Overlay",
                        subtitle = "Enable/disable guided tooltips",
                        icon = Icons.Default.Visibility,
                        onClick = { viewModel.toggleGuidePreference(!uiState.showGuideOnStartup) }
                    ) {
                        Switch(
                            checked = uiState.showGuideOnStartup,
                            onCheckedChange = { viewModel.toggleGuidePreference(it) }
                        )
                    }
                }

                SettingsSection(title = "App Customization") {
                    ModernSettingItem(
                        title = "App Appearance",
                        subtitle = uiState.themeMode.replaceFirstChar { it.uppercase() },
                        icon = Icons.Default.Palette,
                        onClick = { }
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("light", "dark", "system").forEach { mode ->
                                val selected = uiState.themeMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.updateThemeMode(mode) },
                                    label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }
                    }
                }
                
                SettingsSection(title = "Security & Notifications") {
                    ModernSettingItem(
                        title = "Push Notifications",
                        subtitle = "Get alerts about your budget",
                        icon = Icons.Default.Notifications,
                        onClick = { viewModel.toggleNotifications(!uiState.notificationsEnabled) }
                    ) {
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) }
                        )
                    }

                    ModernSettingItem(
                        title = "Biometric Unlock",
                        subtitle = "Protect your financial data",
                        icon = Icons.Default.Fingerprint,
                        onClick = { viewModel.toggleBiometric(!uiState.biometricEnabled) }
                    ) {
                        Switch(
                            checked = uiState.biometricEnabled,
                            onCheckedChange = { viewModel.toggleBiometric(it) }
                        )
                    }
                    
                    ModernSettingItem(
                        title = "Access Credentials",
                        subtitle = "Update account password",
                        icon = Icons.Default.Lock,
                        onClick = { showChangePasswordDialog = true }
                    )
                }

                SettingsSection(title = "Data Management") {
                    ModernSettingItem(
                        title = "Export Data",
                        subtitle = "Export transactions to CSV",
                        icon = Icons.Default.Download,
                        onClick = { viewModel.exportTransactionsToCsv() }
                    )
                    
                    ModernSettingItem(
                        title = "Backup Data",
                        subtitle = "Secure your records",
                        icon = Icons.Default.Backup,
                        onClick = { 
                            android.widget.Toast.makeText(context, "Backup feature coming soon", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                
                SettingsSection(title = "Account Management") {
                    ModernSettingItem(
                        title = "Sign Out",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = onLogout,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                    
                    ModernSettingItem(
                        title = "Delete Account",
                        subtitle = "Permanently remove your data",
                        icon = Icons.Default.DeleteForever,
                        onClick = { showDeleteAccountDialog = true },
                        contentColor = MaterialTheme.colorScheme.error
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "EasEBudget v2.5.0",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
    
    if (showEditProfileDialog && uiState.currentUser != null) {
        ModernEditProfileDialog(
            user = uiState.currentUser!!,
            onDismiss = { showEditProfileDialog = false },
            onSave = { username, email ->
                viewModel.updateUsername(username)
                viewModel.updateEmail(email)
                showEditProfileDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ModernChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSave = { newPassword ->
                viewModel.changePassword(newPassword)
                showChangePasswordDialog = false
            }
        )
    }

    if (showDeleteAccountDialog) {
        var deleteConfirmation by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("This will permanently remove your account and all financial history. This cannot be undone.")
                    Text("Type \"DELETE\" to confirm:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = deleteConfirmation,
                        onValueChange = { deleteConfirmation = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DELETE") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteAccount(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = deleteConfirmation == "DELETE"
                ) { Text("Confirm Deletion") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun shareCsvFile(context: Context, csvData: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvData)
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Export Transactions")
    context.startActivity(shareIntent)
}

// Shows basic info about the logged in user
@Composable
fun ModernProfileHeader(user: User?, onEditProfile: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.username?.take(1)?.uppercase() ?: "U",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.username ?: "Guest User",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = user?.email ?: "Sign in to sync data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "UID: #${user?.id ?: 0}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            IconButton(
                onClick = onEditProfile,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(content = content)
        }
    }
}

// reusable item for the settings list. 
@Composable
fun ModernSettingItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(contentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), 
                    color = contentColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (trailingContent == null) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (trailingContent != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.padding(start = 56.dp)) {
                trailingContent()
            }
        }
    }
}

@Composable
fun ModernEditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(username, email) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ModernChangePasswordDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (password == confirmPassword) onSave(password) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}
