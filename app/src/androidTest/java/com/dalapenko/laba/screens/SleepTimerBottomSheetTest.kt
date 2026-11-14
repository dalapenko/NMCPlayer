package com.dalapenko.laba.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.dalapenko.laba.feature.player.PlayerControlModal
import com.dalapenko.laba.feature.player.SleepTimerBottomSheet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SleepTimerBottomSheetTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun presetStartsFixedTimerAndEndOfFileModeUsesExistingCallback() {
        var fixedMinutes = 0
        var endOfFileStarted = false
        composeRule.setContent {
            MaterialTheme {
                SleepTimerBottomSheet(
                    isActive = false,
                    remainingMs = 0L,
                    remainingChapterMs = 90_000L,
                    initialFixedMinutes = 20,
                    onStartFixedDuration = { fixedMinutes = it },
                    onStartEndOfChapter = { endOfFileStarted = true },
                    onCancel = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag("sleep_timer_mode_time").assertIsSelected()
        composeRule.onNodeWithTag("sleep_timer_preset_30").performClick()
        composeRule.onNodeWithTag("sleep_timer_start_button").performClick()
        composeRule.runOnIdle { assertEquals(30, fixedMinutes) }

        composeRule.onNodeWithTag("sleep_timer_mode_end_of_file").performClick()
        composeRule.onNodeWithTag("sleep_timer_mode_end_of_file").assertIsSelected()
        composeRule.onNodeWithTag("sleep_timer_start_button").performClick()
        composeRule.runOnIdle { assertTrue(endOfFileStarted) }
    }

    @Test
    fun narrowControlModal_scrollsToReachActionsAfterOversizedContent() {
        var actionInvoked = false
        composeRule.setContent {
            MaterialTheme {
                PlayerControlModal(isWide = false, onDismiss = {}) {
                    Spacer(Modifier.height(1000.dp))
                    Button(
                        onClick = { actionInvoked = true },
                        modifier = Modifier.testTag("scroll_reachable_action"),
                    ) {
                        Text("Action")
                    }
                }
            }
        }

        composeRule.onNodeWithTag("scroll_reachable_action")
            .performScrollTo()
            .performClick()
        composeRule.runOnIdle { assertTrue(actionInvoked) }
    }
}
