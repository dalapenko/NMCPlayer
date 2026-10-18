package com.dalapenko.laba.feature.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dalapenko.laba.R
import java.util.Locale
import kotlin.math.roundToInt

private val SPEED_PRESETS = listOf(0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)

@Suppress("LongMethod") // The speed controls share one compact Compose state and interaction flow.
@Composable
fun SpeedBottomSheet(
    currentSpeed: Float,
    onSpeedChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    isWide: Boolean = false,
) {
    PlayerControlModal(isWide = isWide, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .testTag("speed_bottom_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.control_speed_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).testTag("speed_sheet_title"),
                )
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("speed_close")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.control_close),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SPEED_PRESETS.chunked(3).forEachIndexed { rowIndex, rowPresets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    ) {
                        rowPresets.forEachIndexed { columnIndex, speed ->
                            FilterChip(
                                selected = currentSpeed == speed,
                                onClick = { onSpeedChanged(speed) },
                                label = {
                                    Text(
                                        stringResource(
                                            R.string.control_speed_preset,
                                            formatPlaybackSpeed(speed),
                                        ),
                                    )
                                },
                                modifier = Modifier.testTag("speed_preset_${rowIndex * 3 + columnIndex}"),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.control_speed_fine_tune),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.control_speed_value, formatPlaybackSpeed(currentSpeed)),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("speed_value"),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onSpeedChanged(adjustPlaybackSpeed(currentSpeed, -1)) },
                    enabled = currentSpeed > PLAYBACK_SPEED_MIN,
                    modifier = Modifier.testTag("speed_decrease"),
                ) {
                    Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.control_speed_decrease))
                }
                Text(
                    text = stringResource(R.string.control_speed_step),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(
                    onClick = { onSpeedChanged(adjustPlaybackSpeed(currentSpeed, 1)) },
                    enabled = currentSpeed < PLAYBACK_SPEED_MAX,
                    modifier = Modifier.testTag("speed_increase"),
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.control_speed_increase))
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().testTag("speed_done"),
            ) {
                Text(stringResource(R.string.control_done))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

internal fun adjustPlaybackSpeed(currentSpeed: Float, direction: Int): Float =
    ((currentSpeed * SPEED_PERCENT_SCALE).roundToInt() + direction * SPEED_STEP_PERCENT)
        .coerceIn(
            (PLAYBACK_SPEED_MIN * SPEED_PERCENT_SCALE).roundToInt(),
            (PLAYBACK_SPEED_MAX * SPEED_PERCENT_SCALE).roundToInt(),
        ) / SPEED_PERCENT_SCALE.toFloat()

private const val SPEED_PERCENT_SCALE = 100
private const val SPEED_STEP_PERCENT = 5
internal fun formatPlaybackSpeed(speed: Float): String =
    if (speed == speed.toInt().toFloat()) {
        speed.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", speed).trimEnd('0').trimEnd('.', ',')
    }
