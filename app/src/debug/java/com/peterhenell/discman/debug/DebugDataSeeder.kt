package com.peterhenell.discman.debug

import com.peterhenell.discman.data.entities.*
import com.peterhenell.discman.data.repository.DataStorageService
import kotlinx.coroutines.flow.first
import java.util.Date
import javax.inject.Inject

class DebugDataSeeder @Inject constructor(
    private val dataStorage: DataStorageService
) : DataSeeder {

    override suspend fun seed() {
        // Only seed once — skip if Alice already exists
        val existingPlayers = dataStorage.getAllPlayers().first()
        if (existingPlayers.any { it.name == "Alice" }) return

        // --- Players ---
        val aliceId = dataStorage.insertPlayer(Player(name = "Alice"))
        val bobId = dataStorage.insertPlayer(Player(name = "Bob"))

        // --- Course ---
        val courseId = dataStorage.insertCourse(
            Course(name = "Bergshamra", location = "Bergshamra, Sweden")
        )

        // --- Holes: 6 holes, hole 4 is par 4, rest are par 3 ---
        val holes = (1..6).map { holeNumber ->
            Hole(courseId = courseId, holeNumber = holeNumber, par = if (holeNumber == 4) 4 else 3)
        }
        dataStorage.insertHoles(holes)

        // Total par = 5*3 + 4 = 19

        // --- Game ---
        val gameId = dataStorage.insertGame(
            Game(courseId = courseId, startDate = Date())
        )

        // --- Add players to game ---
        dataStorage.insertGamePlayers(
            listOf(
                GamePlayer(gameId = gameId, playerId = aliceId),
                GamePlayer(gameId = gameId, playerId = bobId)
            )
        )

        // --- Alice: -1 under par (total 18 throws) ---
        // Hole 1-4: par, Hole 5: birdie (2), Hole 6: par
        val aliceThrows = listOf(3, 3, 3, 4, 2, 3)
        aliceThrows.forEachIndexed { index, throws ->
            dataStorage.insertGamePlayerHoleThrow(
                GamePlayerHoleThrow(
                    gameId = gameId,
                    playerId = aliceId,
                    holeNumber = index + 1,
                    numberOfThrows = throws
                )
            )
        }

        // --- Bob: +2 over par (total 21 throws) ---
        // Hole 1-2: par, Hole 3: bogey (4), Hole 4: par, Hole 5: par, Hole 6: bogey (4)
        val bobThrows = listOf(3, 3, 4, 4, 3, 4)
        bobThrows.forEachIndexed { index, throws ->
            dataStorage.insertGamePlayerHoleThrow(
                GamePlayerHoleThrow(
                    gameId = gameId,
                    playerId = bobId,
                    holeNumber = index + 1,
                    numberOfThrows = throws
                )
            )
        }
    }
}


