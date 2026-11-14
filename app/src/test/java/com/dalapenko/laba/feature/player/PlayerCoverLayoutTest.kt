package com.dalapenko.laba.feature.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerCoverLayoutTest {
    @Test
    fun artworkSpacingShrinksBeforeScrollWhenContentNearlyFillsViewport() {
        val budget = playerArtworkBudget(
            availableHeight = 500,
            reservedHeight = 460,
            availableWidth = 500,
            maxCoverSize = 237,
            desiredSpacing = PlayerArtworkSpacing(48, 12),
        )

        assertEquals(32, budget.topSpacing)
        assertEquals(8, budget.bottomSpacing)
        assertEquals(0, budget.coverSize)
        assertEquals(500, budget.contentHeight)
    }

    @Test
    fun smallCoverUsesFullArtworkSpacingWhenItFits() {
        val budget = playerArtworkBudget(540, 460, 500, 237, PlayerArtworkSpacing(48, 12))

        assertEquals(48, budget.topSpacing)
        assertEquals(12, budget.bottomSpacing)
        assertEquals(20, budget.coverSize)
        assertEquals(540, budget.contentHeight)
    }

    @Test
    fun artworkUsesFullSizeWhenThereIsEnoughSpace() {
        val budget = playerArtworkBudget(800, 460, 500, 237, PlayerArtworkSpacing(48, 12))

        assertEquals(48, budget.topSpacing)
        assertEquals(12, budget.bottomSpacing)
        assertEquals(237, budget.coverSize)
        assertEquals(757, budget.contentHeight)
    }

    @Test
    fun scrollFallbackRemainsWhenMetadataAndControlsExceedViewport() {
        val budget = playerArtworkBudget(500, 520, 400, 237, PlayerArtworkSpacing(48, 12))

        assertEquals(0, budget.topSpacing)
        assertEquals(0, budget.bottomSpacing)
        assertEquals(0, budget.coverSize)
        check(budget.contentHeight > 500) { "Expected overflow to remain scrollable" }
    }

    @Test
    fun artworkIsCenteredInActualLayoutWidth() {
        assertEquals(131, centeredPlayerChildX(layoutWidth = 500, childWidth = 237))
        assertEquals(0, centeredPlayerChildX(layoutWidth = 237, childWidth = 237))
    }
}
