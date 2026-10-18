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
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dalapenko.laba.R
import com.dalapenko.laba.core.media.PlayerState
import com.dalapenko.laba.core.media.remainingPlaybackTimeMs
import kotlinx.coroutines.flow.Flow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.Locale
import kotlin.math.roundToInt

private val PLAYER_WIDE_LAYOUT_MIN_WIDTH = 720.dp
private val PLAYER_COVER_SIZE = 240.dp
private val WIDE_PLAYER_COVER_SIZE = 224.dp
private val WIDE_PLAYER_COVER_METADATA_SPACE = 120.dp
private val PLAYER_CARD_CORNER = 20.dp
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
    val currentPositionMs: Long,
    val durationMs: Long,
    val isSeeking: Boolean,
    val seekPosition: Float,
    val isPlaying: Boolean,
    val playbackSpeed: Float,
    val progress: Float,
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
    viewModel: PlayerViewModel = koinViewModel { parametersOf(bookId, autoPlay) },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.player_audio_error_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
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
        val isWide = maxWidth >= PLAYER_WIDE_LAYOUT_MIN_WIDTH
        val panelMaxHeight = maxHeight * PLAYER_PANEL_MAX_HEIGHT_FRACTION
        val openFiles = { if (filesInPane) onOpenFiles() else showFiles = true }
        Scaffold(
            modifier = Modifier.testTag("player_screen"),
            topBar = {
                PlayerTopBar(
                    onBack = onBack,
                    onOpenFiles = openFiles,
                    onShowChapters = { showChapters = true },
                    onShowSleepTimer = { showSleepTimer = true },
                    chaptersEnabled = uiState.tracks.size > 1,
                    sleepTimerActive = uiState.sleepTimerState.isActive,
                )
            },
        ) { padding ->
            if (uiState.isLoading || uiState.isInitializing) {
                LoadingIndicator(padding)
            } else {
                PlayerContent(
                    uiState = uiState,
                    playerState = playerState,
                    isWide = isWide,
                    padding = padding,
                    onOpenFiles = openFiles,
                    onShowSleepTimer = { showSleepTimer = true },
                    onShowSpeed = { showSpeed = true },
                    onSeekTo = onSeekTo,
                    onSkipToTrack = onSkipToTrack,
                    onSeekBack = onSeekBack,
                    onSeekForward = onSeekForward,
                    onTogglePlayPause = onTogglePlayPause,
                )
            }
        }
        if (showFiles) {
            if (maxWidth < 600.dp) {
                ModalBottomSheet(onDismissRequest = { showFiles = false }) {
                    PlayerFilesScreen(
                        bookId = bookId,
                        onBack = { showFiles = false },
                        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                    )
                }
            } else {
                Dialog(
                    onDismissRequest = { showFiles = false },
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
                                onBack = { showFiles = false },
                                maxPanelHeight = panelMaxHeight,
                            )
                        }
                    }
                }
            }
        }
        trackUnavailable?.let { error ->
            val fallbackName = stringResource(R.string.fallback_track_name)
            val fileName = error.trackName ?: String.format(fallbackName, error.trackIndex + 1)
            val canRetry = error.trackIndex in uiState.tracks.indices
            val retry = {
                onRetryTrack(error.trackIndex)
                onDismissTrackUnavailable()
            }
            val chooseAnotherFile = {
                onDismissTrackUnavailable()
                openFiles()
            }
            if (maxWidth < 600.dp && !filesInPane) {
                ModalBottomSheet(
                    onDismissRequest = onDismissTrackUnavailable,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("player_audio_error_sheet"),
                ) {
                    AudioFileErrorContent(fileName, canRetry, onDismissTrackUnavailable, retry, chooseAnotherFile)
                }
            } else {
                Dialog(
                    onDismissRequest = onDismissTrackUnavailable,
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
                                onDismiss = onDismissTrackUnavailable,
                                onRetry = retry,
                                onChooseAnotherFile = chooseAnotherFile,
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            }
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

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerTopBar(
    onBack: () -> Unit,
    onOpenFiles: () -> Unit,
    onShowChapters: () -> Unit,
    onShowSleepTimer: () -> Unit,
    chaptersEnabled: Boolean,
    sleepTimerActive: Boolean,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = {
            Text(
                stringResource(R.string.player_now_listening),
                modifier = Modifier.testTag("player_book_title"),
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.testTag("player_overflow_button")) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.player_more_options))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_close)) },
                        onClick = {
                            menuExpanded = false
                            onBack()
                        },
                        modifier = Modifier.testTag("player_close_menu_item"),
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_action_files)) },
                        leadingIcon = { Icon(Icons.Outlined.FolderOpen, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onOpenFiles()
                        },
                        modifier = Modifier.testTag("player_files_menu_item"),
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.cd_sleep_timer)) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = if (sleepTimerActive) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onShowSleepTimer()
                        },
                    )
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
    padding: PaddingValues,
    onOpenFiles: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onShowSpeed: () -> Unit,
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
        WidePlayerContent(state, actions, padding, onOpenFiles, onShowSpeed, onShowSleepTimer)
    } else {
        PhonePlayerContent(state, actions, padding, onOpenFiles, onShowSpeed, onShowSleepTimer)
    }
}

@Composable
private fun PhonePlayerContent(
    state: PlaybackDisplayState,
    actions: TransportActions,
    padding: PaddingValues,
    onOpenFiles: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowSleepTimer: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val availableWidth = maxWidth
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            CoverArt(state.coverUri, state.title, Modifier.size(minOf(PLAYER_COVER_SIZE, availableWidth)))
            Spacer(Modifier.height(12.dp))
            BookInfo(state)
            Spacer(Modifier.height(8.dp))
            TrackInfo(state)
            Spacer(Modifier.height(8.dp))
            ProgressSection(state, actions.onSeekChange, actions.onSeekFinished, Modifier.padding(horizontal = 24.dp))
            Spacer(Modifier.height(8.dp))
            TransportControls(
                state,
                actions,
                PORTRAIT_TRANSPORT_ICON_SIZE,
                PORTRAIT_TRANSPORT_PLAY_BUTTON_SIZE,
                Modifier.padding(horizontal = 12.dp),
            )
            Spacer(Modifier.height(8.dp))
            BookProgress(state, Modifier.padding(horizontal = 24.dp))
            PlayerCards(state, onShowSpeed, onOpenFiles, onShowSleepTimer)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WidePlayerContent(
    state: PlaybackDisplayState,
    actions: TransportActions,
    padding: PaddingValues,
    onOpenFiles: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowSleepTimer: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
        val compact = maxHeight < 500.dp
        val coverSize = minOf(
            WIDE_PLAYER_COVER_SIZE,
            (maxHeight - WIDE_PLAYER_COVER_METADATA_SPACE).coerceAtLeast(120.dp),
        )
        val controlsScrollModifier = if (compact) Modifier else Modifier.verticalScroll(rememberScrollState())
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = if (compact) 0.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CoverArt(state.coverUri, state.title, Modifier.size(coverSize))
                Spacer(Modifier.height(12.dp))
                BookInfo(state)
            }
            Spacer(Modifier.width(24.dp))
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().then(controlsScrollModifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                TrackInfo(state, compact)
                Spacer(Modifier.height(if (compact) 2.dp else 16.dp))
                ProgressSection(state, actions.onSeekChange, actions.onSeekFinished)
                Spacer(Modifier.height(if (compact) 4.dp else 20.dp))
                TransportControls(state, actions, LANDSCAPE_TRANSPORT_ICON_SIZE, LANDSCAPE_TRANSPORT_PLAY_BUTTON_SIZE)
                Spacer(Modifier.height(if (compact) 4.dp else 20.dp))
                BookProgress(state, stacked = !compact, compact = compact)
                Spacer(Modifier.height(if (compact) 4.dp else 16.dp))
                PlayerCards(state, onShowSpeed, onOpenFiles, onShowSleepTimer, compact = true)
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
            if (state.trackCount > 0) {
                Text(position, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                state.trackFileName.orEmpty(),
                modifier = Modifier.weight(1f),
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
            if (state.trackCount > 0) {
                Text(position, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                state.trackFileName.orEmpty(),
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
            Text(formatTime(displayedPosition), style = MaterialTheme.typography.labelSmall)
            Text(formatTime(state.durationMs), style = MaterialTheme.typography.labelSmall)
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
    stacked: Boolean = false,
    compact: Boolean = false,
) {
    val remaining = stringResource(R.string.player_time_remaining, formatTime(state.remainingMs))
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.player_listened_percent, (state.progress * 100).roundToInt()),
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            )
            if (!stacked) {
                Text(
                    remaining,
                    style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (stacked) Spacer(Modifier.height(if (compact) 4.dp else 8.dp))
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth().testTag("book_progress_slider"),
        )
        if (stacked) {
            Spacer(Modifier.height(if (compact) 4.dp else 8.dp))
            Text(
                remaining,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun PlayerCards(
    state: PlaybackDisplayState,
    onShowSpeed: () -> Unit,
    onOpenFiles: () -> Unit,
    onShowSleepTimer: () -> Unit,
    compact: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = 16.dp,
            vertical = if (compact) 0.dp else 8.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlayerActionCard(
            title = stringResource(R.string.player_action_speed),
            value = String.format(Locale.ENGLISH, "%.2f×", state.playbackSpeed),
            icon = Icons.Outlined.Speed,
            tag = "speed_control",
            compact = compact,
            modifier = Modifier.weight(1f),
            onClick = onShowSpeed,
        )
        PlayerActionCard(
            title = stringResource(R.string.player_action_files),
            value = stringResource(R.string.player_files_count, state.trackCount),
            icon = Icons.Outlined.FolderOpen,
            tag = "open_player_files_button",
            compact = compact,
            modifier = Modifier.weight(1f),
            onClick = onOpenFiles,
        )
        PlayerActionCard(
            title = stringResource(R.string.player_action_timer),
            value = stringResource(R.string.cd_sleep_timer),
            icon = Icons.Outlined.Bedtime,
            tag = "player_timer_control",
            compact = compact,
            modifier = Modifier.weight(1f),
            onClick = onShowSleepTimer,
        )
    }
}

@Composable
private fun PlayerActionCard(
    title: String,
    value: String,
    icon: ImageVector,
    tag: String,
    compact: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(if (compact) 56.dp else 84.dp).testTag(tag),
        shape = RoundedCornerShape(PLAYER_CARD_CORNER),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            Modifier.fillMaxSize().padding(
                horizontal = if (compact) 4.dp else 8.dp,
                vertical = if (compact) 2.dp else 10.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(if (compact) 20.dp else 24.dp),
            )
            Text(
                title,
                style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
            Text(
                value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
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
