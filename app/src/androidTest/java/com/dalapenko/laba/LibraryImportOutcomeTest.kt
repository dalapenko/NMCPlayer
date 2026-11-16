package com.dalapenko.laba

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.filters.LargeTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dalapenko.laba.feature.library.LibraryImportOutcomeModal
import com.dalapenko.laba.feature.library.ScanResult
import com.dalapenko.laba.feature.player.ChapterBottomSheet
import com.dalapenko.laba.core.database.entity.TrackEntity
import com.dalapenko.laba.ui.theme.LabaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LibraryImportOutcomeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun successActionSaysCloseAndDismissesImportOutcome() {
        if (composeTestRule.activity.resources.configuration.screenWidthDp >= 600) {
            android.util.Log.i("ImportGeometry", "narrow success action test is not applicable to wide Dialog")
            return
        }
        composeTestRule.setContent {
            var visible by remember { mutableStateOf(true) }
            LabaTheme {
                if (visible) {
                    LibraryImportOutcomeModal(
                        isScanning = false,
                        sourceName = null,
                        result = ScanResult.Success("Imported book", audioFileCount = 1),
                        onDismiss = { visible = false },
                        onChooseSource = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("import_result_action").assertIsDisplayed()
        val expectedLabel = if (
            composeTestRule.activity.resources.configuration.locales[0].language == "ru"
        ) {
            "Закрыть"
        } else {
            "Close"
        }
        composeTestRule.onNodeWithText(expectedLabel)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("sheet_close")
            .assertHeightIsAtLeast(48.dp)
            .assertWidthIsAtLeast(48.dp)
        val titleBounds = composeTestRule.onNodeWithTag("import_result_title").fetchSemanticsNode().boundsInRoot
        val iconBounds = composeTestRule.onNodeWithTag("library_state_icon").fetchSemanticsNode().boundsInRoot
        val fileInfoBounds = composeTestRule.onNodeWithTag("import_file_info").fetchSemanticsNode().boundsInRoot
        val handleBounds = composeTestRule.onNodeWithTag(
            "app_modal_sheet_handle",
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot
        val titleToIcon = iconBounds.top - titleBounds.bottom
        val iconToFileInfo = fileInfoBounds.top - iconBounds.bottom
        val tolerancePx = with(composeTestRule.density) { 1.dp.toPx() }
        val handleToTitle = titleBounds.top - handleBounds.bottom
        val targetGapPx = with(composeTestRule.density) { 16.dp.toPx() }
        android.util.Log.i(
            "ImportGeometry",
            "handleToTitle=$handleToTitle " +
                "titleToIcon=$titleToIcon iconToFile=$iconToFileInfo density=${composeTestRule.density.density}",
        )
        check(kotlin.math.abs(handleToTitle - targetGapPx) <= tolerancePx) {
            "Expected 16dp handle-to-title gap, got $handleToTitle px"
        }
        check(kotlin.math.abs(titleToIcon - targetGapPx) <= tolerancePx) {
            "Expected 16dp title-to-check gap, got $titleToIcon px"
        }
        check(kotlin.math.abs(iconToFileInfo - targetGapPx) <= tolerancePx) {
            "Expected 16dp check-to-info gap, got $iconToFileInfo px"
        }
        check(kotlin.math.abs(titleToIcon - iconToFileInfo) <= tolerancePx) {
            "Import success gaps differ: title-to-check=$titleToIcon px, check-to-info=$iconToFileInfo px"
        }
        saveUiEvidence(composeTestRule.onRoot().captureToImage(), "import-success.png")
        composeTestRule.onNodeWithTag("import_result_action").performClick()

        check(composeTestRule.onAllNodesWithTag("import_result_sheet").fetchSemanticsNodes().isEmpty()) {
            "Expected the existing dismiss callback to close the import result"
        }
    }

    @Test
    fun allImportOutcomesUseActualSystemFontScaleAndKeepInnerGaps() {
        val isWide = composeTestRule.activity.resources.configuration.screenWidthDp >= 600
        val fontScale = assertSystemFontScale(composeTestRule.activity, "ImportGeometry")
        var isScanning by mutableStateOf(false)
        var outcome by mutableStateOf<ScanResult>(ScanResult.Empty)
        composeTestRule.setContent {
            LabaTheme {
                LibraryImportOutcomeModal(
                    isScanning = isScanning,
                    sourceName = "Folder",
                    result = if (isScanning) null else outcome,
                    onDismiss = {},
                    onChooseSource = {},
                )
            }
        }
        val outcomes = listOf(
            ScanResult.Success("An audiobook with a long title", audioFileCount = 2),
            ScanResult.Empty,
            ScanResult.Duplicate("A folder with a long name that wraps at larger text"),
            ScanResult.PermissionError,
        )
        val baselineKey = if (isWide) "wideTitleHeightPx" else "narrowTitleHeightPx"
        outcomes.forEach { currentOutcome ->
            composeTestRule.runOnIdle {
                isScanning = false
                outcome = currentOutcome
            }
            composeTestRule.waitForIdle()
            val titleNode = composeTestRule.onNodeWithTag("import_result_title")
            titleNode.assertIsDisplayed()
            val title = composeTestRule.onNodeWithTag("import_result_title").fetchSemanticsNode().boundsInRoot
            val icon = composeTestRule.onNodeWithTag("library_state_icon").fetchSemanticsNode().boundsInRoot
            val handle = composeTestRule.onNodeWithTag(
                "app_modal_sheet_handle",
                useUnmergedTree = true,
            ).fetchSemanticsNode().boundsInRoot
            val messageTag = if (currentOutcome is ScanResult.Success) {
                "import_file_info"
            } else {
                "import_outcome_message"
            }
            val message = composeTestRule.onNodeWithTag(messageTag)
                .fetchSemanticsNode().boundsInRoot
            val titleToIcon = with(composeTestRule.density) { (icon.top - title.bottom).toDp().value }
            val iconToMessage = with(composeTestRule.density) { (message.top - icon.bottom).toDp().value }
            val handleToTitle = with(composeTestRule.density) { (title.top - handle.bottom).toDp().value }
            assertTextHeightGrows(fontScale, title.height, baselineKey, "ImportGeometry")
            android.util.Log.i(
                "ImportGeometry",
                "outcome=${currentOutcome::class.simpleName} systemFontScale=$fontScale " +
                    "titleHeight=${title.height}px handleToTitle=$handleToTitle " +
                    "titleToIcon=$titleToIcon iconToMessage=$iconToMessage",
            )
            if (isWide) {
                val expectedWideMessageGap = if (currentOutcome is ScanResult.Success) 16f else 8f
                check(kotlin.math.abs(iconToMessage - expectedWideMessageGap) <= 1f) {
                    "wide outcome=$currentOutcome fontScale=$fontScale icon-to-message=$iconToMessage dp"
                }
            } else {
                check(kotlin.math.abs(handleToTitle - 16f) <= 1f) {
                    "outcome=$currentOutcome fontScale=$fontScale handle-to-title=$handleToTitle dp"
                }
                check(kotlin.math.abs(titleToIcon - 16f) <= 1f) {
                    "outcome=$currentOutcome fontScale=$fontScale title-to-icon=$titleToIcon dp"
                }
                check(kotlin.math.abs(iconToMessage - 16f) <= 1f) {
                    "outcome=$currentOutcome fontScale=$fontScale icon-to-message=$iconToMessage dp"
                }
            }
            saveDeviceScreenshot(
                "import-${currentOutcome::class.simpleName}-system-scale-$fontScale.png",
            )
        }
        composeTestRule.runOnIdle {
            isScanning = true
        }
        composeTestRule.waitForIdle()
        if (!isWide) {
            val title = composeTestRule.onNodeWithTag("import_result_title").fetchSemanticsNode().boundsInRoot
            val handle = composeTestRule.onNodeWithTag(
                "app_modal_sheet_handle",
                useUnmergedTree = true,
            ).fetchSemanticsNode().boundsInRoot
            val gapDp = with(composeTestRule.density) { (title.top - handle.bottom).toDp().value }
            check(kotlin.math.abs(gapDp - 16f) <= 1f) {
                "Scanning systemFontScale=$fontScale handle-to-title=$gapDp dp"
            }
        }
    }

    @Test
    fun chapterSheetTitleUsesTheSharedHandleGapAtActualSystemFontScale() {
        val fontScale = assertSystemFontScale(composeTestRule.activity, "ChapterGeometry")
        composeTestRule.setContent {
            LabaTheme {
                ChapterBottomSheet(
                    tracks = listOf(
                        TrackEntity(
                            bookId = 1L,
                            fileUri = "content://test",
                            fileName = "Chapter",
                            durationMs = 60_000L,
                            sequenceOrder = 0,
                        ),
                    ),
                    currentTrackIndex = 0,
                    onTrackSelected = {},
                    onDismiss = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        val title = composeTestRule.onNodeWithTag("chapters_title").fetchSemanticsNode().boundsInRoot
        val handle = composeTestRule.onNodeWithTag(
            "app_modal_sheet_handle",
            useUnmergedTree = true,
        ).fetchSemanticsNode().boundsInRoot
        val gap = with(composeTestRule.density) { (title.top - handle.bottom).toDp().value }
        assertTextHeightGrows(fontScale, title.height, "chapterTitleHeightPx", "ChapterGeometry")
        android.util.Log.i(
            "ChapterGeometry",
            "systemFontScale=$fontScale titleHeight=${title.height}px handleToTitle=$gap dp",
        )
        check(kotlin.math.abs(gap - 16f) <= 1f) { "Chapter systemFontScale=$fontScale handle-to-title=$gap dp" }
        saveDeviceScreenshot("chapters-system-scale-$fontScale.png")
    }
}
