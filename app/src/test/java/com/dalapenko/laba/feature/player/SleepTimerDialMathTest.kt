package com.dalapenko.laba.feature.player

import org.junit.Assert.assertEquals
import org.junit.Test

class SleepTimerDialMathTest {

    // ── ringCountForMinutes ──────────────────────────────────────────────────

    @Test
    fun givenZeroMinutes_whenRingCount_thenOneRing() {
        assertEquals(1, ringCountForMinutes(0))
    }

    @Test
    fun givenOneMinute_whenRingCount_thenOneRing() {
        assertEquals(1, ringCountForMinutes(1))
    }

    @Test
    fun givenExactlySixtyMinutes_whenRingCount_thenOneRing() {
        assertEquals(1, ringCountForMinutes(60))
    }

    @Test
    fun givenSixtyOneMinutes_whenRingCount_thenTwoRings() {
        assertEquals(2, ringCountForMinutes(61))
    }

    @Test
    fun givenExactlyOneHundredTwentyMinutes_whenRingCount_thenTwoRings() {
        assertEquals(2, ringCountForMinutes(120))
    }

    @Test
    fun givenOneHundredTwentyOneMinutes_whenRingCount_thenTwoRings() {
        assertEquals(2, ringCountForMinutes(121))
    }

    @Test
    fun givenMaxMinutes_whenRingCount_thenTwoRings() {
        assertEquals(2, ringCountForMinutes(SLEEP_TIMER_MAX_MINUTES))
    }

    @Test
    fun givenValueAboveMax_whenRingCount_thenClampedToTwoRings() {
        assertEquals(2, ringCountForMinutes(500))
    }

    // ── minutesToDialPosition ─────────────────────────────────────────────────

    @Test
    fun givenZeroMinutes_whenDialPosition_thenLapZeroAngleZero() {
        val position = minutesToDialPosition(0)
        assertEquals(0, position.lapIndex)
        assertEquals(0f, position.angleDegrees)
    }

    @Test
    fun givenOneMinute_whenDialPosition_thenLapZeroSmallAngle() {
        val position = minutesToDialPosition(1)
        assertEquals(0, position.lapIndex)
        assertEquals(6f, position.angleDegrees)
    }

    @Test
    fun givenExactlySixtyMinutes_whenDialPosition_thenLapZeroFullSweep() {
        val position = minutesToDialPosition(60)
        assertEquals(0, position.lapIndex)
        assertEquals(360f, position.angleDegrees)
    }

    @Test
    fun givenSixtyOneMinutes_whenDialPosition_thenLapOneSmallAngle() {
        val position = minutesToDialPosition(61)
        assertEquals(1, position.lapIndex)
        assertEquals(6f, position.angleDegrees)
    }

    @Test
    fun givenExactlyOneHundredTwentyMinutes_whenDialPosition_thenLapOneFullSweep() {
        val position = minutesToDialPosition(120)
        assertEquals(1, position.lapIndex)
        assertEquals(360f, position.angleDegrees)
    }

    @Test
    fun givenMaxMinutes_whenDialPosition_thenLapOneFullSweep() {
        val position = minutesToDialPosition(SLEEP_TIMER_MAX_MINUTES)
        assertEquals(1, position.lapIndex)
        assertEquals(360f, position.angleDegrees)
    }

    @Test
    fun givenValueAboveMax_whenDialPosition_thenClampedToMax() {
        assertEquals(minutesToDialPosition(120), minutesToDialPosition(500))
    }

    // ── touchAngleDegrees ─────────────────────────────────────────────────────

    @Test
    fun givenTouchAboveCenter_whenAngle_thenZeroDegrees() {
        assertEquals(0f, touchAngleDegrees(dx = 0f, dy = -1f), 0.01f)
    }

    @Test
    fun givenTouchRightOfCenter_whenAngle_thenNinetyDegrees() {
        assertEquals(90f, touchAngleDegrees(dx = 1f, dy = 0f), 0.01f)
    }

    @Test
    fun givenTouchBelowCenter_whenAngle_thenOneEightyDegrees() {
        assertEquals(180f, touchAngleDegrees(dx = 0f, dy = 1f), 0.01f)
    }

    @Test
    fun givenTouchLeftOfCenter_whenAngle_thenTwoSeventyDegrees() {
        assertEquals(270f, touchAngleDegrees(dx = -1f, dy = 0f), 0.01f)
    }

    // ── angleDelta ────────────────────────────────────────────────────────────

    @Test
    fun givenSimpleClockwiseMove_whenAngleDelta_thenPositiveDelta() {
        assertEquals(10f, angleDelta(previousDegrees = 350f, currentDegrees = 0f), 0.01f)
    }

    @Test
    fun givenSimpleCounterClockwiseMove_whenAngleDelta_thenNegativeDelta() {
        assertEquals(-10f, angleDelta(previousDegrees = 0f, currentDegrees = 350f), 0.01f)
    }

    @Test
    fun givenNoMove_whenAngleDelta_thenZero() {
        assertEquals(0f, angleDelta(previousDegrees = 45f, currentDegrees = 45f), 0.01f)
    }

    // ── accumulatedAngleToMinutesDelta ───────────────────────────────────────

    @Test
    fun givenSixDegrees_whenMinutesDelta_thenOneMinute() {
        assertEquals(1, accumulatedAngleToMinutesDelta(6f))
    }

    @Test
    fun givenNegativeSixDegrees_whenMinutesDelta_thenNegativeOneMinute() {
        assertEquals(-1, accumulatedAngleToMinutesDelta(-6f))
    }

    @Test
    fun givenMultipleRevolutions_whenMinutesDelta_thenMinutesBeyondSixty() {
        assertEquals(90, accumulatedAngleToMinutesDelta(540f)) // 1.5 revolutions
    }

    @Test
    fun givenZeroDegrees_whenMinutesDelta_thenZero() {
        assertEquals(0, accumulatedAngleToMinutesDelta(0f))
    }

    @Test
    fun givenDragBeyondMaximum_whenMinutesAfterDrag_thenClampedTo120() {
        assertEquals(120, minutesAfterDrag(valueAtDragStart = 115, accumulatedDegrees = 90f))
    }

    @Test
    fun givenDragBelowZero_whenMinutesAfterDrag_thenClampedToZero() {
        assertEquals(0, minutesAfterDrag(valueAtDragStart = 5, accumulatedDegrees = -90f))
    }

    @Test
    fun givenMultipleTurns_whenMinutesAfterDrag_thenAccumulatesTurnsWithinMaximum() {
        assertEquals(90, minutesAfterDrag(valueAtDragStart = 0, accumulatedDegrees = 540f))
    }
}
