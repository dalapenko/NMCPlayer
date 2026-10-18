package com.dalapenko.laba.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dalapenko.laba.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Suppress("LongMethod")
@Composable
fun PlayerFilesScreen(
    bookId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    maxPanelHeight: Dp? = null,
    viewModel: PlayerFilesViewModel = koinViewModel { parametersOf(bookId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val panelHeight = maxPanelHeight?.let { maxHeight ->
        val contentHeight = if (state.isLoading || state.tracks.isEmpty()) {
            180.dp
        } else {
            116.dp + 72.dp * state.tracks.size.toFloat()
        }
        minOf(maxHeight, contentHeight)
    }
    Card(
        modifier = (if (panelHeight == null) modifier.fillMaxSize() else modifier.fillMaxWidth().height(panelHeight))
            .testTag("player_files_screen"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.fillMaxSize()) {
            if (maxPanelHeight != null) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp, bottom = 8.dp)
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(
                    start = 24.dp,
                    top = if (maxPanelHeight != null) 8.dp else 20.dp,
                    end = 12.dp,
                    bottom = 12.dp,
                ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f).padding(top = 4.dp)) {
                    Text(
                        text = stringResource(R.string.player_files_count, state.tracks.size),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = state.title.ifBlank { stringResource(R.string.player_files_title) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                IconButton(onClick = onBack, modifier = Modifier.testTag("close_player_files_button")) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
                }
            }
            when {
                state.isLoading -> Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                state.tracks.isEmpty() -> Text(
                    text = stringResource(R.string.player_files_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
                else -> {
                    val currentTrack = state.currentTrackIndex.takeIf { state.isActiveBook }
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().testTag("player_files_list"),
                    ) {
                        itemsIndexed(
                            state.tracks,
                            key = { _, track -> track.id },
                            contentType = { _, _ -> "track" },
                        ) { index, track ->
                            val isCurrent = index == currentTrack
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = if (isCurrent) {
                                            stringResource(R.string.player_file_current_name, track.fileName)
                                        } else {
                                            track.fileName
                                        },
                                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                supportingContent = { Text(formatDuration(track.durationMs)) },
                                leadingContent = if (isCurrent) {
                                    {
                                        Icon(
                                            imageVector = if (state.isPlaying) {
                                                Icons.Default.Pause
                                            } else {
                                                Icons.Default.PlayArrow
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                } else {
                                    null
                                },
                                colors = if (isCurrent) {
                                    ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    ListItemDefaults.colors()
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .clickable(enabled = state.isActiveBook) { viewModel.skipToTrack(index) }
                                    .testTag("player_file_$index"),
                            )
                        }
                    }
                }
            }
        }
    }
}
