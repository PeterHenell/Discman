package com.peterhenell.discman.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.peterhenell.discman.data.entities.*
import com.peterhenell.discman.data.repository.DataStorageService
import com.peterhenell.discman.ui.model.GameWithDetails
import com.peterhenell.discman.ui.model.PlayerScore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val dataStorage: DataStorageService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()

    val courses = dataStorage.getAllCourses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val players = dataStorage.getAllPlayers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val games = dataStorage.getAllGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectCourse(course: Course) {
        viewModelScope.launch {
            val holes = dataStorage.getHolesByCourse(course.courseId).first()
            _uiState.value = _uiState.value.copy(
                selectedCourse = course,
                holes = holes
            )
        }
    }

    fun togglePlayerSelection(player: Player) {
        val currentPlayers = _uiState.value.selectedPlayers.toMutableList()
        if (currentPlayers.contains(player)) {
            currentPlayers.remove(player)
        } else {
            currentPlayers.add(player)
        }
        _uiState.value = _uiState.value.copy(selectedPlayers = currentPlayers)
    }

    fun startGame() {
        viewModelScope.launch {
            val course = _uiState.value.selectedCourse
            val selectedPlayers = _uiState.value.selectedPlayers
            val holes = _uiState.value.holes

            if (course != null && selectedPlayers.isNotEmpty() && holes.isNotEmpty()) {
                // Create game
                val game = Game(courseId = course.courseId, startDate = Date())
                val gameId = dataStorage.insertGame(game)

                // Add players to game
                val gamePlayers = selectedPlayers.map {
                    GamePlayer(gameId = gameId, playerId = it.playerId)
                }
                dataStorage.insertGamePlayers(gamePlayers)

                // Initialize throws for each player and hole with par value
                val initialThrows = mutableListOf<GamePlayerHoleThrow>()
                selectedPlayers.forEach { player ->
                    holes.forEach { hole ->
                        initialThrows.add(
                            GamePlayerHoleThrow(
                                gameId = gameId,
                                playerId = player.playerId,
                                holeNumber = hole.holeNumber,
                                numberOfThrows = hole.par
                            )
                        )
                    }
                }
                dataStorage.insertGamePlayerHoleThrows(initialThrows)

                // Update the UI state with the created game instead of calling loadGame
                val createdGame = game.copy(gameId = gameId)
                _uiState.value = _uiState.value.copy(
                    currentGame = createdGame,
                    gameThrows = initialThrows,
                    currentHole = holes.firstOrNull()?.holeNumber ?: 1
                )
            }
        }
    }

    fun loadGame(gameId: Long) {
        viewModelScope.launch {
            val game = dataStorage.getGameById(gameId)
            if (game != null) {
                val course = dataStorage.getCourseById(game.courseId)
                val gamePlayers = dataStorage.getGamePlayers(gameId)
                val players = gamePlayers.mapNotNull { gp ->
                    dataStorage.getPlayerById(gp.playerId)
                }
                val holes = dataStorage.getHolesByCourse(game.courseId).first()
                val throws = dataStorage.getGamePlayerHoleThrows(gameId)

                _uiState.value = _uiState.value.copy(
                    currentGame = game,
                    selectedCourse = course,
                    selectedPlayers = players,
                    holes = holes,
                    gameThrows = throws,
                    currentHole = holes.firstOrNull()?.holeNumber ?: 1
                )
            }
        }
    }

    fun setCurrentHole(holeNumber: Int) {
        _uiState.value = _uiState.value.copy(currentHole = holeNumber)
    }

    fun updatePlayerThrows(playerId: Long, holeNumber: Int, throws: Int) {
        viewModelScope.launch {
            val gameId = _uiState.value.currentGame?.gameId ?: return@launch
            val existingThrow = dataStorage.getGamePlayerHoleThrow(gameId, playerId, holeNumber)

            if (existingThrow != null) {
                val updatedThrow = existingThrow.copy(numberOfThrows = throws)
                dataStorage.updateGamePlayerHoleThrow(updatedThrow)

                // Update local state
                val updatedThrows = _uiState.value.gameThrows.map {
                    if (it.gameId == gameId && it.playerId == playerId && it.holeNumber == holeNumber) {
                        updatedThrow
                    } else it
                }
                _uiState.value = _uiState.value.copy(gameThrows = updatedThrows)
            }
        }
    }

    fun getPlayerScores(): List<PlayerScore> {
        val players = _uiState.value.selectedPlayers
        val holes = _uiState.value.holes
        val throws = _uiState.value.gameThrows

        return players.map { player ->
            val holeScores = mutableMapOf<Int, Int>()
            var totalThrows = 0
            var totalScore = 0

            holes.forEach { hole ->
                val playerThrow = throws.find {
                    it.playerId == player.playerId && it.holeNumber == hole.holeNumber
                }
                val throwCount = playerThrow?.numberOfThrows ?: hole.par
                holeScores[hole.holeNumber] = throwCount
                totalThrows += throwCount
                totalScore += (throwCount - hole.par) // Score relative to par
            }

            PlayerScore(
                player = player,
                holeScores = holeScores,
                totalScore = totalScore,
                totalThrows = totalThrows
            )
        }
    }

    fun nextHole() {
        val currentHole = _uiState.value.currentHole
        val holes = _uiState.value.holes
        val nextHoleNumber = holes.find { it.holeNumber > currentHole }?.holeNumber
        if (nextHoleNumber != null) {
            setCurrentHole(nextHoleNumber)
        }
    }

    fun clearGameSetup() {
        _uiState.value = _uiState.value.copy(
            selectedCourse = null,
            selectedPlayers = emptyList(),
            holes = emptyList()
        )
    }
}

data class GameUiState(
    val selectedCourse: Course? = null,
    val selectedPlayers: List<Player> = emptyList(),
    val holes: List<Hole> = emptyList(),
    val currentGame: Game? = null,
    val gameThrows: List<GamePlayerHoleThrow> = emptyList(),
    val currentHole: Int = 1,
    val isLoading: Boolean = false
)
