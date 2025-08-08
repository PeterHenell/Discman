package com.peterhenell.discman.data.dao

import androidx.room.*
import com.peterhenell.discman.data.entities.Hole
import kotlinx.coroutines.flow.Flow

@Dao
interface HoleDao {
    @Query("SELECT * FROM holes WHERE courseId = :courseId ORDER BY holeNumber ASC")
    fun getHolesByCourse(courseId: Long): Flow<List<Hole>>

    @Query("SELECT * FROM holes WHERE courseId = :courseId AND holeNumber = :holeNumber")
    suspend fun getHole(courseId: Long, holeNumber: Int): Hole?

    @Insert
    suspend fun insertHole(hole: Hole): Long

    @Insert
    suspend fun insertHoles(holes: List<Hole>)

    @Update
    suspend fun updateHole(hole: Hole)

    @Delete
    suspend fun deleteHole(hole: Hole)

    @Query("DELETE FROM holes WHERE courseId = :courseId")
    suspend fun deleteHolesByCourse(courseId: Long)
}
