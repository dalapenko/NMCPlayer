package com.dalapenko.laba.feature.player

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import com.dalapenko.laba.assertSystemFontScale
import com.dalapenko.laba.assertTextHeightGrows
import com.dalapenko.laba.saveDeviceScreenshot
import com.dalapenko.laba.ui.theme.LabaTheme
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

@LargeTest
class TrackUnavailableGeometryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun narrowTitleIsSixteenDpBelowHandleAtActualSystemFontScale() {
        val fontScale = assertSystemFontScale(composeRule.activity, "TrackUnavailableGeometry")
        composeRule.setContent {
            LabaTheme {
                TrackUnavailableOverlay(
                    error = PlayerEvent.TrackUnavailable("An unavailable audiobook file", 0),
                    canRetry = false,
                    narrow = true,
                    panelMaxHeight = 600.dp,
                    onDismiss = {},
                    onRetry = {},
                    onChooseAnotherFile = {},
                )
            }
        }
        composeRule.waitForIdle()
        val title = composeRule.onNodeWithTag("player_audio_error_title").fetchSemanticsNode().boundsInRoot
        val handle = composeRule.onNodeWithTag(
            "app_modal_sheet_handle",
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot
        val gapDp = with(composeRule.density) { (title.top - handle.bottom).toDp().value }
        assertTextHeightGrows(fontScale, title.height, "trackTitleHeightPx", "TrackUnavailableGeometry")
        android.util.Log.i(
            "TrackUnavailableGeometry",
            "systemFontScale=$fontScale titleHeight=${title.height}px handleToTitle=$gapDp dp",
        )
        check(abs(gapDp - 16f) <= 1f) {
            "systemFontScale=$fontScale handle-to-title=$gapDp dp"
        }
        composeRule.onNodeWithTag("player_audio_error_close_button")
            .assertIsDisplayed()
            .assertHasClickAction()
        saveDeviceScreenshot("track-unavailable-system-scale-$fontScale.png")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun wideTitleRemainsVerticallyCenteredWithCloseButton() {
        val fontScale = assertSystemFontScale(composeRule.activity, "TrackUnavailableGeometryWide")
        composeRule.setContent {
            LabaTheme {
                TrackUnavailableOverlay(
                    error = PlayerEvent.TrackUnavailable("Unavailable file", 0),
                    canRetry = false,
                    narrow = false,
                    panelMaxHeight = 600.dp,
                    onDismiss = {},
                    onRetry = {},
                    onChooseAnotherFile = {},
                )
            }
        }
        composeRule.waitForIdle()
        val title = composeRule.onNodeWithTag("player_audio_error_title").fetchSemanticsNode().boundsInRoot
        val close = composeRule.onNodeWithTag("player_audio_error_close_button").fetchSemanticsNode().boundsInRoot
        val centerDelta = with(composeRule.density) { abs(title.center.y - close.center.y).toDp().value }
        assertTextHeightGrows(fontScale, title.height, "wideTrackTitleHeightPx", "TrackUnavailableGeometryWide")
        android.util.Log.i(
            "TrackUnavailableGeometryWide",
            "systemFontScale=$fontScale titleHeight=${title.height}px centerDelta=$centerDelta dp",
        )
        check(centerDelta <= 1f) { "Wide title/close center delta=$centerDelta dp" }
        saveDeviceScreenshot("track-unavailable-wide-system-scale-$fontScale.png")
    }
}
