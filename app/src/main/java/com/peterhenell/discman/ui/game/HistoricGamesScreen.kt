package com.peterhenell.discman.ui.game

import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.data.entities.Game
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricGamesScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val sortedGames = remember(games) { games.sortedByDescending { it.startDate } }

    var gameToDelete by remember { mutableStateOf<Game?>(null) }

    // Delete confirmation dialog
    gameToDelete?.let { game ->
        val course = courses.find { it.courseId == game.courseId }
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = { Text("Delete game?") },
            text = { Text("This will permanently delete the game on ${course?.name ?: "Unknown Course"}. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGame(game)
                    gameToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Game History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (sortedGames.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No games played yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start a new game to see it here!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedGames, key = { it.gameId }) { game ->
                    val course = courses.find { it.courseId == game.courseId }
                    SwipeToDiscardItem(onDiscard = { gameToDelete = game }) {
                        GameHistoryItem(
                            game = game,
                            courseName = course?.name ?: "Unknown Course",
                            onClick = {
                                navController.navigate("completed_game/${game.gameId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameHistoryItem(
    game: Game,
    courseName: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = run {
                        val dateFmt = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault())
                        val timeFmt = AndroidDateFormat.getTimeFormat(context)
                        "${dateFmt.format(game.startDate)} ${timeFmt.format(game.startDate)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View Game",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
