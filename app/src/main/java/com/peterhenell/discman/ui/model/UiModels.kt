package com.peterhenell.discman.ui.model

data class CourseWithHoles(
    val course: com.peterhenell.discman.data.entities.Course,
    val holes: List<com.peterhenell.discman.data.entities.Hole>
)

data class GameWithDetails(
    val game: com.peterhenell.discman.data.entities.Game,
    val course: com.peterhenell.discman.data.entities.Course,
    val players: List<com.peterhenell.discman.data.entities.Player>,
    val holes: List<com.peterhenell.discman.data.entities.Hole>,
    val throws: List<com.peterhenell.discman.data.entities.GamePlayerHoleThrow>
)

data class PlayerScore(
    val player: com.peterhenell.discman.data.entities.Player,
    val holeScores: Map<Int, Int>, // hole number to number of throws
    val totalScore: Int,
    val totalThrows: Int
)
