package com.dalapenko.laba.feature.player

import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerControlsTest {
    @Test
    fun speedAdjustmentUsesExactFiveHundredthsAndClampsToBounds() {
        assertEquals(0.55f, adjustPlaybackSpeed(0.5f, 1), 0f)
        assertEquals(0.8f, adjustPlaybackSpeed(0.75f, 1), 0f)
        assertEquals(1.95f, adjustPlaybackSpeed(2f, -1), 0f)
        assertEquals(0.5f, adjustPlaybackSpeed(0.5f, -1), 0f)
        assertEquals(2f, adjustPlaybackSpeed(2f, 1), 0f)
    }
}
