package com.dalapenko.laba.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dalapenko.laba.core.data.BookRepository
import com.dalapenko.laba.core.database.entity.TrackEntity
import com.dalapenko.laba.core.media.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class PlayerFilesUiState(
    val title: String = "",
    val tracks: List<TrackEntity> = emptyList(),
    val isActiveBook: Boolean = false,
    val currentTrackIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = true,
)

class PlayerFilesViewModel(
    private val bookId: Long,
    private val repository: BookRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerFilesUiState())
    val uiState: StateFlow<PlayerFilesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = repository.getBookWithTracks(bookId)
            _uiState.value = _uiState.value.copy(
                title = result?.first?.title.orEmpty(),
                tracks = result?.second.orEmpty(),
                isLoading = false,
            )
        }
        viewModelScope.launch {
            combine(playbackController.playerState, playbackController.currentBookId) { state, activeBookId ->
                state to (activeBookId == bookId)
            }.collect { (playerState, isActiveBook) ->
                _uiState.value = _uiState.value.copy(
                    isActiveBook = isActiveBook,
                    currentTrackIndex = playerState.currentMediaItemIndex,
                    isPlaying = playerState.isPlaying,
                )
            }
        }
    }

    fun skipToTrack(index: Int) {
        if (_uiState.value.isActiveBook && index in _uiState.value.tracks.indices) {
            playbackController.seekToTrack(index)
        }
    }
}
