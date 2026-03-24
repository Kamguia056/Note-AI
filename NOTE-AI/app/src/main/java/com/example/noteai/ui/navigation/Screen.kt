package com.example.noteai.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ImportDoc : Screen("import")
    object Analysis : Screen("analysis/{courseId}") {
        fun createRoute(courseId: String) = "analysis/$courseId"
    }
    object Summary : Screen("summary/{courseId}") {
        fun createRoute(courseId: String) = "summary/$courseId"
    }
    object Quiz : Screen("quiz/{courseId}") {
        fun createRoute(courseId: String) = "quiz/$courseId"
    }
    object Stats : Screen("stats")
}
