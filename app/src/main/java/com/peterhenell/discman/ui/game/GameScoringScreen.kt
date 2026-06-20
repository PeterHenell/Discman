package com.peterhenell.discman.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.ui.platform.LocalContext
import com.peterhenell.discman.data.entities.Player
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScoringScreen(
    gameId: Long,
    navController: NavController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
        // Game details header — course name + datetime on one row
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = run {
                        val dateFmt = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault())
                        val timeFmt = AndroidDateFormat.getTimeFormat(context)
                        "${dateFmt.format(game.startDate)} ${timeFmt.format(game.startDate)}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scoring grid + fixed current-hole panel
        val holesToShow = remember(holes, uiState.currentHole) {
            holes.filter { it.holeNumber <= uiState.currentHole }
        }
        val currentHole = holes.find { it.holeNumber == uiState.currentHole }
        val listState = rememberLazyListState()

        // Auto-scroll to the current (last visible) hole when it changes
        LaunchedEffect(uiState.currentHole, holesToShow.size) {
            if (holesToShow.isNotEmpty()) {
                listState.animateScrollToItem(holesToShow.size) // +1 for header item
            }
        }

        // Table (top 70%) + current hole panel (bottom 30%)
        Column(modifier = Modifier.weight(1f)) {
            // Scrollable table — only holes up to the current one
            Box(modifier = Modifier.weight(0.7f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { ScoringHeaderRow(players = players) }
                    itemsIndexed(holesToShow) { index, hole ->
                        ScoringHoleRow(
                            hole = hole,
                            index = index,
                            players = players,
                            playerScores = playerScores,
                            isCurrentHole = hole.holeNumber == uiState.currentHole,
                            onHoleClick = { viewModel.setCurrentHole(hole.holeNumber) }
                        )
                    }
                }
            }

            // Fixed current hole panel — always 30% of the middle section
            if (currentHole != null) {
                Box(modifier = Modifier.weight(0.3f).fillMaxWidth()) {
                    CurrentHoleView(
                        hole = currentHole,
                        players = players,
                        playerScores = playerScores,
                        onScoreChange = { playerId, throws ->
                            viewModel.updatePlayerThrows(playerId, currentHole.holeNumber, throws)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    viewModel.markGameCompleted()
                    navController.navigate("completed_game/$gameId")
                },
                enabled = uiState.currentHole == holes.lastOrNull()?.holeNumber
            ) {
                Text("Complete Game")
            }

            OutlinedButton(
                onClick = { viewModel.nextHole() },
                enabled = uiState.currentHole < holes.size
            ) {
                Text("Next Hole")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
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
    index: Int,
    players: List<Player>,
    playerScores: List<com.peterhenell.discman.ui.model.PlayerScore>,
    isCurrentHole: Boolean,
    onHoleClick: () -> Unit
) {
    val rowBackground = when {
        isCurrentHole -> MaterialTheme.colorScheme.secondaryContainer
        index % 2 == 0 -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground)
                .clickable { onHoleClick() }
                .padding(horizontal = 8.dp, vertical = 5.dp)
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
}

@Composable
fun CurrentHoleView(
    hole: com.peterhenell.discman.data.entities.Hole,
    players: List<Player>,
    playerScores: List<com.peterhenell.discman.ui.model.PlayerScore>,
    onScoreChange: (Long, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
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
