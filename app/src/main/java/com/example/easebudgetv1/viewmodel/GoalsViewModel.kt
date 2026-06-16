/*
 * OPSC6311 Assignment POE
 * Tech Hustlers
 * 
 * We certify that this is our own work.
 */
package com.example.easebudgetv1.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easebudgetv1.data.database.entities.Achievement
import com.example.easebudgetv1.data.database.entities.Streak
import com.example.easebudgetv1.data.repository.AppRepository
import com.example.easebudgetv1.utils.GamificationUtils
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * handles the gamification part of the app. keeps track of badges and streaks to make
 * budgeting a bit more fun for the user.
 * 
 * References:
 * Bunchball (2024) 'What is Gamification?', Bunchball. Available at: https://www.bunchball.com/gamification (Accessed: 22 May 2024)
 * Google (2024) 'ViewModel Overview', Android Developers. Available at: https://developer.android.com/topic/libraries/architecture/viewmodel (Accessed: 24 May 2024)
 * Kotlin (2024) 'Flow', Kotlin. Available at: https://kotlinlang.org/docs/flow.html (Accessed: 25 May 2024)
 * 
 * we use combine to bring together achievements and streaks. makes it easier to update the UI
 * all at once when something changes.
 */

@Immutable
data class GoalsUiState(
    val achievements: List<Achievement> = emptyList(),
    val streaks: List<Streak> = emptyList(),
    val ageOfMoney: Int = 0,
    val totalPoints: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repositoryLazy: Lazy<AppRepository>
) : ViewModel() {
    
    private val repository get() = repositoryLazy.get()
    private val _userId = MutableStateFlow<Long?>(null)

    // combines achievements streaks and transactions to show progress
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GoalsUiState> = _userId
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                repository.getAchievementsByUserId(userId),
                repository.getStreaksByUserId(userId),
                repository.getTransactionsByUserId(userId)
            ) { achievements, streaks, transactions ->
                GoalsUiState(
                    achievements = achievements,
                    streaks = streaks,
                    ageOfMoney = GamificationUtils.calculateAgeOfMoney(transactions),
                    totalPoints = achievements.sumOf { GamificationUtils.calculatePointsForBadge(it.badgeType) },
                    isLoading = false
                )
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalsUiState(isLoading = true)
        )
    
    fun loadGoalsData(userId: Long) {
        _userId.value = userId
    }
    
    // awards a badge if the user doesnt have it already
    fun checkAndAwardBadge(userId: Long, badgeType: String, title: String, description: String, icon: String) {
        viewModelScope.launch {
            val existing = repository.getAchievementByType(userId, badgeType)
            if (existing == null) {
                val achievement = Achievement(
                    userId = userId,
                    badgeType = badgeType,
                    title = title,
                    description = description,
                    icon = icon
                )
                repository.insertAchievement(achievement)
            }
        }
    }
    
    // updates the user streak. keeps people coming back to the app daily
    fun updateStreak(userId: Long, streakType: String, increment: Boolean) {
        viewModelScope.launch {
            val streak = repository.getStreak(userId, streakType)
            if (streak != null) {
                val newCurrent = if (increment) streak.currentStreak + 1 else 0
                val newLongest = maxOf(streak.longestStreak, newCurrent)
                repository.updateStreak(
                    streak.copy(
                        currentStreak = newCurrent,
                        longestStreak = newLongest,
                        lastActivityDate = System.currentTimeMillis()
                    )
                )
            } else {
                repository.insertStreak(
                    Streak(
                        userId = userId,
                        streakType = streakType,
                        currentStreak = if (increment) 1 else 0,
                        longestStreak = if (increment) 1 else 0
                    )
                )
            }
        }
    }
}
