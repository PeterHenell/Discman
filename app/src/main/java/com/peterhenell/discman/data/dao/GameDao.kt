package com.peterhenell.discman.data.dao

import androidx.room.*
import com.peterhenell.discman.data.entities.Game
import com.peterhenell.discman.data.entities.GamePlayer
import com.peterhenell.discman.data.entities.GamePlayerHoleThrow
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY startDate DESC")
    fun getAllGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE gameId = :gameId")
    suspend fun getGameById(gameId: Long): Game?

    @Insert
    suspend fun insertGame(game: Game): Long

    @Update
    suspend fun updateGame(game: Game)

    @Delete
    suspend fun deleteGame(game: Game)

    // Game Players
    @Insert
    suspend fun insertGamePlayer(gamePlayer: GamePlayer)

    @Insert
    suspend fun insertGamePlayers(gamePlayers: List<GamePlayer>)

    @Query("SELECT * FROM game_players WHERE gameId = :gameId")
    suspend fun getGamePlayers(gameId: Long): List<GamePlayer>

    // Game Player Hole Throws
    @Insert
    suspend fun insertGamePlayerHoleThrow(gamePlayerHoleThrow: GamePlayerHoleThrow)

    @Insert
    suspend fun insertGamePlayerHoleThrows(gamePlayerHoleThrows: List<GamePlayerHoleThrow>)

    @Update
    suspend fun updateGamePlayerHoleThrow(gamePlayerHoleThrow: GamePlayerHoleThrow)

    @Query("SELECT * FROM game_player_hole_throws WHERE gameId = :gameId AND playerId = :playerId AND holeNumber = :holeNumber")
    suspend fun getGamePlayerHoleThrow(gameId: Long, playerId: Long, holeNumber: Int): GamePlayerHoleThrow?

    @Query("SELECT * FROM game_player_hole_throws WHERE gameId = :gameId ORDER BY holeNumber ASC, playerId ASC")
    suspend fun getGamePlayerHoleThrows(gameId: Long): List<GamePlayerHoleThrow>
}
