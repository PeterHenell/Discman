package com.peterhenell.discman.ui.game

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.ui.model.PlayerScore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedGameScreen(
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
    val holes = uiState.holes
    val playerScores = viewModel.getPlayerScores().sortedBy { it.totalScore }

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
        // Header
        Text(
            text = "Game Results",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Game info
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleLarge,
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

        // Scorecard
        Text(
            text = "Scorecard",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Scorecard Table
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                ScorecardTable(
                    holes = holes,
                    playerScores = playerScores
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = {
                    val shareText = buildShareText(course.name, game.startDate, playerScores, holes)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "Disc Golf Scores - ${course.name}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Scores"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share")
            }

            OutlinedButton(
                onClick = {
                    navController.navigate("game_scoring/$gameId")
                }
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
            }

            Button(
                onClick = {
                    navController.navigate("history") {
                        popUpTo("history") { inclusive = true }
                    }
                }
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
fun ScorecardTable(
    holes: List<com.peterhenell.discman.data.entities.Hole>,
    playerScores: List<PlayerScore>
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Player",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1.5f)
            )

            holes.forEach { hole ->
                Text(
                    text = "${hole.holeNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Tot",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.6f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Player rows
        playerScores.forEach { playerScore ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = playerScore.player.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1.5f),
                    maxLines = 1
                )

                holes.forEach { hole ->
                    val throws = playerScore.holeScores[hole.holeNumber] ?: hole.par
                    val score = throws - hole.par

                    Text(
                        text = when {
                            score == 0 -> "E"
                            score > 0 -> "+$score"
                            else -> score.toString()
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .weight(0.5f)
                            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                            .padding(1.dp),
                        textAlign = TextAlign.Center,
                        color = when {
                            score < 0 -> MaterialTheme.colorScheme.primary
                            score > 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )
                }

                // Total score
                Text(
                    text = when {
                        playerScore.totalScore == 0 -> "E"
                        playerScore.totalScore > 0 -> "+${playerScore.totalScore}"
                        else -> playerScore.totalScore.toString()
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

private fun buildShareText(
    courseName: String,
    gameDate: Date,
    playerScores: List<PlayerScore>,
    holes: List<com.peterhenell.discman.data.entities.Hole>
): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val builder = StringBuilder()

    builder.appendLine("🥏 Disc Golf Scores")
    builder.appendLine("Course: $courseName")
    builder.appendLine("Date: ${dateFormat.format(gameDate)}")
    builder.appendLine("Total Par: ${holes.sumOf { it.par }}")
    builder.appendLine()

    builder.appendLine("Final Results:")
    playerScores.forEachIndexed { index, playerScore ->
        val position = index + 1
        val scoreText = when {
            playerScore.totalScore == 0 -> "Even"
            playerScore.totalScore > 0 -> "+${playerScore.totalScore}"
            else -> playerScore.totalScore.toString()
        }
        builder.appendLine("$position. ${playerScore.player.name}: $scoreText (${playerScore.totalThrows} throws)")
    }

    return builder.toString()
}
