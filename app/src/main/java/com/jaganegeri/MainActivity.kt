package com.jaganegeri

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.jaganegeri.ui.theme.Red700
import com.jaganegeri.ui.validation.ValidationQueueScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2000)
                    showSplash = false
                }

                if (showSplash) {
            SplashScreen()
                } else {
                    JagaNegeriNavHost(authRepository, caseRepository, validationRepository)
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = com.jaganegeri.R.mipmap.ic_launcher),
                contentDescription = "Logo JagaNegeri",
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "JAGA NEGERI",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Red700
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pantau & Catat Kasus Korupsi Indonesia",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

    val logout: () -> Unit = {
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
