package com.peterhenell.discman.ui.game

import android.content.Intent
import android.text.format.DateFormat as AndroidDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.peterhenell.discman.ui.model.PlayerScore
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

    // Date/time picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableStateOf(0L) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = game?.startDate?.time
    )
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().also { it.time = game?.startDate ?: Date() }.get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().also { it.time = game?.startDate ?: Date() }.get(Calendar.MINUTE),
        is24Hour = AndroidDateFormat.is24HourFormat(context)
    )

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
            .padding(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Game Results",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = {
                    val shareText = buildShareText(context, course.name, game.startDate, playerScores, holes)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "Disc Golf Scores - ${course.name}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Scores"))
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = { navController.navigate("game_scoring/$gameId") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = {
                    navController.navigate("history") {
                        popUpTo("history") { inclusive = true }
                    }
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = run {
                                val dateFmt = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, Locale.getDefault())
                                val timeFmt = AndroidDateFormat.getTimeFormat(context)
                                "${dateFmt.format(game.startDate)} ${timeFmt.format(game.startDate)}"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit date",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Scorecard
//        Text(
//            text = "Scorecard",
//            style = MaterialTheme.typography.titleLarge,
//            fontWeight = FontWeight.Bold
//        )


        // Scorecard Table
        Card(
            modifier = Modifier.weight(1f),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {

                ScorecardTable(
                    holes = holes,
                    playerScores = playerScores
                )
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = RectangleShape,
            confirmButton = {
                TextButton(onClick = {
                    pendingDateMillis = datePickerState.selectedDateMillis ?: game.startDate.time
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            shape = RectangleShape,
            title = { Text("Select time") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = pendingDateMillis
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    viewModel.updateGameDate(cal.time)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ScorecardTable(
    holes: List<com.peterhenell.discman.data.entities.Hole>,
    playerScores: List<PlayerScore>
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val holeColWidth = 48.dp
    val parColWidth = 36.dp
    val playerColWidth = 72.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
            .verticalScroll(verticalScrollState)
    ) {
        // Header row: Hole | Par | Player1 | Player2 | ...
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScorecardCell(text = "Hole", width = holeColWidth, bold = true, isHeader = true)
            ScorecardCell(text = "Par", width = parColWidth, bold = true, isHeader = true)
            playerScores.forEach { ps ->
                ScorecardCell(text = ps.player.name, width = playerColWidth, bold = true, isHeader = true, maxLines = 1)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // One row per hole
        holes.forEachIndexed { index, hole ->
            val rowBackground = if (index % 2 == 0)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(rowBackground)
            ) {
                ScorecardCell(text = "${hole.holeNumber}", width = holeColWidth, bold = true)
                ScorecardCell(text = "${hole.par}", width = parColWidth)
                playerScores.forEach { ps ->
                    val throws = ps.holeScores[hole.holeNumber] ?: hole.par
                    val score = throws - hole.par
                    ScorecardCell(
                        text = throws.toString(),
                        width = playerColWidth,
                        color = when {
                            score < 0 -> MaterialTheme.colorScheme.primary
                            score > 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }

        // Total row
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScorecardCell(text = "Total", width = holeColWidth, bold = true)
            ScorecardCell(text = "${holes.sumOf { it.par }}", width = parColWidth, bold = true)
            playerScores.forEach { ps ->
                val totalText = when {
                    ps.totalScore == 0 -> "E"
                    ps.totalScore > 0 -> "+${ps.totalScore}"
                    else -> ps.totalScore.toString()
                }
                ScorecardCell(
                    text = "$totalText (${ps.totalThrows})",
                    width = playerColWidth,
                    bold = true,
                    color = when {
                        ps.totalScore < 0 -> MaterialTheme.colorScheme.primary
                        ps.totalScore > 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun ScorecardCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false,
    isHeader: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = 1
) {
    val style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
    Text(
        text = text,
        style = style,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        maxLines = maxLines,
        color = color,
        modifier = Modifier
            .width(width)
            .padding(vertical = 3.dp, horizontal = 2.dp)
    )
}

private fun buildShareText(
    context: android.content.Context,
    courseName: String,
    gameDate: Date,
    playerScores: List<PlayerScore>,
    holes: List<com.peterhenell.discman.data.entities.Hole>
): String {
    val dateFmt = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, java.util.Locale.getDefault())
    val timeFmt = AndroidDateFormat.getTimeFormat(context)
    val builder = StringBuilder()

    builder.appendLine("🥏 Disc Golf Scores")
    builder.appendLine("Course: $courseName")
    builder.appendLine("Date: ${dateFmt.format(gameDate)} ${timeFmt.format(gameDate)}")
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
