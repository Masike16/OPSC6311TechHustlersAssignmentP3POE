/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.Streak
import com.example.easebudgetv1.utils.GamificationUtils
import com.example.easebudgetv1.viewmodel.GoalsViewModel

/*
 * this screen is for gamification showing off all the badges and streaks.
 * it makes it feel like a game so people actually want to save money.
 * 
 * References:
 * Zichermann, G. and Cunningham, C. (2023) 'Gamification by Design', O'Reilly Media. Available at: https://www.oreilly.com/library/view/gamification-by-design/ (Accessed: 22 May 2024)
 * Google (2024) 'Lazy grids', Android Developers. Available at: https://developer.android.com/develop/ui/compose/lists#lazy-vertical-grid (Accessed: 24 May 2024)
 * 
 * we used a lazyverticalgrid here so we can show badges side by side in a nice grid. 
 * it looks really professional like those high end finance apps.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    userId: Long,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(userId) {
        viewModel.loadGoalsData(userId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gamification", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
            // using a grid for the layout. looks way better than just a list
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // top stats for age of money and points
                item {
                    ModernGoalStatCard(
                        title = "Age of Money",
                        value = "${uiState.ageOfMoney} Days",
                        icon = "💰",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                item {
                    ModernGoalStatCard(
                        title = "Total Points",
                        value = "${uiState.totalPoints}",
                        icon = "⭐",
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                // showing the streaks to keep the user motivated
                item {
                    val streak = uiState.streaks.find { it.streakType == "BUDGET_CHECKIN" }
                    ModernStreakCard("Check-in", streak)
                }
                
                item {
                    val streak = uiState.streaks.find { it.streakType == "LOGGING" }
                    ModernStreakCard("Activity", streak)
                }
                
                item(span = { GridItemSpan(2) }) {
                    Text(
                        "Your Badges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // loop through the achievements the user has earned
                items(
                    items = uiState.achievements,
                    key = { it.id }
                ) { achievement ->
                    ModernBadgeCard(achievement)
                }
                
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

// simple card for showing stats. uses icons to make it look fancy
@Composable
fun ModernGoalStatCard(
    title: String,
    value: String,
    icon: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.7f)
            )
        }
    }
}

// streak card showing how many days in a row they've been active
@Composable
fun ModernStreakCard(label: String, streak: Streak?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Whatshot,
                contentDescription = null,
                tint = if ((streak?.currentStreak ?: 0) > 0) Color(0xFFFF7043) else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${streak?.currentStreak ?: 0} Days",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Best: ${streak?.longestStreak ?: 0}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// card for individual badges with description and points earned
@Composable
fun ModernBadgeCard(achievement: Achievement) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = achievement.icon, fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = "+${GamificationUtils.calculatePointsForBadge(achievement.badgeType)}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
