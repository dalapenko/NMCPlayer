package com.dalapenko.laba.core.media

import com.dalapenko.laba.core.database.dao.ProgressDao
import com.dalapenko.laba.core.database.entity.BookEntity
import com.dalapenko.laba.core.database.entity.ProgressEntity
import com.dalapenko.laba.core.database.entity.TrackEntity

class PlaybackPreparer(
    private val progressDao: ProgressDao,
    private val playbackController: PlaybackController,
    private val safPlaybackCache: SafPlaybackCache? = null,
) {

    suspend fun setupPlayback(
        bookId: Long,
        book: BookEntity,
        tracks: List<TrackEntity>,
        autoPlay: Boolean = false,
    ) {
        playbackController.captureCurrentBookState()

        if (playbackController.currentBookId.value == bookId) {
            if (autoPlay) playbackController.play()
            return
        }

        val progress = progressDao.getByBook(bookId)
        val targetTrackIndex = if (progress != null && !progress.isCompleted) {
            tracks.indexOfFirst { it.id == progress.lastTrackId }.takeIf { it >= 0 }
        } else {
            null
        }

        val playbackUris = safPlaybackCache?.cacheTracks(bookId, tracks)

        applyInitialState(progress, targetTrackIndex, tracks)

        playbackController.setPlaylist(
            buildPlaylistItems(tracks, book, playbackUris),
            bookId,
        )
        playbackController.setBookMetadata(
            bookId = bookId,
            trackIds = tracks.map { it.id },
            trackDurations = tracks.map { it.durationMs },
        )

        val targetSpeed = progress?.playbackSpeed
            ?.takeIf { targetTrackIndex != null }
            ?.coerceIn(MIN_PLAYBACK_SPEED, MAX_PLAYBACK_SPEED)
            ?: DEFAULT_PLAYBACK_SPEED
        playbackController.setSpeed(targetSpeed)

        resumeOrResetProgress(targetTrackIndex, progress, tracks)

        if (autoPlay) playbackController.play()
    }

    private fun applyInitialState(
        progress: ProgressEntity?,
        targetTrackIndex: Int?,
        tracks: List<TrackEntity>,
    ) {
        if (targetTrackIndex != null && progress != null) {
            playbackController.setInitialState(
                position = progress.lastPositionMs,
                duration = tracks[targetTrackIndex].durationMs,
                trackIndex = targetTrackIndex,
                speed = progress.playbackSpeed.coerceIn(MIN_PLAYBACK_SPEED, MAX_PLAYBACK_SPEED),
            )
        } else {
            playbackController.setInitialState(
                position = 0L,
                duration = tracks.firstOrNull()?.durationMs ?: 0L,
                trackIndex = 0,
                speed = DEFAULT_PLAYBACK_SPEED,
            )
        }
    }

    private fun buildPlaylistItems(
        tracks: List<TrackEntity>,
        book: BookEntity,
        playbackUris: List<String>?,
    ): List<PlaylistItem> {
        return tracks.mapIndexed { index, track ->
            PlaylistItem(
                uri = playbackUris?.getOrNull(index) ?: track.fileUri,
                title = track.fileName,
                artist = book.author,
                artworkUri = book.coverUri,
            )
        }
    }

    suspend fun clearBookCache(bookId: Long) {
        safPlaybackCache?.clearBook(bookId)
    }

    private suspend fun resumeOrResetProgress(
        targetTrackIndex: Int?,
        progress: ProgressEntity?,
        tracks: List<TrackEntity>,
    ) {
        if (targetTrackIndex != null && progress != null) {
            playbackController.seekToTrack(targetTrackIndex, progress.lastPositionMs)
        } else if (progress?.isCompleted == true) {
            progressDao.upsertIfNewer(
                progress.copy(
                    isCompleted = false,
                    lastPositionMs = 0L,
                    completedTracksMs = 0L,
                    lastTrackId = tracks.first().id,
                    lastUpdated = System.currentTimeMillis(),
                ),
            )
        }
    }
}

private const val MIN_PLAYBACK_SPEED = 0.5f
private const val MAX_PLAYBACK_SPEED = 2.0f
private const val DEFAULT_PLAYBACK_SPEED = 1.0f
