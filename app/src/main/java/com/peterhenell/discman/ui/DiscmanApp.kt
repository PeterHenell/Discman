package com.peterhenell.discman.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.peterhenell.discman.ui.courses.CoursesScreen
import com.peterhenell.discman.ui.courses.CourseEditScreen
import com.peterhenell.discman.ui.game.PlayGameScreen
import com.peterhenell.discman.ui.game.GameScoringScreen
import com.peterhenell.discman.ui.game.CompletedGameScreen
import com.peterhenell.discman.ui.game.HistoricGamesScreen
import com.peterhenell.discman.ui.players.PlayersScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscmanApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "courses",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("courses") {
                CoursesScreen(navController = navController)
            }
            composable("course_edit") {
                CourseEditScreen(navController = navController)
            }
            composable("course_edit/{courseId}") { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId")?.toLongOrNull() ?: 0L
                CourseEditScreen(
                    navController = navController,
                    courseId = if (courseId == 0L) null else courseId
                )
            }
            composable("play") {
                PlayGameScreen(navController = navController)
            }
            composable("history") {
                HistoricGamesScreen(navController = navController)
            }
            composable("players") {
                PlayersScreen()
            }
            composable("game_scoring/{gameId}") { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull() ?: 0L
                GameScoringScreen(
                    gameId = gameId,
                    navController = navController
                )
            }
            composable("completed_game/{gameId}") { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull() ?: 0L
                CompletedGameScreen(
                    gameId = gameId,
                    navController = navController
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("courses", Icons.Default.Place, "Courses"),
    BottomNavItem("play", Icons.Default.PlayArrow, "Play"),
    BottomNavItem("history", Icons.Default.DateRange, "History"),
    BottomNavItem("players", Icons.Default.Person, "Players")
)
