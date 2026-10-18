package com.dalapenko.laba.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.dalapenko.laba.feature.player.SleepTimerBottomSheet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SleepTimerBottomSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

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
}
