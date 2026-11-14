package com.dalapenko.laba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.filters.LargeTest
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@LargeTest
class PlayerFilesPaneTest : BaseKaspressoTest() {
    private val book = TestFixtures.threeChapterBook()

    @Before
    fun setupTestData() {
        runBlocking {
            bookDao.insert(book.bookEntity)
            trackDao.insertAll(book.trackEntities)
        }
        launchActivity()
    }

    @Test
    fun multiTrackBookOpensFilesPaneAndCloseReturnsToPlayer() {
        composeTestRule.onRoot().assertWidthIsAtLeast(1200.dp)

        composeTestRule.onNodeWithTag("book_card_${book.bookEntity.id}").performClick()

        composeTestRule.onNodeWithTag("player_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_content_bounds").assertHeightIsAtLeast(500.dp)
        composeTestRule.onNodeWithTag("player_track_file_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_file_position").assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_files_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("player_file_0").assertIsDisplayed()

        composeTestRule.onNodeWithTag("close_player_files_button").performClick()

        composeTestRule.onNodeWithTag("player_screen").assertIsDisplayed()
        check(composeTestRule.onAllNodesWithTag("player_files_screen").fetchSemanticsNodes().isEmpty()) {
            "Expected the Files pane to be absent after closing it"
        }
    }
}
