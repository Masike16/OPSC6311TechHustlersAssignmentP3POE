package com.example.easebudgetv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.easebudgetv1.ui.navigation.AppNavigation
import com.example.easebudgetv1.ui.theme.EasEBudgetTheme
import com.example.easebudgetv1.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // FORCED LOGOUT ON FRESH LAUNCH ONLY (Requirement 1)
        if (savedInstanceState == null) {
            sessionManager.logout()
        }

        // Splash screen delay for 2 seconds (Requirement 1)
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            keepSplashScreen = false
        }

        enableEdgeToEdge()

        setContent {
            EasEBudgetTheme {
                AppNavigation(sessionManager = sessionManager)
            }
        }
    }
}
