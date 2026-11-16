package com.dalapenko.laba.feature.player

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerCoverLayoutTest {
    @Test
    fun coverUsesAvailableHeightBeforeOverflow() {
        val budget = playerArtworkBudget(
            availableHeight = 500,
            reservedHeight = 460,
            availableWidth = 500,
            maxCoverSize = 237,
        )

        assertEquals(0, budget.topSpacing)
        assertEquals(0, budget.bottomSpacing)
        assertEquals(40, budget.coverSize)
        assertEquals(500, budget.contentHeight)
    }

    @Test
    fun outerSpacingCentersTheWholePlayerContent() {
        val budget = playerArtworkBudget(800, 460, 500, 180)

        assertEquals(budget.topSpacing, budget.bottomSpacing)
        assertEquals(80, budget.topSpacing)
        assertEquals(180, budget.coverSize)
        assertEquals(800, budget.contentHeight)
    }

    @Test
    fun wideArtworkShrinksToFitMeasuredMetadataAndProgressBeforeScroll() {
        val coverSize = widePlayerArtworkSize(
            availableHeight = 360,
            reservedHeight = 176,
            availableWidth = 320,
            maxCoverSize = 237,
            outerSpacing = 16,
        )

        assertEquals(168, coverSize)
        assertEquals(360, coverSize + 176 + 16)
    }

    @Test
    fun wideArtworkCanShrinkToZeroWithoutInvalidConstraintsWhenNoSpaceRemains() {
        val coverSize = widePlayerArtworkSize(
            availableHeight = 360,
            reservedHeight = 360,
            availableWidth = 320,
            maxCoverSize = 237,
            outerSpacing = 16,
        )

        assertEquals(0, coverSize)
        assertTrue(coverSize + 360 + 16 > 360)
        assertEquals(
            0,
            widePlayerArtworkSize(
                availableHeight = 0,
                reservedHeight = 10,
                availableWidth = -1,
                maxCoverSize = 237,
                outerSpacing = 16,
            ),
        )
    }

    @Test
    fun playerLayoutSwitchesAtWidthBreakpointAndUsesCompactHeightBreakpoint() {
        assertEquals(false to true, playerLayoutBreakpoints(719.dp, 499.dp))
        assertEquals(true to false, playerLayoutBreakpoints(720.dp, 500.dp))
    }

    @Test
    fun overflowKeepsControlsContentScrollableWithoutArtwork() {
        val budget = playerArtworkBudget(500, 520, 400, 237)

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
