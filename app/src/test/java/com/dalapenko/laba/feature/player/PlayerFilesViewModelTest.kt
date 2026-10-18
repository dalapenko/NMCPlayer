package com.dalapenko.laba.feature.player

import app.cash.turbine.test
import com.dalapenko.laba.MainDispatcherRule
import com.dalapenko.laba.core.data.BookRepository
import com.dalapenko.laba.core.media.PlaybackController
import com.dalapenko.laba.core.media.PlayerState
import com.dalapenko.laba.testBook
import com.dalapenko.laba.testTrack
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerFilesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun positionOnlyPlayerStateChangesDoNotEmitFileListUiUpdates() = runTest {
        val bookId = 42L
        val book = testBook(id = bookId)
        val tracks = listOf(
            testTrack(id = 1L, bookId = bookId, durationMs = 60_000L, sequenceOrder = 0),
            testTrack(id = 2L, bookId = bookId, durationMs = 90_000L, sequenceOrder = 1),
        )
        val repository = mockk<BookRepository>()
        val playbackController = mockk<PlaybackController>()
        val playerState = MutableStateFlow(PlayerState(currentPositionMs = 12_000L))
        val currentBookId = MutableStateFlow<Long?>(bookId)
        coEvery { repository.getBookWithTracks(bookId) } returns (book to tracks)
        every { playbackController.playerState } returns playerState
        every { playbackController.currentBookId } returns currentBookId

        val viewModel = PlayerFilesViewModel(bookId, repository, playbackController)
        advanceUntilIdle()

        viewModel.uiState.test {
            val loadedState = awaitItem()
            assertTrue(loadedState.isActiveBook)
            assertFalse(loadedState.isPlaying)

            playerState.value = playerState.value.copy(currentPositionMs = 12_250L)
            advanceUntilIdle()
            expectNoEvents()

            playerState.value = playerState.value.copy(currentPositionMs = 12_500L)
            advanceUntilIdle()
            expectNoEvents()

            playerState.value = playerState.value.copy(currentMediaItemIndex = 1)
            advanceUntilIdle()
            assertEquals(1, awaitItem().currentTrackIndex)

            playerState.value = playerState.value.copy(isPlaying = true)
            advanceUntilIdle()
            assertTrue(awaitItem().isPlaying)
            cancelAndConsumeRemainingEvents()
        }
    }
}
