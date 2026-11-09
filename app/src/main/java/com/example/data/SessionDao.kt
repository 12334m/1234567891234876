package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM hr_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<HeartRateSession>>

    @Query("SELECT * FROM hr_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): HeartRateSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HeartRateSession): Long

    @Delete
    suspend fun deleteSession(session: HeartRateSession)

    @Query("DELETE FROM hr_sessions")
    suspend fun clearAll()
}
