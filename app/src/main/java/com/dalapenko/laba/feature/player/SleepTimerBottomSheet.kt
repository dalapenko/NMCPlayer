package com.dalapenko.laba.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dalapenko.laba.R
import java.util.Locale

private enum class SleepTimerMode { TIME, END_OF_FILE }

private val SLEEP_TIMER_PRESETS = listOf(15, 30, 45)

@Composable
fun SleepTimerBottomSheet(
    isActive: Boolean,
    remainingMs: Long,
    remainingChapterMs: Long,
    initialFixedMinutes: Int,
    onStartFixedDuration: (Int) -> Unit,
    onStartEndOfChapter: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    isWide: Boolean = false,
) {
    PlayerControlModal(isWide = isWide, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("sleep_timer_bottom_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.control_sleep_timer_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("sleep_timer_title"),
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("sleep_timer_close")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.control_close),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isActive) {
                ActiveSleepTimerContent(remainingMs = remainingMs, onCancel = onCancel)
            } else {
                ConfigureSleepTimerContent(
                    remainingChapterMs = remainingChapterMs,
                    initialFixedMinutes = initialFixedMinutes,
                    onStartFixedDuration = onStartFixedDuration,
                    onStartEndOfChapter = onStartEndOfChapter,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActiveSleepTimerContent(remainingMs: Long, onCancel: () -> Unit) {
    Text(
        text = stringResource(R.string.control_sleep_timer_active_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(12.dp))
    Text(
        text = formatCountdown(remainingMs),
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("sleep_timer_remaining"),
    )
    Spacer(Modifier.height(24.dp))
    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sleep_timer_cancel_button"),
    ) {
        Text(stringResource(R.string.control_sleep_timer_cancel))
    }
}

@Suppress("LongMethod") // Timer configuration is one cohesive Compose form with shared selection state.
@Composable
private fun ConfigureSleepTimerContent(
    remainingChapterMs: Long,
    initialFixedMinutes: Int,
    onStartFixedDuration: (Int) -> Unit,
    onStartEndOfChapter: () -> Unit,
) {
    var mode by remember { mutableStateOf(SleepTimerMode.TIME) }
    var pendingMinutes by remember {
        mutableIntStateOf(initialFixedMinutes.coerceIn(1, SLEEP_TIMER_MAX_MINUTES))
    }
    var hasLocalEdit by remember { mutableStateOf(false) }

    LaunchedEffect(initialFixedMinutes) {
        if (!hasLocalEdit) {
            pendingMinutes = initialFixedMinutes.coerceIn(1, SLEEP_TIMER_MAX_MINUTES)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = mode == SleepTimerMode.TIME,
            onClick = { mode = SleepTimerMode.TIME },
            label = {
                Text(
                    text = stringResource(R.string.control_sleep_timer_after_time),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            modifier = Modifier.weight(1f).testTag("sleep_timer_mode_time"),
        )
        FilterChip(
            selected = mode == SleepTimerMode.END_OF_FILE,
            onClick = { mode = SleepTimerMode.END_OF_FILE },
            label = {
                Text(
                    text = stringResource(R.string.control_sleep_timer_end_of_file),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            },
            modifier = Modifier.weight(1f).testTag("sleep_timer_mode_end_of_file"),
        )
    }

    Spacer(Modifier.height(12.dp))
    when (mode) {
        SleepTimerMode.TIME -> {
            SleepTimerDial(
                minutes = pendingMinutes,
                onMinutesChanged = { newValue ->
                    hasLocalEdit = true
                    pendingMinutes = newValue
                },
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                SLEEP_TIMER_PRESETS.forEach { minutes ->
                    FilterChip(
                        selected = pendingMinutes == minutes,
                        onClick = {
                            hasLocalEdit = true
                            pendingMinutes = minutes
                        },
                        label = { Text(stringResource(R.string.control_sleep_timer_minutes, minutes)) },
                        modifier = Modifier.testTag("sleep_timer_preset_$minutes"),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.control_sleep_timer_time_description, pendingMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SleepTimerMode.END_OF_FILE -> {
            Text(
                text = stringResource(
                    R.string.control_sleep_timer_end_of_file_description,
                    formatCountdown(remainingChapterMs),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 24.dp),
            )
        }
    }

    Spacer(Modifier.height(16.dp))
    Button(
        onClick = {
            if (mode == SleepTimerMode.END_OF_FILE) {
                onStartEndOfChapter()
            } else {
                onStartFixedDuration(pendingMinutes)
            }
        },
        enabled = mode == SleepTimerMode.END_OF_FILE || pendingMinutes > 0,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sleep_timer_start_button"),
    ) {
        Text(stringResource(R.string.control_sleep_timer_start))
    }
}

private fun formatCountdown(remainingMs: Long): String {
    val totalSeconds = remainingMs / MILLIS_PER_SECOND
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}

private const val MILLIS_PER_SECOND = 1000
private const val SECONDS_PER_MINUTE = 60
