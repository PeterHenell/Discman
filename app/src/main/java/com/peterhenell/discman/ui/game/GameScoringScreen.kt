package com.peterhenell.discman.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.data.entities.Player
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScoringScreen(
    gameId: Long,
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gameId) {
        if (gameId > 0) {
            viewModel.loadGame(gameId)
        }
    }

    val game = uiState.currentGame
    val course = uiState.selectedCourse
    val players = uiState.selectedPlayers
    val holes = uiState.holes
    val playerScores = viewModel.getPlayerScores()

    if (game == null || course == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Game details header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(game.startDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total Par: ${holes.sumOf { it.par }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scoring grid
        Box(
            modifier = Modifier.weight(1f)
        ) {
            val listState = rememberLazyListState()

            LazyColumn(
                state = listState
            ) {
                // Header row
                item {
                    ScoringHeaderRow(players = players)
                }

                // Hole rows
                itemsIndexed(holes) { index, hole ->
                    ScoringHoleRow(
                        hole = hole,
                        players = players,
                        playerScores = playerScores,
                        isCurrentHole = hole.holeNumber == uiState.currentHole,
                        onHoleClick = { viewModel.setCurrentHole(hole.holeNumber) },
                        onScoreChange = { playerId, throws ->
                            viewModel.updatePlayerThrows(playerId, hole.holeNumber, throws)
                        }
                    )
                }
            }

            // Custom scrollbar indicator
            if (holes.size > 1) {
                val firstVisibleIndex by remember {
                    derivedStateOf { listState.firstVisibleItemIndex }
                }
                val layoutInfo = listState.layoutInfo
                val totalItems = layoutInfo.totalItemsCount
                val visibleItems = layoutInfo.visibleItemsInfo.size

                // Only show scrollbar if there are items that extend beyond the visible area
                if (totalItems > visibleItems) {
                    val scrollProgress = if (totalItems > 1) {
                        firstVisibleIndex.toFloat() / (totalItems - visibleItems).coerceAtLeast(1)
                    } else 0f

                    // Scrollbar track
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(4.dp)
                            .padding(vertical = 8.dp)
                            .background(
                                Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    // Scrollbar thumb
                    val thumbHeight = 0.3f // 30% of track height
                    val thumbOffset = scrollProgress * (1f - thumbHeight)

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight(thumbHeight)
                            .width(4.dp)
                            .padding(horizontal = 0.dp)
                            .offset(y = (thumbOffset * 200).dp) // Approximate offset calculation
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { viewModel.nextHole() },
                enabled = uiState.currentHole < holes.size
            ) {
                Text("Next Hole")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Button(
                onClick = {
                    navController.navigate("completed_game/$gameId")
                }
            ) {
                Text("Complete Game")
            }
        }
    }
}

@Composable
fun ScoringHeaderRow(players: List<Player>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp)
    ) {
        Text(
            text = "Hole",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        players.forEach { player ->
            Text(
                text = player.name,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScoringHoleRow(
    hole: com.peterhenell.discman.data.entities.Hole,
    players: List<Player>,
    playerScores: List<com.peterhenell.discman.ui.model.PlayerScore>,
    isCurrentHole: Boolean,
    onHoleClick: () -> Unit,
    onScoreChange: (Long, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isCurrentHole)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
                .clickable { onHoleClick() }
                .padding(8.dp)
        ) {
            // Hole number and par
            Text(
                text = "${hole.holeNumber}\n(${hole.par})",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = if (isCurrentHole) FontWeight.Bold else FontWeight.Normal
            )

            // Player scores
            players.forEach { player ->
                val playerScore = playerScores.find { it.player.playerId == player.playerId }
                val throws = playerScore?.holeScores?.get(hole.holeNumber) ?: hole.par

                Text(
                    text = throws.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = if (isCurrentHole) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Expanded current hole view
        if (isCurrentHole) {
            CurrentHoleView(
                hole = hole,
                players = players,
                playerScores = playerScores,
                onScoreChange = onScoreChange
            )
        }
    }
}

@Composable
fun CurrentHoleView(
    hole: com.peterhenell.discman.data.entities.Hole,
    players: List<Player>,
    playerScores: List<com.peterhenell.discman.ui.model.PlayerScore>,
    onScoreChange: (Long, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hole ${hole.holeNumber} - Par ${hole.par}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            hole.distance?.let { distance ->
                Text(
                    text = "Distance: ${distance}m",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            hole.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            players.forEach { player ->
                val playerScore = playerScores.find { it.player.playerId == player.playerId }
                val currentThrows = playerScore?.holeScores?.get(hole.holeNumber) ?: hole.par
                val score = currentThrows - hole.par

                PlayerScoreRow(
                    player = player,
                    throws = currentThrows,
                    score = score,
                    onThrowsChange = { newThrows ->
                        onScoreChange(player.playerId, newThrows)
                    }
                )

                if (player != players.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PlayerScoreRow(
    player: Player,
    throws: Int,
    score: Int,
    onThrowsChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = player.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (throws > 1) onThrowsChange(throws - 1) }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease throws")
            }

            Text(
                text = throws.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { onThrowsChange(throws + 1) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase throws")
            }

            Text(
                text = when {
                    score == 0 -> "Par"
                    score > 0 -> "+$score"
                    else -> score.toString()
                },
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
