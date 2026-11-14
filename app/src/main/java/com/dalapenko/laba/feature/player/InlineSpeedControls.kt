package com.dalapenko.laba.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.dalapenko.laba.R

private val PLAYER_SPEED_PRESETS = listOf(0.5f, 1f, 1.25f, 1.5f, 2f)

@Composable
internal fun InlineSpeedControls(
    speed: Float,
    onShowSpeed: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    compact: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = if (compact) 8.dp else 24.dp)
            .testTag("speed_control"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CurrentSpeedControl(speed, onShowSpeed)
        Spacer(Modifier.height(16.dp))
        SpeedAdjustmentControl(speed, onSetSpeed)
        Spacer(Modifier.height(8.dp))
        SpeedPresets(speed, onSetSpeed)
    }
}

@Composable
private fun CurrentSpeedControl(speed: Float, onShowSpeed: () -> Unit) {
    val speedDescription = stringResource(R.string.player_speed_value_accessibility, speed)
    Box(Modifier.fillMaxWidth().height(32.dp)) {
        Text(
            stringResource(R.string.player_action_speed),
            modifier = Modifier.align(Alignment.CenterStart),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            stringResource(R.string.control_speed_value, formatPlaybackSpeed(speed)),
            modifier = Modifier.align(Alignment.Center).clickable(onClick = onShowSpeed)
                .testTag("speed_value").semantics { contentDescription = speedDescription },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun SpeedAdjustmentControl(speed: Float, onSetSpeed: (Float) -> Unit) {
    val sliderDescription = stringResource(R.string.control_speed_fine_tune)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpeedStepButton(
            label = "−",
            tag = "speed_decrease",
            description = stringResource(R.string.control_speed_decrease),
            onClick = { onSetSpeed(adjustPlaybackSpeed(speed, -1)) },
        )
        Slider(
            value = speed.coerceIn(PLAYBACK_SPEED_MIN, PLAYBACK_SPEED_MAX),
            onValueChange = onSetSpeed,
            valueRange = PLAYBACK_SPEED_MIN..PLAYBACK_SPEED_MAX,
            modifier = Modifier.weight(1f).testTag("speed_slider")
                .semantics { contentDescription = sliderDescription },
        )
        SpeedStepButton(
            label = "+",
            tag = "speed_increase",
            description = stringResource(R.string.control_speed_increase),
            onClick = { onSetSpeed(adjustPlaybackSpeed(speed, 1)) },
        )
    }
}

@Composable
private fun SpeedPresets(speed: Float, onSetSpeed: (Float) -> Unit) {
    val selectedDescription = stringResource(R.string.player_speed_selected)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PLAYER_SPEED_PRESETS.forEachIndexed { index, preset ->
            val selected = speed == preset
            val presetDescription =
                stringResource(R.string.control_speed_preset, formatPlaybackSpeed(preset))
            Box(
                modifier = Modifier.size(56.dp, 48.dp)
                    .clickable { onSetSpeed(preset) }
                    .testTag("speed_preset_$index")
                    .semantics {
                        contentDescription = presetDescription
                        if (selected) stateDescription = selectedDescription
                    },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(52.dp, 40.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            formatPlaybackSpeed(preset) + "×",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedStepButton(
    label: String,
    tag: String,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(56.dp, 48.dp).clickable(onClick = onClick).testTag(tag)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.size(52.dp, 40.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
