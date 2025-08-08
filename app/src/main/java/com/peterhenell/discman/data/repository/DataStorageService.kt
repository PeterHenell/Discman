package com.peterhenell.discman.data.repository

import com.peterhenell.discman.data.dao.*
import com.peterhenell.discman.data.entities.*
import kotlinx.coroutines.flow.Flow

class DataStorageService(
    private val courseDao: CourseDao,
    private val holeDao: HoleDao,
    private val playerDao: PlayerDao,
    private val gameDao: GameDao
) {
    // Course operations
    fun getAllCourses(): Flow<List<Course>> = courseDao.getAllCourses()
    suspend fun getCourseById(courseId: Long): Course? = courseDao.getCourseById(courseId)
    suspend fun insertCourse(course: Course): Long = courseDao.insertCourse(course)
    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course)
    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course)

    // Hole operations
    fun getHolesByCourse(courseId: Long): Flow<List<Hole>> = holeDao.getHolesByCourse(courseId)
    suspend fun getHole(courseId: Long, holeNumber: Int): Hole? = holeDao.getHole(courseId, holeNumber)
    suspend fun insertHole(hole: Hole): Long = holeDao.insertHole(hole)
    suspend fun insertHoles(holes: List<Hole>) = holeDao.insertHoles(holes)
    suspend fun updateHole(hole: Hole) = holeDao.updateHole(hole)
    suspend fun deleteHole(hole: Hole) = holeDao.deleteHole(hole)
    suspend fun deleteHolesByCourse(courseId: Long) = holeDao.deleteHolesByCourse(courseId)

    // Player operations
    fun getAllPlayers(): Flow<List<Player>> = playerDao.getAllPlayers()
    suspend fun getPlayerById(playerId: Long): Player? = playerDao.getPlayerById(playerId)
    suspend fun insertPlayer(player: Player): Long = playerDao.insertPlayer(player)
    suspend fun updatePlayer(player: Player) = playerDao.updatePlayer(player)
    suspend fun deletePlayer(player: Player) = playerDao.deletePlayer(player)

    // Game operations
    fun getAllGames(): Flow<List<Game>> = gameDao.getAllGames()
    suspend fun getGameById(gameId: Long): Game? = gameDao.getGameById(gameId)
    suspend fun insertGame(game: Game): Long = gameDao.insertGame(game)
    suspend fun updateGame(game: Game) = gameDao.updateGame(game)
    suspend fun deleteGame(game: Game) = gameDao.deleteGame(game)

    // Game player operations
    suspend fun insertGamePlayer(gamePlayer: GamePlayer) = gameDao.insertGamePlayer(gamePlayer)
    suspend fun insertGamePlayers(gamePlayers: List<GamePlayer>) = gameDao.insertGamePlayers(gamePlayers)
    suspend fun getGamePlayers(gameId: Long): List<GamePlayer> = gameDao.getGamePlayers(gameId)

    // Game player hole throw operations
    suspend fun insertGamePlayerHoleThrow(gamePlayerHoleThrow: GamePlayerHoleThrow) = gameDao.insertGamePlayerHoleThrow(gamePlayerHoleThrow)
    suspend fun insertGamePlayerHoleThrows(gamePlayerHoleThrows: List<GamePlayerHoleThrow>) = gameDao.insertGamePlayerHoleThrows(gamePlayerHoleThrows)
    suspend fun updateGamePlayerHoleThrow(gamePlayerHoleThrow: GamePlayerHoleThrow) = gameDao.updateGamePlayerHoleThrow(gamePlayerHoleThrow)
    suspend fun getGamePlayerHoleThrow(gameId: Long, playerId: Long, holeNumber: Int): GamePlayerHoleThrow? =
        gameDao.getGamePlayerHoleThrow(gameId, playerId, holeNumber)
    suspend fun getGamePlayerHoleThrows(gameId: Long): List<GamePlayerHoleThrow> = gameDao.getGamePlayerHoleThrows(gameId)
}
