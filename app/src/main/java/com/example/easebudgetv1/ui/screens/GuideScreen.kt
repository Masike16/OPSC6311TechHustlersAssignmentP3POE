package com.example.easebudgetv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(
    userId: Long,
    onDismiss: (Boolean) -> Unit // Returns whether to show again
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Getting Started", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { onDismiss(!dontShowAgain) }) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            GuideItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Set Your Monthly Goal",
                description = "Start by going to the Budget tab. Set your target income and spending limits. This drives your 'Available Balance' on the dashboard.",
                color = MaterialTheme.colorScheme.primary
            )

            GuideItem(
                icon = Icons.Default.AddCircleOutline,
                title = "Track Every Transaction",
                description = "Tap the '+' button to log expenses. We'll show you exactly how much room you have left in each category's budget as you type.",
                color = Color(0xFF2E7D32)
            )

            GuideItem(
                icon = Icons.Default.Stars,
                title = "Earn Badges & Points",
                description = "Earn badges like 'First Log' or 'Under Budget'. Every badge adds to your total points. Check the 'Rewards' tab to see your progress.",
                color = Color(0xFFFFA500)
            )

            GuideItem(
                icon = Icons.Default.People,
                title = "Shared Finances",
                description = "Use the family icon on the dashboard bottom-left to invite partners. You can manage shared budgets together.",
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Checkbox(
                    checked = dontShowAgain,
                    onCheckedChange = { dontShowAgain = it }
                )
                Text(
                    "Don't show this again on startup",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { onDismiss(!dontShowAgain) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Got it, let's start!", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GuideItem(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
