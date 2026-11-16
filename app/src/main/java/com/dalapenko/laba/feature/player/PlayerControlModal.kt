package com.dalapenko.laba.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dalapenko.laba.ui.components.AppModalBottomSheet
import com.dalapenko.laba.ui.components.AppModalSheetHandle

private const val CONTROL_PANEL_MAX_HEIGHT_FRACTION = 0.82f

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod") // Wide and narrow modal layouts share dismissal and content behavior.
@Composable
internal fun PlayerControlModal(
    isWide: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (isWide) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Surface(
                    modifier = Modifier
                        .width(420.dp)
                        .heightIn(max = maxHeight * CONTROL_PANEL_MAX_HEIGHT_FRACTION)
                        .clickable(onClick = {})
                        .testTag("player_control_dialog_surface"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        AppModalSheetHandle()
                        content()
                    }
                }
            }
        }
    } else {
        AppModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.testTag("player_control_sheet_surface"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                content()
            }
        }
    }
}
