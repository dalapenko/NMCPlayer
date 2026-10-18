package com.dalapenko.laba.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.dalapenko.laba.feature.library.LibraryScreen
import com.dalapenko.laba.feature.player.PlayerFilesScreen
import com.dalapenko.laba.feature.player.PlayerScreen
import com.dalapenko.laba.feature.settings.SettingsScreen

private const val SUPPORTING_PANE_HEIGHT_FRACTION = 0.82f
private const val NAVIGATION_TRANSITION_DURATION_MILLIS = 200
private val NAVIGATION_SLIDE_DISTANCE = 24.dp
private val NAVIGATION_SLIDE_SPEC =
    tween<IntOffset>(durationMillis = NAVIGATION_TRANSITION_DURATION_MILLIS)
private val NAVIGATION_FADE_SPEC =
    tween<Float>(durationMillis = NAVIGATION_TRANSITION_DURATION_MILLIS)

private fun navigationTransition(offsetPx: Int): ContentTransform {
    val enter = slideInHorizontally(
        initialOffsetX = { offsetPx },
        animationSpec = NAVIGATION_SLIDE_SPEC,
    ) + fadeIn(animationSpec = NAVIGATION_FADE_SPEC)
    val exit = slideOutHorizontally(
        targetOffsetX = { -offsetPx },
        animationSpec = NAVIGATION_SLIDE_SPEC,
    ) + fadeOut(animationSpec = NAVIGATION_FADE_SPEC)
    return enter togetherWith exit
}

@Composable
private fun navigationForwardOffsetPx(): Int {
    val distancePx = with(LocalDensity.current) { NAVIGATION_SLIDE_DISTANCE.roundToPx() }
    return if (LocalLayoutDirection.current == LayoutDirection.Ltr) distancePx else -distancePx
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppNavHost(backStack: NavBackStack<NavKey>) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val forwardOffsetPx = navigationForwardOffsetPx()
        val openFilesOnEntry = maxWidth >= 1200.dp
        val supportingPaneHeight = maxHeight * SUPPORTING_PANE_HEIGHT_FRACTION
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            sceneStrategies = listOf(rememberSupportingPaneSceneStrategy<NavKey>()),
            transitionSpec = { navigationTransition(forwardOffsetPx) },
            popTransitionSpec = { navigationTransition(-forwardOffsetPx) },
            predictivePopTransitionSpec = { _ -> navigationTransition(-forwardOffsetPx) },
            entryProvider = entryProvider {
                entry<Library> {
                    LibraryScreen(
                        onBookClick = { bookId ->
                            backStack.add(Player(bookId))
                            if (openFilesOnEntry) backStack.add(PlayerFiles(bookId))
                        },
                        onSettingsClick = { backStack.add(Settings) },
                    )
                }
                entry<Settings> { SettingsScreen(onBack = { backStack.removeLastOrNull() }) }
                entry<Player>(metadata = SupportingPaneSceneStrategy.mainPane()) { route ->
                    PlayerScreen(
                        bookId = route.bookId,
                        autoPlay = route.autoPlay,
                        onBack = {
                            if (backStack.lastOrNull() == PlayerFiles(route.bookId)) {
                                backStack.removeLastOrNull()
                            }
                            backStack.removeLastOrNull()
                        },
                        onOpenFiles = {
                            if (backStack.lastOrNull() != PlayerFiles(route.bookId)) {
                                backStack.add(PlayerFiles(route.bookId))
                            }
                        },
                        filesInPane = openFilesOnEntry,
                    )
                }
                entry<PlayerFiles>(metadata = SupportingPaneSceneStrategy.supportingPane()) { route ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PlayerFilesScreen(
                            bookId = route.bookId,
                            onBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.width(420.dp),
                            maxPanelHeight = supportingPaneHeight,
                        )
                    }
                }
            },
        )
    }
}
