package com.peterhenell.discman.ui.game

import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.data.entities.Course
import com.peterhenell.discman.data.entities.Game
import com.peterhenell.discman.data.entities.Player
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayGameScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()
    val incompleteGames by viewModel.incompleteGames.collectAsStateWithLifecycle()

    var showCourseDropdown by remember { mutableStateOf(false) }
    var hasUserStartedGame by remember { mutableStateOf(false) }
    var gameToDiscard by remember { mutableStateOf<Game?>(null) }

    // Discard confirmation dialog
    gameToDiscard?.let { game ->
        val course = courses.find { it.courseId == game.courseId }
        AlertDialog(
            onDismissRequest = { gameToDiscard = null },
            title = { Text("Discard game?") },
            text = { Text("This will permanently delete the in-progress game on ${course?.name ?: "Unknown Course"}. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGame(game)
                    gameToDiscard = null
                }) { Text("Discard", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { gameToDiscard = null }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Start New Game",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Incomplete games section
        if (incompleteGames.isNotEmpty()) {
            Text(
                text = "Resume Game",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(incompleteGames, key = { it.gameId }) { game ->
                    val course = courses.find { it.courseId == game.courseId }
                    SwipeToDiscardItem(
                        onDiscard = { gameToDiscard = game }
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RectangleShape,
                            onClick = { navController.navigate("game_scoring/${game.gameId}") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = course?.name ?: "Unknown Course",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = run {
                                            val dateFmt = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault())
                                            val timeFmt = AndroidDateFormat.getTimeFormat(context)
                                            "${dateFmt.format(game.startDate)} ${timeFmt.format(game.startDate)}"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Resume",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "New Game",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Course selection
        if (incompleteGames.isEmpty()) {
            Text(
                text = "Select Course",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        ExposedDropdownMenuBox(
            expanded = showCourseDropdown,
            onExpandedChange = { showCourseDropdown = it }
        ) {
            OutlinedTextField(
                value = uiState.selectedCourse?.name ?: "Select a course",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCourseDropdown)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showCourseDropdown,
                onDismissRequest = { showCourseDropdown = false }
            ) {
                courses.sortedBy { it.name }.forEach { course ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(course.name)
                                Text(
                                    text = course.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            viewModel.selectCourse(course)
                            showCourseDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player selection
        Text(
            text = "Select Players",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (players.isEmpty()) {
            Text(
                text = "No players available. Please add players first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(players) { player ->
                    PlayerSelectionItem(
                        player = player,
                        isSelected = uiState.selectedPlayers.contains(player),
                        onToggle = { viewModel.togglePlayerSelection(player) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start game button
        Button(
            onClick = {
                hasUserStartedGame = true
                viewModel.startGame()
            },
            enabled = uiState.selectedCourse != null && uiState.selectedPlayers.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Game")
        }
    }

    // Handle navigation when game is created
    LaunchedEffect(uiState.currentGame) {
        if (hasUserStartedGame && uiState.currentGame != null) {
            navController.navigate("game_scoring/${uiState.currentGame!!.gameId}")
            hasUserStartedGame = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDiscardItem(
    onDiscard: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDiscard()
            }
            // Always reset so the item snaps back (confirmation dialog handles actual delete)
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Discard",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        content()
    }
}

@Composable
fun PlayerSelectionItem(
    player: Player,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleMedium
            )

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}
