package com.dalapenko.laba.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import com.dalapenko.laba.TestFixtures
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PlayerFilesScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun lastTrackRemainsReachableInBoundedPanel() {
        val tracks = (0 until 10).map { index ->
            TestFixtures.testTrack(
                id = index + 1L,
                bookId = 1L,
                fileName = "Chapter ${index + 1}",
                sequenceOrder = index,
                durationMs = 60_000L,
            )
        }
        val viewModel = mockk<PlayerFilesViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(
            PlayerFilesUiState(
                title = "Test Book",
                tracks = tracks,
                isActiveBook = true,
                isLoading = false,
            ),
        )

        composeRule.setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    PlayerFilesScreen(
                        bookId = 1L,
                        onBack = {},
                        maxPanelHeight = 240.dp,
                        viewModel = viewModel,
                    )
                }
            }
        }

        composeRule.onNodeWithTag("player_files_list")
            .performScrollToNode(hasTestTag("player_file_9"))
        composeRule.onNodeWithTag("player_file_9").assertIsDisplayed()
    }
}
