package com.dalapenko.laba.screens

import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

/**
 * Page Object for the Player Screen.
 * 
 * Provides access to all UI elements on the player screen using the Page Object pattern.
 */
class PlayerScreen(semanticsProvider: SemanticsNodeInteractionsProvider) :
    ComposeScreen<PlayerScreen>(
        semanticsProvider = semanticsProvider,
        viewBuilderAction = { hasTestTag("player_screen") }
    ) {


    val playerBookTitle: KNode = child {
        hasTestTag("player_book_title")
    }
    val playerBookName: KNode = child {
        hasTestTag("player_book_name")
    }

    val overflowButton: KNode = child {
        hasTestTag("player_overflow_button")
    }

    val rewindButton: KNode = child {
        hasTestTag("rewind_button")
    }

    val forwardButton: KNode = child {
        hasTestTag("forward_button")
    }

    val previousChapterButton: KNode = child {
        hasTestTag("previous_chapter_button")
    }

    val nextChapterButton: KNode = child {
        hasTestTag("next_chapter_button")
    }

    val speedControl: KNode = child {
        hasTestTag("speed_control")
    }

}
