package com.jaganegeri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jaganegeri.data.model.Profile
import com.jaganegeri.data.repository.AuthRepository
import com.jaganegeri.data.repository.CaseRepository
import com.jaganegeri.data.repository.ValidationRepository
import com.jaganegeri.ui.addcase.AddCaseScreen
import com.jaganegeri.ui.calendar.CalendarScreen
import com.jaganegeri.ui.detail.DetailCaseScreen
import com.jaganegeri.ui.home.HomeScreen
import com.jaganegeri.ui.home.HomeViewModel
import com.jaganegeri.ui.login.LoginScreen
import com.jaganegeri.ui.login.LoginViewModel
import com.jaganegeri.ui.navigation.Screen
import com.jaganegeri.ui.profile.ProfileScreen
import com.jaganegeri.ui.riwayat.RiwayatScreen
import com.jaganegeri.ui.theme.JagaNegeriTheme
import com.jaganegeri.ui.validation.ValidationQueueScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as JagaNegeriApp
        val authRepository = AuthRepository(app.supabase)
        val caseRepository = CaseRepository(app.supabase)
        val validationRepository = ValidationRepository(app.supabase)

        setContent {
            JagaNegeriTheme {
                JagaNegeriNavHost(authRepository, caseRepository, validationRepository)
            }
        }
    }
}

@Composable
fun JagaNegeriNavHost(
    authRepository: AuthRepository,
    caseRepository: CaseRepository,
    validationRepository: ValidationRepository
) {
    val navController = rememberNavController()
    val loginViewModel = remember { LoginViewModel(authRepository) }
    val loginState by loginViewModel.uiState.collectAsState()
    val loggedInProfile: Profile? = loginState.profile

    val homeViewModel = remember(loggedInProfile?.id) {
        loggedInProfile?.let { HomeViewModel(it.id, caseRepository) }
    }

    val logout = {
        CoroutineScope(Dispatchers.IO).launch {
            authRepository.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // ========== LOGIN ==========
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ========== HOME ==========
        composable(Screen.Home.route) {
            val profile = loggedInProfile
            if (profile != null && homeViewModel != null) {
                HomeScreen(
                    username = profile.username,
                    userId = profile.id,
                    homeViewModel = homeViewModel,
                    onMonthClick = { month, year ->
                        navController.navigate(Screen.Calendar.createRoute(month, year))
                    },
                    onRiwayatClick = {
                        navController.navigate(Screen.Riwayat.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onLogout = logout
                )
            }
        }

        // ========== CALENDAR ==========
        composable(
            route = Screen.Calendar.route,
            arguments = listOf(
                navArgument("month") { type = NavType.IntType },
                navArgument("year") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val month = backStackEntry.arguments?.getInt("month") ?: 0
            val year = backStackEntry.arguments?.getInt("year") ?: 2025
            val profile = loggedInProfile

            if (profile != null) {
                CalendarScreen(
                    userId = profile.id,
                    initialMonth = month,
                    initialYear = year,
                    caseRepository = caseRepository,
                    onBack = { navController.popBackStack() },
                    onAddCase = { date ->
                        navController.navigate(Screen.AddCase.createRoute(date))
                    },
                    onCaseClick = { caseId ->
                        navController.navigate(Screen.DetailCase.createRoute(caseId))
                    }
                )
            }
        }

        // ========== ADD CASE ==========
        composable(
            route = Screen.AddCase.route,
            arguments = listOf(
                navArgument("selectedDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("selectedDate") ?: ""
            val profile = loggedInProfile

            if (profile != null) {
                AddCaseScreen(
                    userId = profile.id,
                    selectedDate = date,
                    caseRepository = caseRepository,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                        homeViewModel?.loadData()
                    }
                )
            }
        }

        // ========== DETAIL CASE ==========
        composable(
            route = Screen.DetailCase.route,
            arguments = listOf(
                navArgument("caseId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            DetailCaseScreen(
                caseId = caseId,
                caseRepository = caseRepository,
                onBack = { navController.popBackStack() }
            )
        }

        // ========== VALIDATION QUEUE ==========
        composable(Screen.ValidationQueue.route) {
            val profile = loggedInProfile
            if (profile != null) {
                ValidationQueueScreen(
                    userId = profile.id,
                    validationRepository = validationRepository,
                    onBack = { navController.popBackStack() },
                    onCaseClick = { caseId ->
                        navController.navigate(Screen.DetailCase.createRoute(caseId))
                    }
                )
            }
        }

        // ========== RIWAYAT ==========
        composable(Screen.Riwayat.route) {
            RiwayatScreen(
                caseRepository = caseRepository,
                onBack = { navController.popBackStack() },
                onCaseClick = { caseId ->
                    navController.navigate(Screen.DetailCase.createRoute(caseId))
                }
            )
        }

        // ========== PROFILE ==========
        composable(Screen.Profile.route) {
            val profile = loggedInProfile
            if (profile != null) {
                ProfileScreen(
                    userId = profile.id,
                    username = profile.username,
                    caseRepository = caseRepository,
                    validationRepository = validationRepository,
                    onBack = { navController.popBackStack() },
                    onValidationQueueClick = {
                        navController.navigate(Screen.ValidationQueue.route)
                    },
                    onLogout = logout
                )
            }
        }
    }
}
