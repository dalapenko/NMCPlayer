package com.dalapenko.laba.feature.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LanguageDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectingLanguageAppliesOnlyAfterDone() {
        var wasApplied = false
        var appliedTag: String? = null
        composeRule.setContent {
            MaterialTheme {
                LanguageDialog(
                    currentTag = "ru",
                    onSelect = {
                        wasApplied = true
                        appliedTag = it
                    },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithTag("settings_language_option_en").performClick()
        composeRule.runOnIdle { assertFalse(wasApplied) }

        composeRule.onNodeWithTag("settings_language_done_button").performClick()
        composeRule.runOnIdle {
            assertTrue(wasApplied)
            assertEquals("en", appliedTag)
        }
    }

    @Test
    fun closingDialogDoesNotApplyStagedLanguage() {
        var wasApplied = false
        var wasDismissed = false
        composeRule.setContent {
            MaterialTheme {
                LanguageDialog(
                    currentTag = "ru",
                    onSelect = { wasApplied = true },
                    onDismiss = { wasDismissed = true },
                )
            }
        }

        composeRule.onNodeWithTag("settings_language_option_en").performClick()
        composeRule.onNodeWithTag("settings_language_close_button").performClick()
        composeRule.runOnIdle {
            assertTrue(wasDismissed)
            assertFalse(wasApplied)
        }
    }
}
