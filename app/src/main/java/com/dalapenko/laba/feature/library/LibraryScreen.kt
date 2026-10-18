@file:Suppress("TooManyFunctions") // Screen-specific Compose helpers stay together to keep the UI flow readable.

package com.dalapenko.laba.feature.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dalapenko.laba.R
import com.dalapenko.laba.core.data.BookWithProgress
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private data class LibraryBookCardUiState(
    val bookWithProgress: BookWithProgress,
    val isPlaying: Boolean,
    val isHighlighted: Boolean,
)

private const val UNAVAILABLE_BOOK_ALPHA = 0.4f
private const val WIDE_LIBRARY_DETAILS_WEIGHT = 0.38f
private const val WIDE_LIBRARY_BOOKS_WEIGHT = 0.62f
private const val EMPTY_COVER_ROTATION_DEGREES = 7f

private const val MODAL_MAX_HEIGHT_FRACTION = 0.82f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Suppress("LongMethod", "CyclomaticComplexMethod")
@Composable
fun LibraryScreen(
    onBookClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val continueBook by viewModel.continueBook.collectAsStateWithLifecycle()
    val books by viewModel.libraryBooks.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanningSourceName by viewModel.scanningSourceName.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val playbackStatus by viewModel.playbackStatus.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val msgFilesDeleted = stringResource(R.string.snackbar_book_and_files_deleted)
    val msgFilesDeleteFailed = stringResource(R.string.snackbar_removed_from_library_no_delete)
    val bookToDelete = remember { mutableStateOf<BookWithProgress?>(null) }
    var unavailableBookId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.FileNotAvailable -> unavailableBookId = event.bookId
                LibraryEvent.FilesDeletedSuccessfully ->
                    snackbarHostState.showSnackbar(msgFilesDeleted)
                LibraryEvent.FilesDeleteFailed ->
                    snackbarHostState.showSnackbar(msgFilesDeleteFailed)
            }
        }
    }

    val unavailableBook = (
        books.firstOrNull { it.book.id == unavailableBookId }
            ?: continueBook?.book?.takeIf { it.book.id == unavailableBookId }
        )?.takeIf { !it.book.isAvailable }
    LibraryScreenContent(
        continueBook = continueBook,
        books = books,
        isScanning = isScanning,
        scanningSourceName = scanningSourceName,
        scanResult = scanResult,
        unavailableBook = unavailableBook,
        isRefreshing = isRefreshing,
        playbackStatus = playbackStatus,
        sortOption = sortOption,
        snackbarHostState = snackbarHostState,
        onBookClick = onBookClick,
        onSettingsClick = onSettingsClick,
        onSortOptionSelected = viewModel::setLibrarySortOption,
        onTogglePlayPause = viewModel::togglePlayPause,
        onPrepareAndPlay = viewModel::prepareAndPlay,
        onUnavailableBookClicked = viewModel::onUnavailableBookClicked,
        onUnavailableDismiss = { unavailableBookId = null },
        onRecheckUnavailable = viewModel::recheckBookAvailability,
        onResyncAll = viewModel::resyncAll,
        onFolderPicked = viewModel::onFolderPicked,
        onFilePicked = viewModel::onFilePicked,
        onScanResultDismiss = viewModel::clearScanResult,
        onDeleteRequested = { bookToDelete.value = it },
    )

    DeleteBookDialog(
        book = bookToDelete.value,
        onDismiss = { bookToDelete.value = null },
        onConfirm = { book, deleteFiles ->
            viewModel.deleteBook(book, deleteFiles)
            bookToDelete.value = null
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod") // Compose layout and event wiring are clearer together than split into wrapper helpers.
@Composable
private fun LibraryScreenContent(
    continueBook: ContinueBookUiState?,
    books: List<BookWithProgress>,
    isScanning: Boolean,
    scanningSourceName: String?,
    scanResult: ScanResult?,
    unavailableBook: BookWithProgress?,
    isRefreshing: Boolean,
    playbackStatus: PlaybackStatus,
    sortOption: LibrarySortOption,
    snackbarHostState: SnackbarHostState,
    onBookClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onSortOptionSelected: (LibrarySortOption) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrepareAndPlay: (Long) -> Unit,
    onUnavailableBookClicked: (Long) -> Unit,
    onUnavailableDismiss: () -> Unit,
    onRecheckUnavailable: (Long) -> Unit,
    onResyncAll: () -> Unit,
    onFolderPicked: (Uri, android.content.ContentResolver, String?) -> Unit,
    onFilePicked: (Uri, android.content.ContentResolver, String?) -> Unit,
    onScanResultDismiss: () -> Unit,
    onDeleteRequested: (BookWithProgress) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .testTag("library_screen"),
        topBar = {
            LibraryTopBar(
                scrollBehavior = scrollBehavior,
                onSettingsClick = onSettingsClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            LibraryFabMenuWithPickers(
                expanded = showFabMenu,
                fabVisible = books.isNotEmpty(),
                onExpandChange = { showFabMenu = it },
                snackbarHostState = snackbarHostState,
                onFolderPicked = onFolderPicked,
                onFilePicked = onFilePicked,
            )
        },
    ) { padding ->
        LibraryListContent(
            continueBook = continueBook,
            books = books,
            isScanning = isScanning,
            isRefreshing = isRefreshing,
            playbackStatus = playbackStatus,
            onBookClick = onBookClick,
            onTogglePlayPause = onTogglePlayPause,
            onPrepareAndPlay = onPrepareAndPlay,
            onUnavailableBookClicked = onUnavailableBookClicked,
            onResyncAll = onResyncAll,
            onDeleteRequested = onDeleteRequested,
            sortOption = sortOption,
            onSortOptionSelected = onSortOptionSelected,
            onImportClick = { showFabMenu = true },
            scaffoldPadding = padding,
        )
    }
    if (isScanning || scanResult != null) {
        LibraryImportOutcomeModal(
            isScanning = isScanning,
            sourceName = scanningSourceName,
            result = scanResult,
            onDismiss = onScanResultDismiss,
            onChooseSource = {
                onScanResultDismiss()
                showFabMenu = true
            },
        )
    }
    unavailableBook?.let { book ->
        UnavailableSourceModal(
            book = book,
            onDismiss = onUnavailableDismiss,
            onCheckAgain = { onRecheckUnavailable(book.book.id) },
        )
    }
}

@Composable
private fun LibraryFabMenuWithPickers(
    expanded: Boolean,
    fabVisible: Boolean,
    onExpandChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    onFolderPicked: (Uri, android.content.ContentResolver, String?) -> Unit,
    onFilePicked: (Uri, android.content.ContentResolver, String?) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val msgFilePickerUnavailable = stringResource(R.string.snackbar_file_picker_unavailable)
    val onPickerUnavailable: () -> Unit = {
        coroutineScope.launch { snackbarHostState.showSnackbar(msgFilePickerUnavailable) }
    }
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            onFolderPicked(it, context.contentResolver, DocumentFile.fromTreeUri(context, it)?.name)
        }
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            onFilePicked(it, context.contentResolver, DocumentFile.fromSingleUri(context, it)?.name)
        }
    }

    LibraryFabMenu(
        expanded = expanded,
        fabVisible = fabVisible,
        onExpandChange = onExpandChange,
        onAddFolder = {
            launchSafPickerSafely(
                launch = { folderPicker.launch(null) },
                onUnavailable = onPickerUnavailable,
            )
        },
        onAddFile = {
            launchSafPickerSafely(
                launch = { filePicker.launch(arrayOf("audio/*")) },
                onUnavailable = onPickerUnavailable,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onSettingsClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_brand)) },
        scrollBehavior = scrollBehavior,
        actions = {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("settings_button"),
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                )
            }
        },
    )
}

@Composable
private fun LibrarySortSheet(
    selectedSortOption: LibrarySortOption,
    onDismiss: () -> Unit,
    onSortOptionSelected: (LibrarySortOption) -> Unit,
) {
    ResponsiveLibraryModal(onDismiss = onDismiss, testTag = "sort_menu") {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            LibraryModalHeader(stringResource(R.string.sort_menu_title), onDismiss)
            LibrarySortOption.entries.forEach { option ->
                val selected = option == selectedSortOption
                val selectedDescription = if (selected) stringResource(R.string.sort_selected) else null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sort_option_${option.name}")
                        .semantics { selectedDescription?.let { stateDescription = it } }
                        .clickable { onSortOptionSelected(option) }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(sortOptionLabel(option), modifier = Modifier.weight(1f))
                    RadioButton(selected = selected, onClick = { onSortOptionSelected(option) })
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss, modifier = Modifier.testTag("sheet_close")) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cd_close))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResponsiveLibraryModal(
    dismissible: Boolean = true,
    onDismiss: () -> Unit,
    testTag: String,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val modalMaxHeight = maxHeight * MODAL_MAX_HEIGHT_FRACTION
        if (maxWidth >= 600.dp) {
            Dialog(
                onDismissRequest = { if (dismissible) onDismiss() },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    Surface(
                        modifier = Modifier.width(420.dp).heightIn(max = modalMaxHeight).testTag(testTag),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                            Box(
                                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    Modifier
                                        .size(width = 48.dp, height = 4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
                                )
                            }
                            content()
                        }
                    }
                }
            }
        } else {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { dismissible || it != SheetValue.Hidden },
            )
            ModalBottomSheet(
                onDismissRequest = { if (dismissible) onDismiss() },
                sheetState = sheetState,
                modifier = Modifier.testTag(testTag),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun LibraryModalHeader(title: String, onDismiss: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        onDismiss?.let { dismiss ->
            IconButton(onClick = dismiss, modifier = Modifier.testTag("sheet_close")) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.library_modal_close))
            }
        }
    }
}

@Composable
private fun LibraryStateIcon(icon: ImageVector) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Suppress("LongMethod") // This modal's scan-result states form one cohesive Compose presentation.
@Composable
private fun LibraryImportOutcomeModal(
    isScanning: Boolean,
    sourceName: String?,
    result: ScanResult?,
    onDismiss: () -> Unit,
    onChooseSource: () -> Unit,
) {
    ResponsiveLibraryModal(
        dismissible = !isScanning,
        onDismiss = onDismiss,
        testTag = "import_result_sheet",
    ) {
        val scanProgressDescription = stringResource(R.string.library_scan_in_progress)
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            val title = when {
                isScanning -> stringResource(R.string.library_import_scanning_title)
                result is ScanResult.Success -> stringResource(R.string.library_import_added_title)
                result is ScanResult.Empty -> stringResource(R.string.library_import_no_audio_title)
                result is ScanResult.Duplicate -> stringResource(R.string.library_import_duplicate_title)
                else -> stringResource(R.string.library_import_permission_title)
            }
            val stateIcon = when {
                isScanning -> Icons.Default.CreateNewFolder
                result is ScanResult.Success -> Icons.Default.Check
                result is ScanResult.Empty -> Icons.Default.Search
                result is ScanResult.Duplicate -> Icons.Default.Info
                else -> Icons.Default.Folder
            }
            LibraryModalHeader(title, if (isScanning) null else onDismiss)
            LibraryStateIcon(stateIcon)
            when {
                isScanning -> {
                    Text(
                        text = sourceName?.let { stringResource(R.string.library_import_scanning_folder, it) }
                            ?: stringResource(R.string.library_import_scanning_message),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.CenterHorizontally)
                            .semantics {
                                contentDescription = scanProgressDescription
                            },
                    )
                }
                result is ScanResult.Success -> {
                    Text(
                        result.title,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            pluralStringResource(
                                R.plurals.library_import_record_count,
                                result.recordCount,
                                result.recordCount,
                            ),
                        )
                        Text(" · ")
                        Text(
                            pluralStringResource(
                                R.plurals.library_import_audio_file_count,
                                result.audioFileCount,
                                result.audioFileCount,
                            ),
                        )
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).testTag("import_result_action"),
                    ) {
                        Text(stringResource(R.string.library_import_open))
                    }
                }
                result is ScanResult.Empty -> {
                    Text(
                        stringResource(R.string.snackbar_no_audio_files),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = onChooseSource,
                        modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("import_result_action"),
                    ) {
                        Text(stringResource(R.string.library_import_choose_folder))
                    }
                }
                result is ScanResult.Duplicate -> {
                    Text(
                        stringResource(R.string.library_import_duplicate_message, result.sourceName),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("import_result_action"),
                    ) {
                        Text(stringResource(R.string.library_import_open_library))
                    }
                }
                else -> {
                    Text(
                        stringResource(R.string.snackbar_permission_denied),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = onChooseSource,
                        modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("import_result_action"),
                    ) {
                        Text(stringResource(R.string.library_import_choose_source))
                    }
                }
            }
        }
    }
}

@Composable
private fun UnavailableSourceModal(
    book: BookWithProgress,
    onDismiss: () -> Unit,
    onCheckAgain: () -> Unit,
) {
    ResponsiveLibraryModal(onDismiss = onDismiss, testTag = "unavailable_source_sheet") {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            LibraryModalHeader(stringResource(R.string.library_source_unavailable_title), onDismiss)
            LibraryStateIcon(Icons.Default.Warning)
            Text(
                book.book.title,
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(R.string.library_source_unavailable_message),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = onCheckAgain,
                modifier = Modifier.fillMaxWidth().padding(24.dp).testTag("unavailable_source_retry"),
            ) {
                Text(stringResource(R.string.library_source_check_again))
            }
        }
    }
}

@Composable
private fun LibraryFilterSheet(
    selectedFilter: LibraryFilter,
    counts: Map<LibraryFilter, Int>,
    onDismiss: () -> Unit,
    onFilterChange: (LibraryFilter) -> Unit,
) {
    ResponsiveLibraryModal(onDismiss = onDismiss, testTag = "filter_sheet") {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            LibraryModalHeader(stringResource(R.string.library_filter_sheet_title), onDismiss)
            LibraryFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                val selectedDescription = if (selected) stringResource(R.string.library_filter_selected) else null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("library_filter_${filter.name}")
                        .semantics { selectedDescription?.let { stateDescription = it } }
                        .clickable { onFilterChange(filter) }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(filter.labelResource()) + " · " + counts.getValue(filter),
                        modifier = Modifier.weight(1f),
                    )
                    RadioButton(selected = selected, onClick = { onFilterChange(filter) })
                }
            }
        }
    }
}

@Composable
private fun sortOptionLabel(option: LibrarySortOption): String = stringResource(
    when (option) {
        LibrarySortOption.ADDED_AT_ASC -> R.string.sort_added_at_ascending
        LibrarySortOption.ADDED_AT_DESC -> R.string.sort_added_at_descending
        LibrarySortOption.TITLE_ASC -> R.string.sort_title_ascending
        LibrarySortOption.TITLE_DESC -> R.string.sort_title_descending
    },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryFabMenu(
    expanded: Boolean,
    fabVisible: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onAddFolder: () -> Unit,
    onAddFile: () -> Unit,
) {
    if (fabVisible) {
        FloatingActionButton(
            onClick = { onExpandChange(true) },
            modifier = Modifier.testTag("fab_add"),
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.library_empty_add_audio))
        }
    }
    if (expanded) {
        ResponsiveLibraryModal(
            onDismiss = { onExpandChange(false) },
            testTag = "fab_menu",
        ) {
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                SheetHeader(stringResource(R.string.library_add_audio_title)) {
                    onExpandChange(false)
                }
                LibraryAddAudioOption(
                    icon = Icons.Default.CreateNewFolder,
                    title = stringResource(R.string.menu_add_folder),
                    testTag = "menu_add_folder",
                    onClick = {
                        onExpandChange(false)
                        onAddFolder()
                    },
                )
                LibraryAddAudioOption(
                    icon = Icons.Default.AudioFile,
                    title = stringResource(R.string.menu_add_file),
                    testTag = "menu_add_file",
                    onClick = {
                        onExpandChange(false)
                        onAddFile()
                    },
                )
                HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        stringResource(R.string.library_add_folder_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.library_add_files_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryAddAudioOption(
    icon: ImageVector,
    title: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Suppress("LongMethod") // This list composes coordinated loading, empty, and book-list states.
@Composable
private fun LibraryListContent(
    continueBook: ContinueBookUiState?,
    books: List<BookWithProgress>,
    isScanning: Boolean,
    isRefreshing: Boolean,
    playbackStatus: PlaybackStatus,
    sortOption: LibrarySortOption,
    onSortOptionSelected: (LibrarySortOption) -> Unit,
    onBookClick: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrepareAndPlay: (Long) -> Unit,
    onUnavailableBookClicked: (Long) -> Unit,
    onResyncAll: () -> Unit,
    onDeleteRequested: (BookWithProgress) -> Unit,
    onImportClick: () -> Unit,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(LibraryFilter.ALL) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    val filteredBooks = remember(books, query, selectedFilter) {
        filterLibraryBooks(books, query, selectedFilter)
    }
    val filterCounts = remember(books) {
        LibraryFilter.entries.associateWith { filterLibraryBooks(books, "", it).size }
    }
    val filteredContinueBook = remember(continueBook, query, selectedFilter) {
        continueBook?.takeIf {
            filterLibraryBooks(listOf(it.book), query, selectedFilter).isNotEmpty()
        }
    }
    val filteredBookCount = filteredBooks.size
    val totalBookCount = books.size
    val hasLibraryContent = books.isNotEmpty()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onResyncAll,
        modifier = modifier.fillMaxSize().consumeWindowInsets(scaffoldPadding).imePadding(),
    ) {
        if (!hasLibraryContent && !isScanning) {
            EmptyLibrary(
                onImportClick = onImportClick,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                if (maxWidth >= 840.dp) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(WIDE_LIBRARY_DETAILS_WEIGHT)
                                .fillMaxHeight()
                                .padding(start = 20.dp, top = 20.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            LibraryHeader(totalBookCount = totalBookCount)
                            LibrarySearchAndFilters(
                                query = query,
                                onQueryChange = { query = it },
                            )
                            filteredContinueBook?.let {
                                ContinueBookItem(
                                    continueItem = it,
                                    playbackStatus = playbackStatus,
                                    onBookClick = onBookClick,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onPrepareAndPlay = onPrepareAndPlay,
                                    onUnavailableBookClicked = onUnavailableBookClicked,
                                    onDeleteRequested = onDeleteRequested,
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(WIDE_LIBRARY_BOOKS_WEIGHT)
                                .fillMaxHeight()
                                .padding(top = 20.dp, end = 20.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            LibraryFilterSortRow(
                                resultCount = filteredBookCount,
                                onShowFilterSheet = { showFilterSheet = true },
                                onShowSortSheet = { showSortSheet = true },
                            )
                            LibraryBookPane(
                                books = filteredBooks,
                                query = query,
                                selectedFilter = selectedFilter,
                                showNoResults = filteredContinueBook == null && !isScanning,
                                onClear = {
                                    query = ""
                                    selectedFilter = LibraryFilter.ALL
                                },
                                playbackStatus = playbackStatus,
                                onBookClick = onBookClick,
                                onUnavailableBookClicked = onUnavailableBookClicked,
                                onDeleteRequested = onDeleteRequested,
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                            )
                        }
                    }
                } else {
                    val layoutDirection = LocalLayoutDirection.current
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp + scaffoldPadding.calculateLeftPadding(layoutDirection),
                            top = 16.dp + scaffoldPadding.calculateTopPadding(),
                            end = 16.dp + scaffoldPadding.calculateRightPadding(layoutDirection),
                            bottom = 16.dp + scaffoldPadding.calculateBottomPadding(),
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("books_list"),
                    ) {
                        item(key = "library_header", contentType = "header") {
                            LibraryHeader(totalBookCount = totalBookCount)
                        }
                        item(key = "library_search", contentType = "search") {
                            LibrarySearchAndFilters(
                                query = query,
                                onQueryChange = { query = it },
                            )
                        }
                        filteredContinueBook?.let { continueItem ->
                            item(
                                key = "continue_${continueItem.book.book.id}",
                                contentType = "continue",
                            ) {
                                ContinueBookItem(
                                    continueItem = continueItem,
                                    playbackStatus = playbackStatus,
                                    onBookClick = onBookClick,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onPrepareAndPlay = onPrepareAndPlay,
                                    onUnavailableBookClicked = onUnavailableBookClicked,
                                    onDeleteRequested = onDeleteRequested,
                                )
                            }
                        }
                        item(key = "library_filters", contentType = "filters") {
                            LibraryFilterSortRow(
                                resultCount = filteredBookCount,
                                onShowFilterSheet = { showFilterSheet = true },
                                onShowSortSheet = { showSortSheet = true },
                            )
                        }
                        if (filteredBooks.isEmpty() && filteredContinueBook == null && !isScanning) {
                            item(key = "library_empty_results", contentType = "empty") {
                                LibraryNoResults(
                                    query = query,
                                    selectedFilter = selectedFilter,
                                    onClear = {
                                        query = ""
                                        selectedFilter = LibraryFilter.ALL
                                    },
                                )
                            }
                        } else {
                            items(
                                filteredBooks,
                                key = { it.book.id },
                                contentType = { "book" },
                            ) { bookWithProgress ->
                                LibraryBookItem(
                                    bookWithProgress = bookWithProgress,
                                    playbackStatus = playbackStatus,
                                    onBookClick = onBookClick,
                                    onUnavailableBookClicked = onUnavailableBookClicked,
                                    onDeleteRequested = onDeleteRequested,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showFilterSheet) {
        LibraryFilterSheet(
            selectedFilter = selectedFilter,
            counts = filterCounts,
            onDismiss = { showFilterSheet = false },
            onFilterChange = {
                selectedFilter = it
                showFilterSheet = false
            },
        )
    }
    if (showSortSheet) {
        LibrarySortSheet(
            selectedSortOption = sortOption,
            onDismiss = { showSortSheet = false },
            onSortOptionSelected = {
                onSortOptionSelected(it)
                showSortSheet = false
            },
        )
    }
}

@Composable
private fun LibraryHeader(totalBookCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = pluralStringResource(
                R.plurals.library_book_count,
                totalBookCount,
                totalBookCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LibrarySearchAndFilters(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val searchLabel = stringResource(R.string.library_search_label)
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("library_search")
            .semantics { contentDescription = searchLabel },
        singleLine = true,
        placeholder = { Text(stringResource(R.string.library_search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.testTag("library_search_clear"),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.library_search_clear),
                    )
                }
            }
        } else {
            null
        },
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun LibraryFilterSortRow(
    resultCount: Int,
    onShowFilterSheet: () -> Unit,
    onShowSortSheet: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = pluralStringResource(
                R.plurals.library_result_count,
                resultCount,
                resultCount,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onShowFilterSheet,
            modifier = Modifier.testTag("filter_button"),
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = stringResource(R.string.cd_filter_library),
            )
        }
        IconButton(
            onClick = onShowSortSheet,
            modifier = Modifier.testTag("sort_button"),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.cd_sort_library),
            )
        }
    }
}

private fun LibraryFilter.labelResource(): Int = when (this) {
    LibraryFilter.ALL -> R.string.library_filter_all
    LibraryFilter.IN_PROGRESS -> R.string.library_filter_in_progress
    LibraryFilter.NOT_STARTED -> R.string.library_filter_not_started
    LibraryFilter.COMPLETED -> R.string.library_filter_completed
}

@Composable
private fun LibraryBookPane(
    books: List<BookWithProgress>,
    query: String,
    showNoResults: Boolean,
    selectedFilter: LibraryFilter,
    onClear: () -> Unit,
    playbackStatus: PlaybackStatus,
    onBookClick: (Long) -> Unit,
    onUnavailableBookClicked: (Long) -> Unit,
    onDeleteRequested: (BookWithProgress) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier) {
        if (books.isEmpty() && showNoResults) {
            LibraryNoResults(
                query = query,
                selectedFilter = selectedFilter,
                onClear = onClear,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (books.isEmpty()) {
            Spacer(Modifier.fillMaxSize())
        } else if (maxWidth >= 620.dp) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 4.dp,
                    top = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("books_list"),
            ) {
                items(books, key = { it.book.id }, contentType = { "book" }) { bookWithProgress ->
                    LibraryBookItem(
                        bookWithProgress = bookWithProgress,
                        playbackStatus = playbackStatus,
                        onBookClick = onBookClick,
                        onUnavailableBookClicked = onUnavailableBookClicked,
                        onDeleteRequested = onDeleteRequested,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 4.dp,
                    top = 4.dp,
                    end = 4.dp,
                    bottom = 4.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.testTag("books_list"),
            ) {
                items(books, key = { it.book.id }, contentType = { "book" }) { bookWithProgress ->
                    LibraryBookItem(
                        bookWithProgress = bookWithProgress,
                        playbackStatus = playbackStatus,
                        onBookClick = onBookClick,
                        onUnavailableBookClicked = onUnavailableBookClicked,
                        onDeleteRequested = onDeleteRequested,
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryNoResults(
    query: String,
    selectedFilter: LibraryFilter,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(
                if (query.isNotBlank()) {
                    R.string.library_search_no_match_title
                } else {
                    R.string.library_filter_no_books_title
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (query.isNotBlank()) {
                    R.string.library_search_no_match_message
                } else {
                    R.string.library_filter_no_books_message
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (query.isNotBlank() || selectedFilter != LibraryFilter.ALL) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.library_clear_search_and_filter))
            }
        }
    }
}

@Composable
private fun ContinueBookItem(
    continueItem: ContinueBookUiState,
    playbackStatus: PlaybackStatus,
    onBookClick: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPrepareAndPlay: (Long) -> Unit,
    onUnavailableBookClicked: (Long) -> Unit,
    onDeleteRequested: (BookWithProgress) -> Unit,
) {
    val bookWithProgress = continueItem.book
    val bookId = bookWithProgress.book.id
    val isActive = playbackStatus.activeBookId == bookId
    val cardState = rememberCardState(
        bookWithProgress = bookWithProgress,
        playbackStatus = playbackStatus,
        isHighlighted = true,
    )
    ContinueListeningSection(
        title = stringResource(continueSectionTitleRes(continueItem.status)),
        stateDescription = stringResource(continueSectionStatusRes(continueItem.status)),
        card = {
            BookCard(
                uiState = cardState,
                onClick = { handleBookClick(bookWithProgress, onBookClick, onUnavailableBookClicked) },
                onLongClick = { onDeleteRequested(bookWithProgress) },
                onPrimaryAction = {
                    when {
                        !bookWithProgress.book.isAvailable -> onUnavailableBookClicked(bookId)
                        isActive && playbackStatus.isMediaLoaded -> onTogglePlayPause()
                        else -> onPrepareAndPlay(bookId)
                    }
                },
                primaryActionDescription = stringResource(
                    if (isActive && playbackStatus.isPlaying) {
                        R.string.cd_pause_playback
                    } else {
                        R.string.cd_resume_playback
                    },
                ),
                statusLabel = stringResource(continueSectionStatusRes(continueItem.status)),
                testTag = "continue_book_card_$bookId",
            )
        },
        modifier = Modifier.testTag("continue_section"),
        cardModifier = Modifier,
    )
}

@Composable
private fun LibraryBookItem(
    bookWithProgress: BookWithProgress,
    playbackStatus: PlaybackStatus,
    onBookClick: (Long) -> Unit,
    onUnavailableBookClicked: (Long) -> Unit,
    onDeleteRequested: (BookWithProgress) -> Unit,
) {
    val statusResource = when {
        bookWithProgress.progress?.isCompleted == true || bookWithProgress.progressFraction >= 1f ->
            R.string.library_filter_completed
        bookWithProgress.progressFraction > 0f -> R.string.library_filter_in_progress
        else -> R.string.library_filter_not_started
    }
    BookCard(
        uiState = rememberCardState(bookWithProgress, playbackStatus, isHighlighted = false),
        onClick = { handleBookClick(bookWithProgress, onBookClick, onUnavailableBookClicked) },
        onLongClick = { onDeleteRequested(bookWithProgress) },
        modifier = Modifier,
        statusLabel = stringResource(statusResource),
    )
}

private fun continueSectionTitleRes(status: ContinueBookStatus): Int =
    if (status == ContinueBookStatus.LAST_PLAYED) {
        R.string.section_last_played
    } else {
        R.string.section_continue_listening
    }

private fun continueSectionStatusRes(status: ContinueBookStatus): Int =
    when (status) {
        ContinueBookStatus.CONTINUE -> R.string.continue_status_continue
        ContinueBookStatus.PLAYING -> R.string.cd_playing
        ContinueBookStatus.PAUSED -> R.string.cd_paused
        ContinueBookStatus.LAST_PLAYED -> R.string.continue_status_last_played
    }

private fun handleBookClick(
    bookWithProgress: BookWithProgress,
    onBookClick: (Long) -> Unit,
    onUnavailableBookClicked: (Long) -> Unit,
) {
    if (bookWithProgress.book.isAvailable) {
        onBookClick(bookWithProgress.book.id)
    } else {
        onUnavailableBookClicked(bookWithProgress.book.id)
    }
}

private fun rememberCardState(
    bookWithProgress: BookWithProgress,
    playbackStatus: PlaybackStatus,
    isHighlighted: Boolean = true,
): LibraryBookCardUiState {
    val isActive = playbackStatus.activeBookId == bookWithProgress.book.id
    return LibraryBookCardUiState(
        bookWithProgress = bookWithProgress,
        isPlaying = isActive && playbackStatus.isPlaying,
        isHighlighted = isHighlighted,
    )
}

@Suppress("LongMethod") // The confirmation form is a single responsive Compose dialog.
@Composable
private fun DeleteBookDialog(
    book: BookWithProgress?,
    onDismiss: () -> Unit,
    onConfirm: (BookWithProgress, Boolean) -> Unit,
) {
    book ?: return
    var deleteFiles by remember(book.book.id) { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val foldablePanel = maxWidth >= 600.dp && maxWidth < 1000.dp
        val modalMaxHeight = maxHeight * MODAL_MAX_HEIGHT_FRACTION
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = if (foldablePanel) Alignment.CenterEnd else Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 364.dp)
                        .heightIn(max = modalMaxHeight)
                        .testTag("delete_book_dialog"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp),
                    ) {
                        LibraryModalHeader(
                            stringResource(
                                if (deleteFiles) {
                                    R.string.dialog_delete_files_title
                                } else {
                                    R.string.dialog_delete_title
                                },
                            ),
                            onDismiss,
                        )
                        Text(
                            stringResource(R.string.dialog_delete_message, book.book.title),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { deleteFiles = !deleteFiles }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = deleteFiles, onCheckedChange = { deleteFiles = it })
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.dialog_delete_also_files),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            stringResource(
                                if (deleteFiles) {
                                    R.string.dialog_delete_irreversible
                                } else {
                                    R.string.dialog_delete_stays_on_device
                                },
                            ),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (deleteFiles) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(stringResource(R.string.dialog_delete_cancel))
                            }
                            Button(onClick = { onConfirm(book, deleteFiles) }) {
                                Text(
                                    stringResource(
                                        if (deleteFiles) {
                                            R.string.dialog_delete_files_confirm
                                        } else {
                                            R.string.dialog_delete_confirm
                                        },
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("LongMethod") // The card's two layouts share state and interactions in this composable.
@Composable
private fun BookCard(
    uiState: LibraryBookCardUiState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionDescription: String? = null,
    statusLabel: String? = null,
    testTag: String = "book_card_${uiState.bookWithProgress.book.id}",
) {
    val book = uiState.bookWithProgress.book

    if (uiState.isHighlighted) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (book.isAvailable) 1f else UNAVAILABLE_BOOK_ALPHA)
                .testTag(testTag)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookThumbnail(
                    coverUri = book.coverUri,
                    title = book.title,
                    modifier = Modifier.size(72.dp),
                )
                Spacer(Modifier.width(12.dp))
                BookCardContent(
                    bookWithProgress = uiState.bookWithProgress,
                    statusLabel = statusLabel,
                    modifier = Modifier.weight(1f),
                )
                onPrimaryAction?.let { action ->
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = action,
                        modifier = Modifier.size(56.dp).testTag("continue_play_button_${book.id}"),
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = primaryActionDescription,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }
        }
    } else {
        val fraction = uiState.bookWithProgress.progressFraction
        val isCompleted = uiState.bookWithProgress.progress?.isCompleted == true || fraction >= 1f
        Column(
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (book.isAvailable) 1f else UNAVAILABLE_BOOK_ALPHA)
                .testTag(testTag)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BookThumbnail(
                    coverUri = book.coverUri,
                    title = book.title,
                    modifier = Modifier.size(56.dp),
                )
                Spacer(Modifier.width(12.dp))
                BookCardContent(
                    bookWithProgress = uiState.bookWithProgress,
                    statusLabel = statusLabel,
                    compactRow = true,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.chip_completed),
                        modifier = Modifier.size(24.dp).testTag("book_completed_chip_${book.id}"),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.progress_percent, (fraction * 100).toInt()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("book_progress_text_${book.id}"),
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(start = 76.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun ContinueListeningSection(
    title: String,
    stateDescription: String,
    card: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .testTag("continue_header")
                .semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = cardModifier.semantics(mergeDescendants = true) {
                this.stateDescription = stateDescription
            },
        ) {
            card()
        }
    }
}

@Composable
private fun BookCardContent(
    bookWithProgress: BookWithProgress,
    statusLabel: String? = null,
    compactRow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val book = bookWithProgress.book
    val fraction = bookWithProgress.progressFraction
    val isCompleted = bookWithProgress.progress?.isCompleted == true || fraction >= 1f

    Column(modifier = modifier) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("book_title_${book.id}"),
        )
        book.author?.let { author ->
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        statusLabel?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { fraction },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().testTag("book_progress_${book.id}"),
        )
        if (!compactRow) {
            Spacer(Modifier.height(4.dp))
            if (isCompleted) {
                Row(
                    modifier = Modifier.testTag("book_completed_chip_${book.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.chip_completed),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(R.string.chip_completed), style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Text(
                    text = stringResource(R.string.progress_percent, (fraction * 100).toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("book_progress_text_${book.id}"),
                )
            }
        }
    }
}

@Composable
private fun BookThumbnail(
    coverUri: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    if (coverUri != null) {
        AsyncImage(
            model = coverUri,
            contentDescription = stringResource(R.string.cd_cover_for, title),
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title.take(2).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun EmptyLibraryIllustration() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val background = when (index) {
                0 -> MaterialTheme.colorScheme.secondaryContainer
                1 -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
            val foreground = when (index) {
                0 -> MaterialTheme.colorScheme.onSecondaryContainer
                1 -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onTertiaryContainer
            }
            Column(
                modifier = Modifier
                    .rotate((index - 1) * EMPTY_COVER_ROTATION_DEGREES)
                    .size(width = 56.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(background)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(foreground),
                )
                Spacer(Modifier.height(8.dp))
                Box(Modifier.width(32.dp).height(3.dp).clip(CircleShape).background(foreground))
                Spacer(Modifier.height(5.dp))
                Box(Modifier.width(24.dp).height(3.dp).clip(CircleShape).background(foreground))
            }
        }
    }
}

@Composable
private fun EmptyLibrary(
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            EmptyLibraryIllustration()
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.library_empty_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.library_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = onImportClick,
            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("empty_library_import"),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.library_empty_add_audio))
        }
    }
}
