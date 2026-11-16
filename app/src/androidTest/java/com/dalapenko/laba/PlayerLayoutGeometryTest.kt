package com.dalapenko.laba

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

@LargeTest
class PlayerLayoutGeometryTest : BaseKaspressoTest() {
    private val book = TestFixtures.threeChapterBook()

    @Before
    fun openPlayer() {
        runBlocking {
            bookDao.insert(book.bookEntity)
            trackDao.insertAll(book.trackEntities)
        }
        launchActivity()
        composeTestRule.onNodeWithTag("book_card_${book.bookEntity.id}").performClick()
        composeTestRule.onNodeWithTag("player_screen").assertIsDisplayed()
    }

    @Test
    fun playerChildrenFitMeasuredWindowGeometry() {
        val contentNode = composeTestRule.onNodeWithTag("player_content_bounds").fetchSemanticsNode()
        val coverNode = composeTestRule.onNodeWithTag("player_cover_art").fetchSemanticsNode()
        val contentTop = contentNode.positionInRoot.y
        val contentBottom = contentTop + contentNode.size.height
        val coverTop = coverNode.positionInRoot.y
        val coverBottom = coverTop + coverNode.size.height
        val widthDp = with(composeTestRule.density) { contentNode.size.width.toDp() }
        val heightDp = with(composeTestRule.density) { contentNode.size.height.toDp() }
        android.util.Log.i(
            "PlayerGeometry",
            "window=${widthDp.value}x${heightDp.value}dp cover=$coverTop..$coverBottom",
        )

        if (widthDp < 720.dp) {
            val speedNode = composeTestRule.onNodeWithTag("speed_control").fetchSemanticsNode()
            val speedBottom = speedNode.positionInRoot.y + speedNode.size.height
            val topMargin = coverTop - contentTop
            val bottomMargin = contentBottom - speedBottom
            val coverHeight = with(composeTestRule.density) { coverNode.size.height.toDp() }
            android.util.Log.i("PlayerGeometry", "portrait cover=$coverHeight top=$topMargin bottom=$bottomMargin")

            check(coverHeight <= 181.dp) { "Portrait cover was $coverHeight, expected approximately 180.dp" }
            if (coverHeight > 0.dp) {
                check(kotlin.math.abs(topMargin - bottomMargin) <= with(composeTestRule.density) { 2.dp.toPx() }) {
                    "Portrait outer margins differ: top=$topMargin px, bottom=$bottomMargin px"
                }
            } else {
                check(speedBottom > contentBottom) {
                    "A zero-sized portrait cover is only valid when the remaining controls genuinely overflow"
                }
            }
        } else {
            val leftNode = composeTestRule.onNodeWithTag("player_left_column").fetchSemanticsNode()
            val leftTop = leftNode.positionInRoot.y
            val leftBottom = leftTop + leftNode.size.height
            val seekNode = composeTestRule.onNodeWithTag("position_slider").fetchSemanticsNode()
            val seekTop = seekNode.positionInRoot.y
            val seekBottom = seekTop + seekNode.size.height
            val leftScrollRange = scrollRange("player_left_column")
            val rightScrollRange = scrollRange("player_right_column")
            val knownFitViewport = widthDp >= 720.dp && heightDp >= 250.dp

            if (knownFitViewport) {
                check(leftScrollRange == 0f) {
                    "Known-fit wide viewport unexpectedly scrolls on the left: $leftScrollRange px"
                }
                check(rightScrollRange == 0f) {
                    "Known-fit wide viewport unexpectedly scrolls on the right: $rightScrollRange px"
                }
                check(coverTop >= leftTop && coverBottom <= leftBottom) {
                    "Wide cover exceeded left column bounds: cover=$coverTop..$coverBottom, " +
                        "left=$leftTop..$leftBottom"
                }
                check(seekTop >= leftTop && seekBottom <= leftBottom) {
                    "Wide progress/seek exceeded left column bounds: seek=$seekTop..$seekBottom, " +
                        "left=$leftTop..$leftBottom"
                }
                composeTestRule.onNodeWithTag("position_elapsed").assertIsDisplayed()
                composeTestRule.onNodeWithTag("position_remaining").assertIsDisplayed()
            } else {
                android.util.Log.i(
                    "PlayerGeometry",
                    "wide non-fit viewport ranges left=$leftScrollRange right=$rightScrollRange " +
                        "seek=$seekTop..$seekBottom viewport=$leftTop..$leftBottom",
                )
            }
            val playNode = composeTestRule.onNodeWithTag("play_pause_button").fetchSemanticsNode()
            val speedNode = composeTestRule.onNodeWithTag("speed_control").fetchSemanticsNode()
            val playBottom = playNode.positionInRoot.y + playNode.size.height
            val speedGap = with(composeTestRule.density) {
                (speedNode.positionInRoot.y - playBottom).toDp()
            }
            val expectedGap = if (heightDp < 500.dp) 16.dp else 32.dp
            android.util.Log.i(
                "PlayerGeometry",
                "wide cover=$coverTop..$coverBottom left=$leftTop..$leftBottom " +
                    "seek=$seekTop..$seekBottom speedGap=$speedGap",
            )
            check(kotlin.math.abs(speedGap.value - expectedGap.value) <= 1f) {
                "Wide controls-to-speed gap was $speedGap, expected $expectedGap"
            }
        }

        saveUiEvidence(
            composeTestRule.onRoot().captureToImage(),
            "player-${widthDp.value.toInt()}x${heightDp.value.toInt()}.png",
        )
    }

    @Test
    fun wideOverflowCanScrollToSpeedActionAndOpenItsSheet() {
        val content = composeTestRule.onNodeWithTag("player_content_bounds").fetchSemanticsNode()
        val widthDp = with(composeTestRule.density) { content.size.width.toDp() }
        val heightDp = with(composeTestRule.density) { content.size.height.toDp() }
        if (widthDp < 720.dp || heightDp >= 200.dp) {
            android.util.Log.i("PlayerGeometry", "overflow interaction not applicable at ${widthDp}x${heightDp}dp")
            return
        }

        val rightColumn = composeTestRule.onNodeWithTag("player_right_column").fetchSemanticsNode()
        val rightScrollRange = rightColumn.config[SemanticsProperties.VerticalScrollAxisRange].maxValue()
        check(rightScrollRange > 0f) { "Expected a real right-column overflow, got scroll range $rightScrollRange" }

        composeTestRule.onNodeWithTag("speed_preset_4").performScrollTo()
        composeTestRule.onNodeWithTag("speed_preset_4").assertIsDisplayed().performClick()
        verify(exactly = 1) { playbackController.setSpeed(2f) }
    }

    private fun scrollRange(tag: String): Float {
        val node = composeTestRule.onNodeWithTag(tag).fetchSemanticsNode()
        val range = node.config[SemanticsProperties.VerticalScrollAxisRange].maxValue()
        android.util.Log.i("PlayerGeometry", "scrollRange $tag=$range px")
        return range
    }
}
