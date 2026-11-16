package com.dalapenko.laba.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dalapenko.laba.R
import com.dalapenko.laba.core.media.PlayerState
import com.dalapenko.laba.core.media.remainingPlaybackTimeMs
import com.dalapenko.laba.ui.components.AppModalBottomSheet
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale
import kotlin.math.roundToInt

private val PLAYER_COVER_SIZE = 180.dp
private val WIDE_PLAYER_COVER_SIZE = 237.dp
private val LANDSCAPE_TRANSPORT_ICON_SIZE = 32.dp
private val LANDSCAPE_TRANSPORT_PLAY_BUTTON_SIZE = 56.dp
private val PORTRAIT_TRANSPORT_ICON_SIZE = 36.dp
private val PORTRAIT_TRANSPORT_PLAY_BUTTON_SIZE = 64.dp
private const val PLAYER_PANEL_MAX_HEIGHT_FRACTION = 0.82f

private data class PlaybackDisplayState(
    val title: String,
    val author: String?,
    val coverUri: String?,
    val trackFileName: String?,
    val trackNumber: Int,
    val trackCount: Int,
    val position: Float,
    val duration: Float,
    val progress: Float,
    val currentPositionMs: Long,
    val durationMs: Long,
    val isSeeking: Boolean,
    val seekPosition: Float,
    val isPlaying: Boolean,
    val playbackSpeed: Float,
    val remainingMs: Long,
    val isPreviousEnabled: Boolean,
    val isNextEnabled: Boolean,
)

private data class TransportActions(
    val onPrevious: () -> Unit,
    val onRewind: () -> Unit,
    val onPlayPause: () -> Unit,
    val onForward: () -> Unit,
    val onNext: () -> Unit,
    val onSeekChange: (Float) -> Unit,
    val onSeekFinished: () -> Unit,
)

@Composable
fun PlayerScreen(
    bookId: Long,
    autoPlay: Boolean = true,
    onBack: () -> Unit,
    onOpenFiles: () -> Unit,
    filesInPane: Boolean = false,
    onPlayerStateLoaded: (trackCount: Int) -> Unit = {},
    viewModel: PlayerViewModel = koinViewModel { parametersOf(bookId, autoPlay) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var reportedLoaded by rememberSaveable(bookId) { mutableStateOf(false) }
    LaunchedEffect(uiState.isLoading, uiState.isInitializing, uiState.tracks.size) {
        if (!uiState.isLoading && !uiState.isInitializing && !reportedLoaded) {
            reportedLoaded = true
            onPlayerStateLoaded(uiState.tracks.size)
        }
    }
    var unavailableTrack by remember { mutableStateOf<PlayerEvent.TrackUnavailable?>(null) }
    PlayerEventEffect(
        events = viewModel.events,
        onBack = onBack,
        onTrackUnavailable = { unavailableTrack = it },
    )
    PlayerScaffold(
        bookId = bookId,
        filesInPane = filesInPane,
        uiState = uiState,
        onBack = onBack,
        onOpenFiles = onOpenFiles,
        trackUnavailable = unavailableTrack,
        onDismissTrackUnavailable = { unavailableTrack = null },
        onRetryTrack = viewModel::retryTrack,
        onSeekTo = viewModel::seekTo,
        onSkipToTrack = viewModel::skipToTrack,
        onSeekBack = viewModel::seekBack,
        onSeekForward = viewModel::seekForward,
        onTogglePlayPause = viewModel::togglePlayPause,
        onSetSpeed = viewModel::setSpeed,
        onStartFixedDuration = viewModel::startSleepTimerFixed,
        onStartEndOfChapter = viewModel::startSleepTimerEndOfChapter,
        onCancelSleepTimer = viewModel::cancelSleepTimer,
    )
}

@Composable
private fun PlayerEventEffect(
    events: Flow<PlayerEvent>,
    onBack: () -> Unit,
    onTrackUnavailable: (PlayerEvent.TrackUnavailable) -> Unit,
) {
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                PlayerEvent.ClosePlayer -> onBack()
                is PlayerEvent.TrackUnavailable -> onTrackUnavailable(event)
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun AudioFileErrorContent(
    fileName: String,
    canRetry: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChooseAnotherFile: () -> Unit,
    compactHeader: Boolean = false,
    topPadding: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp)
            .padding(top = topPadding, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (compactHeader) Alignment.Top else Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.player_audio_error_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).testTag("player_audio_error_title"),
            )
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("player_audio_error_close_button")) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.player_audio_error_close))
            }
        }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(
                    56.dp,
                ).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            text = fileName,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().testTag("player_audio_error_filename"),
        )
        Text(
            text = stringResource(R.string.player_audio_error_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(
            onClick = onChooseAnotherFile,
            modifier = Modifier.fillMaxWidth().testTag("player_audio_error_choose_file_button"),
        ) {
            Text(stringResource(R.string.player_audio_error_choose_file))
        }
        Button(
            onClick = onRetry,
            enabled = canRetry,
            modifier = Modifier.fillMaxWidth().testTag("player_audio_error_retry_button"),
        ) {
            Text(stringResource(R.string.player_audio_error_retry))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod") // The player scaffold coordinates responsive overlays and playback controls.
@Composable
private fun PlayerScaffold(
    bookId: Long,
    filesInPane: Boolean,
    uiState: PlayerUiState,
    onBack: () -> Unit,
    onOpenFiles: () -> Unit,
    trackUnavailable: PlayerEvent.TrackUnavailable?,
    onDismissTrackUnavailable: () -> Unit,
    onRetryTrack: (Int) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipToTrack: (Int) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onStartFixedDuration: (Int) -> Unit,
    onStartEndOfChapter: () -> Unit,
    onCancelSleepTimer: () -> Unit,
) {
    val playerState = uiState.playerState
    var showChapters by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showFiles by remember { mutableStateOf(false) }
    var showSpeed by remember { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = playerLayoutBreakpoints(maxWidth, maxHeight).first
        val panelMaxHeight = maxHeight * PLAYER_PANEL_MAX_HEIGHT_FRACTION
        val isTabletLayout = maxWidth >= 1200.dp || filesInPane
        val openFiles = { if (isTabletLayout) onOpenFiles() else showFiles = true }
        val showFilesAction = !isTabletLayout || (uiState.tracks.size > 1 && !filesInPane)
        Scaffold(
            modifier = Modifier.testTag("player_screen"),
            topBar = {
                Column {
                    PlayerTopBar(
                        onBack = onBack,
                        onOpenFiles = openFiles,
                        showFilesAction = showFilesAction,
                        onShowChapters = { showChapters = true },
                        onShowSleepTimer = { showSleepTimer = true },
                        chaptersEnabled = uiState.tracks.size > 1,
                    )
                    if (uiState.sleepTimerState.isActive) {
                        Text(
                            stringResource(
                                R.string.player_active_timer_status,
                                formatTime(uiState.sleepTimerState.remainingMs),
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("player_active_timer_status")
                                .padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
        ) { padding ->
            if (uiState.isLoading || uiState.isInitializing) {
                LoadingIndicator(padding)
            } else {
                PlayerContent(
                    uiState = uiState,
                    playerState = playerState,
                    isWide = isWide,
                    isTabletLayout = isTabletLayout,
                    padding = padding,
                    onShowSpeed = { showSpeed = true },
                    onSetSpeed = onSetSpeed,
                    onSeekTo = onSeekTo,
                    onSkipToTrack = onSkipToTrack,
                    onSeekBack = onSeekBack,
                    onSeekForward = onSeekForward,
                    onTogglePlayPause = onTogglePlayPause,
                )
            }
        }
        PlayerFilesOverlay(
            visible = showFiles,
            bookId = bookId,
            narrow = maxWidth < 600.dp,
            panelMaxHeight = panelMaxHeight,
            onDismiss = { showFiles = false },
        )
        trackUnavailable?.let { error ->
            TrackUnavailableOverlay(
                error = error,
                canRetry = error.trackIndex in uiState.tracks.indices,
                narrow = maxWidth < 600.dp && !filesInPane,
                panelMaxHeight = panelMaxHeight,
                onDismiss = onDismissTrackUnavailable,
                onRetry = {
                    onRetryTrack(error.trackIndex)
                    onDismissTrackUnavailable()
                },
                onChooseAnotherFile = {
                    onDismissTrackUnavailable()
                    openFiles()
                },
            )
        }
        if (showChapters) {
            ChapterBottomSheet(
                tracks = uiState.tracks,
                currentTrackIndex = playerState.currentMediaItemIndex,
                onTrackSelected = { index ->
                    onSkipToTrack(index)
                    showChapters = false
                },
                onDismiss = { showChapters = false },
            )
        }
        if (showSleepTimer) {
            SleepTimerBottomSheet(
                isWide = maxWidth >= 600.dp,
                isActive = uiState.sleepTimerState.isActive,
                remainingMs = uiState.sleepTimerState.remainingMs,
                remainingChapterMs = remainingPlaybackTimeMs(
                    playerState.durationMs,
                    playerState.currentPositionMs,
                    playerState.playbackSpeed,
                ),
                initialFixedMinutes = uiState.rememberedSleepTimerMinutes,
                onStartFixedDuration = {
                    onStartFixedDuration(it)
                    showSleepTimer = false
                },
                onStartEndOfChapter = {
                    onStartEndOfChapter()
                    showSleepTimer = false
                },
                onCancel = {
                    onCancelSleepTimer()
                    showSleepTimer = false
                },
                onDismiss = { showSleepTimer = false },
            )
        }
        if (showSpeed) {
            SpeedBottomSheet(
                currentSpeed = playerState.playbackSpeed,
                isWide = maxWidth >= 600.dp,
                onSpeedChanged = onSetSpeed,
                onDismiss = { showSpeed = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerFilesOverlay(
    visible: Boolean,
    bookId: Long,
    narrow: Boolean,
    panelMaxHeight: Dp,
    onDismiss: () -> Unit,
) {
    if (visible) {
        if (narrow) {
            AppModalBottomSheet(
                onDismissRequest = onDismiss,
                modifier = Modifier.testTag("player_files_sheet_surface"),
            ) {
                PlayerFilesScreen(
                    bookId = bookId,
                    onBack = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                    modalSheetHandle = true,
                )
            }
        } else {
            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    Surface(
                        modifier = Modifier.width(420.dp).heightIn(max = panelMaxHeight),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                    ) {
                        PlayerFilesScreen(
                            bookId = bookId,
                            onBack = onDismiss,
                            maxPanelHeight = panelMaxHeight,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackUnavailableOverlay(
    error: PlayerEvent.TrackUnavailable,
    canRetry: Boolean,
    narrow: Boolean,
    panelMaxHeight: Dp,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChooseAnotherFile: () -> Unit,
) {
    val fallbackName = stringResource(R.string.fallback_track_name)
    val fileName = error.trackName ?: String.format(fallbackName, error.trackIndex + 1)
    if (narrow) {
        AppModalBottomSheet(
            onDismissRequest = onDismiss,
            modifier = Modifier.testTag("player_audio_error_sheet"),
        ) {
            AudioFileErrorContent(
                fileName,
                canRetry,
                onDismiss,
                onRetry,
                onChooseAnotherFile,
                compactHeader = true,
                topPadding = 0.dp,
            )
        }
    } else {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                Surface(
                    modifier = Modifier.width(420.dp).heightIn(max = panelMaxHeight)
                        .testTag("player_audio_error_sheet"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                ) {
                    AudioFileErrorContent(
                        fileName = fileName,
                        canRetry = canRetry,
                        onDismiss = onDismiss,
                        onRetry = onRetry,
                        onChooseAnotherFile = onChooseAnotherFile,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    )
                }
            }
        }
    }
}

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerTopBar(
    onBack: () -> Unit,
    onOpenFiles: () -> Unit,
    showFilesAction: Boolean,
    onShowChapters: () -> Unit,
    onShowSleepTimer: () -> Unit,
    chaptersEnabled: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.player_now_listening),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("player_book_title"),
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
        actions = {
            if (showFilesAction) {
                IconButton(onClick = onOpenFiles, modifier = Modifier.testTag("open_player_files_button")) {
                    Icon(
                        Icons.AutoMirrored.Filled.ListAlt,
                        contentDescription = stringResource(R.string.player_action_files),
                    )
                }
            }
            IconButton(onClick = onShowSleepTimer, modifier = Modifier.testTag("player_timer_control")) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = stringResource(R.string.player_action_timer),
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("player_overflow_button")) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.player_more_options))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.cd_show_chapters)) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null) },
                        enabled = chaptersEnabled,
                        onClick = {
                            menuExpanded = false
                            onShowChapters()
                        },
                        modifier = Modifier.testTag("chapters_button"),
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_close)) },
                        onClick = {
                            menuExpanded = false
                            onBack()
                        },
                        modifier = Modifier.testTag("player_close_menu_item"),
                    )
                }
            }
        },
    )
}

@Suppress("LongMethod")
@Composable
private fun PlayerContent(
    uiState: PlayerUiState,
    playerState: PlayerState,
    isWide: Boolean,
    isTabletLayout: Boolean,
    padding: PaddingValues,
    onShowSpeed: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipToTrack: (Int) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onTogglePlayPause: () -> Unit,
) {
    val tracks = uiState.tracks
    val trackIndex = playerState.currentMediaItemIndex.coerceIn(0, tracks.lastIndex.coerceAtLeast(0))
    val track = tracks.getOrNull(trackIndex)
    val trackStarts = remember(tracks) {
        LongArray(tracks.size + 1).also { starts ->
            tracks.forEachIndexed { index, item -> starts[index + 1] = starts[index] + item.durationMs }
        }
    }
    val totalDurationMs = (uiState.book?.totalDurationMs ?: 0L)
        .takeIf { it > 0L }
        ?: trackStarts.last()
    val listenedMs = (trackStarts[trackIndex] + playerState.currentPositionMs)
        .coerceIn(0L, totalDurationMs.coerceAtLeast(playerState.currentPositionMs))
    val progress = if (totalDurationMs > 0) (listenedMs.toFloat() / totalDurationMs).coerceIn(0f, 1f) else 0f
    val remainingMs = remainingPlaybackTimeMs(totalDurationMs, listenedMs, playerState.playbackSpeed)
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }
    val durationMs = playerState.durationMs.takeIf { it > 0L } ?: track?.durationMs ?: 0L
    val state = PlaybackDisplayState(
        title = uiState.book?.title.orEmpty(),
        author = uiState.book?.author,
        coverUri = uiState.book?.coverUri,
        trackFileName = track?.fileName,
        trackNumber = trackIndex + 1,
        trackCount = tracks.size,
        position = playerState.currentPositionMs.toFloat(),
        duration = durationMs.toFloat().coerceAtLeast(1f),
        currentPositionMs = playerState.currentPositionMs,
        durationMs = durationMs,
        isSeeking = isSeeking,
        seekPosition = seekPosition,
        isPlaying = playerState.isPlaying,
        playbackSpeed = playerState.playbackSpeed,
        progress = progress,
        remainingMs = remainingMs,
        isPreviousEnabled = tracks.size > 1 && trackIndex > 0,
        isNextEnabled = tracks.size > 1 && trackIndex < tracks.lastIndex,
    )
    val actions = TransportActions(
        onPrevious = { onSkipToTrack(trackIndex - 1) },
        onRewind = onSeekBack,
        onPlayPause = onTogglePlayPause,
        onForward = onSeekForward,
        onNext = { onSkipToTrack(trackIndex + 1) },
        onSeekChange = {
            isSeeking = true
            seekPosition = it
        },
        onSeekFinished = {
            onSeekTo(seekPosition.toLong())
            isSeeking = false
        },
    )
    if (isWide) {
        WidePlayerContent(state, actions, padding, onShowSpeed, onSetSpeed, isTabletLayout)
    } else {
        PhonePlayerContent(state, actions, padding, onShowSpeed, onSetSpeed)
    }
}

@Composable
private fun PhonePlayerContent(
    state: PlaybackDisplayState,
    actions: TransportActions,
    padding: PaddingValues,
    onShowSpeed: () -> Unit,
    onSetSpeed: (Float) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        Box(Modifier.matchParentSize().testTag("player_content_bounds"))
        PhonePlayerLayout(availableHeight = maxHeight) {
            Spacer(Modifier.height(0.dp).testTag("player_outer_top_space"))
            CoverArt(
                state.coverUri,
                state.title,
                Modifier.testTag("player_cover_art"),
            )
            Spacer(Modifier.height(12.dp))
            BookInfo(state)
            Spacer(Modifier.height(8.dp))
            TrackInfo(state)
            if (state.trackCount > 1) {
                Spacer(Modifier.height(8.dp))
                BookProgress(state, Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(12.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }
            ProgressSection(state, actions.onSeekChange, actions.onSeekFinished, Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(12.dp))
            TransportControls(
                state,
                actions,
                PORTRAIT_TRANSPORT_ICON_SIZE,
                PORTRAIT_TRANSPORT_PLAY_BUTTON_SIZE,
                Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(20.dp))
            InlineSpeedControls(state.playbackSpeed, onShowSpeed, onSetSpeed)
            Spacer(Modifier.height(0.dp).testTag("player_outer_bottom_space"))
        }
    }
}

@Composable
private fun PhonePlayerLayout(
    availableHeight: Dp,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        content = content,
    ) { measurables, constraints ->
        val coverIndex = 1
        val topArtworkSpacingIndex = 0
        val bottomArtworkSpacingIndex = measurables.lastIndex
        val childConstraints = constraints.copy(minWidth = 0, maxHeight = Constraints.Infinity)
        val placeables = arrayOfNulls<Placeable>(measurables.size)
        var reservedHeight = 0
        measurables.forEachIndexed { index, measurable ->
            if (index != coverIndex && index != topArtworkSpacingIndex && index != bottomArtworkSpacingIndex) {
                val placeable = measurable.measure(childConstraints)
                placeables[index] = placeable
                reservedHeight += placeable.height
            }
        }
        val availableHeightPx = availableHeight.roundToPx()
        val budget = playerArtworkBudget(
            availableHeight = availableHeightPx,
            reservedHeight = reservedHeight,
            availableWidth = constraints.maxWidth,
            maxCoverSize = PLAYER_COVER_SIZE.roundToPx(),
        )
        placeables[topArtworkSpacingIndex] =
            measurables[topArtworkSpacingIndex].measure(Constraints.fixed(0, budget.topSpacing))
        placeables[coverIndex] =
            measurables[coverIndex].measure(Constraints.fixed(budget.coverSize, budget.coverSize))
        placeables[bottomArtworkSpacingIndex] =
            measurables[bottomArtworkSpacingIndex].measure(Constraints.fixed(0, budget.bottomSpacing))
        val layoutHeight = maxOf(budget.contentHeight, availableHeightPx)
        layout(constraints.maxWidth, layoutHeight) {
            var y = 0
            placeables.forEachIndexed { index, placeable ->
                placeable!!.placeRelative(
                    x = if (index == coverIndex) centeredPlayerChildX(constraints.maxWidth, placeable.width) else 0,
                    y = y,
                )
                y += placeable.height
            }
        }
    }
}

@Composable
@Suppress("LongMethod") // The two adaptive player columns share the same measured window bounds.
private fun WidePlayerContent(
    state: PlaybackDisplayState,
    actions: TransportActions,
    padding: PaddingValues,
    onShowSpeed: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    tabletLayout: Boolean,
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val compact = playerLayoutBreakpoints(maxWidth, maxHeight).second
        val availableHeight = maxHeight
        val outerSpacing = if (compact) 0.dp else 16.dp
        val contentAvailableHeight = (availableHeight - outerSpacing).coerceAtLeast(0.dp)
        val tabletMainPane = tabletLayout
        Box(Modifier.matchParentSize().testTag("player_content_bounds"))
        Row(
            modifier = Modifier.fillMaxHeight()
                .widthIn(max = if (tabletMainPane) 848.dp else maxWidth)
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(
                    start = if (tabletMainPane) 0.dp else 24.dp,
                    top = if (compact) 0.dp else 8.dp,
                    end = if (tabletMainPane) 60.dp else 24.dp,
                    bottom = if (compact) 0.dp else 8.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .then(if (tabletMainPane) Modifier.width(424.dp) else Modifier.weight(1f))
                    .fillMaxHeight()
                    .padding(start = if (tabletMainPane) 68.dp else 0.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("player_left_column"),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Layout(
                    modifier = Modifier.fillMaxWidth().heightIn(min = contentAvailableHeight),
                    content = {
                        CoverArt(
                            state.coverUri,
                            state.title,
                            Modifier.testTag("player_cover_art"),
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(12.dp))
                            BookInfo(state)
                            if (state.trackCount > 1) {
                                Spacer(Modifier.height(if (compact) 4.dp else 12.dp))
                                BookProgress(
                                    state,
                                    Modifier.padding(horizontal = 24.dp),
                                    compact = compact,
                                )
                            }
                            Spacer(Modifier.height(if (compact) 2.dp else 8.dp))
                            TrackInfo(state, compact = compact)
                            Spacer(Modifier.height(if (compact) 2.dp else 8.dp))
                            ProgressSection(
                                state,
                                actions.onSeekChange,
                                actions.onSeekFinished,
                                Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    },
                ) { measurables, constraints ->
                    val otherContent = measurables[1].measure(
                        constraints.copy(minWidth = 0, minHeight = 0, maxHeight = Constraints.Infinity),
                    )
                    val coverSize = widePlayerArtworkSize(
                        availableHeight = availableHeight.roundToPx(),
                        reservedHeight = otherContent.height,
                        availableWidth = constraints.maxWidth,
                        maxCoverSize = WIDE_PLAYER_COVER_SIZE.roundToPx(),
                        outerSpacing = outerSpacing.roundToPx(),
                    )
                    val cover = measurables[0].measure(
                        Constraints.fixed(coverSize.coerceAtLeast(0), coverSize.coerceAtLeast(0)),
                    )
                    val contentHeight = cover.height + otherContent.height
                    val innerHeight = contentAvailableHeight.roundToPx()
                    val layoutHeight = maxOf(innerHeight, contentHeight, constraints.minHeight)
                    val top = ((innerHeight - contentHeight) / 2).coerceAtLeast(0)
                    layout(constraints.maxWidth, layoutHeight) {
                        cover.placeRelative(centeredPlayerChildX(constraints.maxWidth, cover.width), top)
                        otherContent.placeRelative(0, top + cover.height)
                    }
                }
            }
            Spacer(Modifier.width(if (tabletMainPane) 0.dp else 24.dp))
            WidePlayerControls(
                state = state,
                actions = actions,
                compact = compact,
                availableHeight = contentAvailableHeight,
                onShowSpeed = onShowSpeed,
                onSetSpeed = onSetSpeed,
                modifier = Modifier.weight(1f).testTag("player_right_column"),
            )
        }
    }
}

@Composable
private fun WidePlayerControls(
    state: PlaybackDisplayState,
    actions: TransportActions,
    compact: Boolean,
    availableHeight: Dp,
    onShowSpeed: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.fillMaxWidth().heightIn(min = availableHeight),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TransportControls(state, actions, LANDSCAPE_TRANSPORT_ICON_SIZE, LANDSCAPE_TRANSPORT_PLAY_BUTTON_SIZE)
                Spacer(Modifier.height(if (compact) 16.dp else 32.dp))
                InlineSpeedControls(state.playbackSpeed, onShowSpeed, onSetSpeed, compact = compact)
            }
        }
    }
}

@Composable
private fun BookInfo(state: PlaybackDisplayState) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("player_book_name"),
        )
        state.author?.takeIf(String::isNotBlank)?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TrackInfo(state: PlaybackDisplayState, compact: Boolean = false) {
    val position = stringResource(R.string.player_file_position, state.trackNumber, state.trackCount)
    if (compact) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.trackCount > 1) {
                Text(
                    position,
                    modifier = Modifier.testTag("player_file_position"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                state.trackFileName.orEmpty(),
                modifier = Modifier.weight(1f).testTag("player_track_file_name"),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.trackCount > 1) {
                Text(
                    position,
                    modifier = Modifier.testTag("player_file_position"),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                state.trackFileName.orEmpty(),
                modifier = Modifier.testTag("player_track_file_name"),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProgressSection(
    state: PlaybackDisplayState,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.cd_playback_position)
    val displayedPosition = if (state.isSeeking) state.seekPosition.toLong() else state.currentPositionMs
    Column(modifier.fillMaxWidth()) {
        Slider(
            value = if (state.isSeeking) state.seekPosition else state.position.coerceIn(0f, state.duration),
            onValueChange = onSeekChange,
            onValueChangeFinished = onSeekFinished,
            valueRange = 0f..state.duration,
            modifier = Modifier.fillMaxWidth().testTag(
                "position_slider",
            ).semantics { contentDescription = description },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                formatTime(displayedPosition),
                modifier = Modifier.testTag("position_elapsed"),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                formatTime(
                    remainingPlaybackTimeMs(
                        state.durationMs,
                        displayedPosition,
                        state.playbackSpeed,
                    ),
                ),
                modifier = Modifier.testTag("position_remaining"),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun TransportControls(
    state: PlaybackDisplayState,
    actions: TransportActions,
    iconSize: Dp,
    playButtonSize: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (state.trackCount > 1) {
            TransportIconButton(
                Icons.Default.SkipPrevious,
                stringResource(R.string.cd_previous_chapter),
                iconSize,
                "previous_chapter_button",
                actions.onPrevious,
                state.isPreviousEnabled,
            )
            Spacer(Modifier.width(4.dp))
        }
        TransportIconButton(
            Icons.Default.Replay10,
            stringResource(R.string.cd_rewind_10),
            iconSize,
            "rewind_button",
            actions.onRewind,
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = actions.onPlayPause,
            modifier = Modifier.size(playButtonSize).testTag("play_pause_button"),
        ) {
            Icon(
                if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(if (state.isPlaying) R.string.cd_pause else R.string.cd_play),
                modifier = Modifier.size(iconSize),
            )
        }
        Spacer(Modifier.width(8.dp))
        TransportIconButton(
            Icons.Default.Forward10,
            stringResource(R.string.cd_forward_10),
            iconSize,
            "forward_button",
            actions.onForward,
        )
        if (state.trackCount > 1) {
            Spacer(Modifier.width(4.dp))
            TransportIconButton(
                Icons.Default.SkipNext,
                stringResource(R.string.cd_next_chapter),
                iconSize,
                "next_chapter_button",
                actions.onNext,
                state.isNextEnabled,
            )
        }
    }
}

@Composable
private fun TransportIconButton(
    icon: ImageVector,
    contentDescription: String,
    iconSize: Dp,
    testTag: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.testTag(testTag)) {
        Icon(icon, contentDescription, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun BookProgress(
    state: PlaybackDisplayState,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val textStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
    Text(
        stringResource(
            R.string.player_total_progress_info,
            (state.progress * 100).roundToInt(),
            formatTime(state.remainingMs),
        ),
        style = textStyle,
        modifier = modifier.fillMaxWidth().testTag("total_progress_info"),
    )
}

@Composable
private fun CoverArt(coverUri: String?, title: String, modifier: Modifier = Modifier) {
    if (coverUri != null) {
        AsyncImage(
            model = coverUri,
            contentDescription = stringResource(R.string.cd_cover_art_for, title),
            modifier = modifier.clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier.clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                title.take(2).uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private const val MILLIS_PER_SECOND = 1_000
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3_600

private fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return if (hours > 0) {
        String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds)
    }
}

@Composable
private fun LoadingIndicator(padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
