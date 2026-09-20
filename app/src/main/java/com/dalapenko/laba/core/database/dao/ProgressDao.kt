package com.dalapenko.laba.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.dalapenko.laba.core.database.entity.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Query("SELECT * FROM progress WHERE bookId = :bookId")
    suspend fun getByBook(bookId: Long): ProgressEntity?

    @Query("SELECT bookId FROM progress ORDER BY lastUpdated DESC LIMIT 1")
    fun observeLastPlayedBookId(): Flow<Long?>

    @Query("SELECT * FROM progress")
    fun observeAll(): Flow<List<ProgressEntity>>

    @Upsert
    suspend fun upsert(progress: ProgressEntity)

    /**
     * Persists a snapshot without allowing an older asynchronous writer to roll progress back.
     */
    @Transaction
    suspend fun upsertIfNewer(progress: ProgressEntity) {
        val updatedRows = updateIfNewer(
            bookId = progress.bookId,
            lastTrackId = progress.lastTrackId,
            lastPositionMs = progress.lastPositionMs,
            completedTracksMs = progress.completedTracksMs,
            lastUpdated = progress.lastUpdated,
            isCompleted = progress.isCompleted,
            playbackSpeed = progress.playbackSpeed,
        )
        if (updatedRows == 0) insertIfAbsent(progress)
    }

    @Suppress("LongParameterList")
    @Query(
        """
        UPDATE progress SET
            lastTrackId = :lastTrackId,
            lastPositionMs = :lastPositionMs,
            completedTracksMs = :completedTracksMs,
            lastUpdated = :lastUpdated,
            isCompleted = :isCompleted,
            playbackSpeed = :playbackSpeed
        WHERE bookId = :bookId AND lastUpdated < :lastUpdated
        """,
    )
    suspend fun updateIfNewer(
        bookId: Long,
        lastTrackId: Long,
        lastPositionMs: Long,
        completedTracksMs: Long,
        lastUpdated: Long,
        isCompleted: Boolean,
        playbackSpeed: Float,
    ): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(progress: ProgressEntity): Long
}
