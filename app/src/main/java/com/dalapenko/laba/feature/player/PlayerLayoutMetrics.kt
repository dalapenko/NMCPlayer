package com.dalapenko.laba.feature.player

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val PLAYER_WIDE_LAYOUT_MIN_WIDTH = 720.dp
internal val PLAYER_COMPACT_LAYOUT_MAX_HEIGHT = 500.dp

internal data class PlayerArtworkBudget(
    val topSpacing: Int,
    val bottomSpacing: Int,
    val coverSize: Int,
    val contentHeight: Int,
)

internal fun playerArtworkBudget(
    availableHeight: Int,
    reservedHeight: Int,
    availableWidth: Int,
    maxCoverSize: Int,
): PlayerArtworkBudget {
    val remainingHeight = (availableHeight - reservedHeight).coerceAtLeast(0)
    val coverSize = minOf(maxCoverSize, availableWidth, remainingHeight)
    val remainingSpacing = remainingHeight - coverSize
    val topSpacing = remainingSpacing / 2
    val bottomSpacing = remainingSpacing - topSpacing
    return PlayerArtworkBudget(
        topSpacing = topSpacing,
        bottomSpacing = bottomSpacing,
        coverSize = coverSize,
        contentHeight = reservedHeight + remainingHeight,
    )
}

internal fun centeredPlayerChildX(layoutWidth: Int, childWidth: Int): Int =
    ((layoutWidth - childWidth) / 2).coerceAtLeast(0)

internal fun playerLayoutBreakpoints(width: Dp, height: Dp): Pair<Boolean, Boolean> =
    (width >= PLAYER_WIDE_LAYOUT_MIN_WIDTH) to (height < PLAYER_COMPACT_LAYOUT_MAX_HEIGHT)

internal fun widePlayerArtworkSize(
    availableHeight: Int,
    reservedHeight: Int,
    availableWidth: Int,
    maxCoverSize: Int,
    outerSpacing: Int,
): Int {
    val heightBudget = (availableHeight - reservedHeight - outerSpacing).coerceAtLeast(0)
    return minOf(maxCoverSize, availableWidth.coerceAtLeast(0), heightBudget)
}
