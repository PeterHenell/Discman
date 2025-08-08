package com.peterhenell.discman.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "game_player_hole_throws",
    primaryKeys = ["gameId", "playerId", "holeNumber"],
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["gameId"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["playerId"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameId"]), Index(value = ["playerId"])]
)
data class GamePlayerHoleThrow(
    val gameId: Long,
    val playerId: Long,
    val holeNumber: Int,
    val numberOfThrows: Int
)
