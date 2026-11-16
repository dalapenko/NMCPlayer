package com.dalapenko.laba.feature.player

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import com.dalapenko.laba.TestFixtures
import com.dalapenko.laba.assertSystemFontScale
import com.dalapenko.laba.assertTextHeightGrows
import com.dalapenko.laba.saveDeviceScreenshot
import com.dalapenko.laba.ui.components.AppModalBottomSheet
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PlayerFilesScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun modalTitleStaysSixteenDpBelowHandleAtActualSystemFontScale() {
        val fontScale = assertSystemFontScale(composeRule.activity, "PlayerFilesGeometry")
        val viewModel = mockk<PlayerFilesViewModel>()
        every { viewModel.uiState } returns MutableStateFlow(
            PlayerFilesUiState(
                title = "Test Book",
                tracks = listOf(
                    TestFixtures.testTrack(
                        id = 1L,
                        bookId = 1L,
                        fileName = "Chapter 1",
                        sequenceOrder = 0,
                        durationMs = 60_000L,
                    ),
                ),
                isLoading = false,
            ),
        )
        composeRule.setContent {
            MaterialTheme {
                AppModalBottomSheet(onDismissRequest = {}) {
                    PlayerFilesScreen(
                        bookId = 1L,
                        onBack = {},
                        modalSheetHandle = true,
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        val title = composeRule.onNodeWithTag("player_files_title").fetchSemanticsNode().boundsInRoot
        val handle = composeRule.onNodeWithTag(
            "app_modal_sheet_handle",
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot
        val gap = with(composeRule.density) { (title.top - handle.bottom).toDp().value }
        assertTextHeightGrows(fontScale, title.height, "playerFilesTitleHeightPx", "PlayerFilesGeometry")
        android.util.Log.i(
            "PlayerFilesGeometry",
            "systemFontScale=$fontScale titleHeight=${title.height}px handleToTitle=$gap dp",
        )
        check(kotlin.math.abs(gap - 16f) <= 1f) {
            "systemFontScale=$fontScale handle-to-title=$gap dp"
        }
        composeRule.onNodeWithTag("close_player_files_button")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertHeightIsAtLeast(48.dp)
        saveDeviceScreenshot("player-files-system-scale-$fontScale.png")
    }
}
