package com.dalapenko.laba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.filters.LargeTest
import kotlinx.coroutines.runBlocking
import org.junit.Test

@LargeTest
class PlayerProgressPresentationTest : BaseKaspressoTest() {
    @Test
    fun singleTrackKeepsFilenameAndSeekTimesButHidesProgressRowsAndCounter() {
        val bookId = 84L
        val fileName = "Single chapter.m4b"
        runBlocking {
            bookDao.insert(
                TestFixtures.testBook(
                    id = bookId,
                    title = "Single file",
                    totalDurationMs = 60_000L,
                ),
            )
            trackDao.insertAll(
                listOf(
                    TestFixtures.testTrack(
                        id = bookId,
                        bookId = bookId,
                        fileName = fileName,
                        sequenceOrder = 0,
                        durationMs = 60_000L,
                    ),
                ),
            )
        }
        launchActivity()

        composeTestRule.onNodeWithTag("book_card_$bookId").performClick()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithTag("player_track_file_name").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("player_track_file_name")
            .assertIsDisplayed()
            .assertTextContains(fileName)
        assertNoNode("current_file_progress_info")
        assertNoNode("total_progress_info")
        assertNoNode("player_file_position")
        composeTestRule.onNodeWithTag("position_slider").assertIsDisplayed()
        composeTestRule.onNodeWithTag("position_elapsed").assertIsDisplayed()
        composeTestRule.onNodeWithTag("position_remaining").assertIsDisplayed()
    }

    private fun assertNoNode(tag: String) {
        check(composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()) {
            "Expected '$tag' to be absent"
        }
    }
}
