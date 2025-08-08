package com.peterhenell.discman.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.data.entities.Course
import com.peterhenell.discman.data.entities.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayGameScreen(
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()

    var showCourseDropdown by remember { mutableStateOf(false) }
    var hasUserStartedGame by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Start New Game",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Course selection
        Text(
            text = "Select Course",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

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

    // Handle navigation when game is created - only if user actually started a game
    LaunchedEffect(uiState.currentGame) {
        if (hasUserStartedGame && uiState.currentGame != null) {
            navController.navigate("game_scoring/${uiState.currentGame!!.gameId}")
            hasUserStartedGame = false // Reset flag
        }
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
