package com.jaganegeri.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Calendar : Screen("calendar/{month}/{year}") {
        fun createRoute(month: Int, year: Int) = "calendar/$month/$year"
    }
    object AddCase : Screen("addcase/{selectedDate}") {
        fun createRoute(date: String) = "addcase/$date"
    }
    object DetailCase : Screen("detail/{caseId}") {
        fun createRoute(caseId: String) = "detail/$caseId"
    }
    object ValidationQueue : Screen("validation_queue")
    object Riwayat : Screen("riwayat")
    object Profile : Screen("profile")
}
