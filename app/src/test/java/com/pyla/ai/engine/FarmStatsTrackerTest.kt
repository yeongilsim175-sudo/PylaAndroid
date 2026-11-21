package com.pyla.ai.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class FarmStatsTrackerTest {
    @Test fun elapsedTimeExcludesPausedTime() {
        FarmStatsTracker.start(1_000L)
        FarmStatsTracker.setPaused(true, 6_000L)
        assertEquals(5_000L, FarmStatsTracker.snapshot(21_000L).activeMillis)
        FarmStatsTracker.setPaused(false, 25_000L)
        FarmStatsTracker.stop(30_000L)
        assertEquals(10_000L, FarmStatsTracker.snapshot(80_000L).activeMillis)
    }

    @Test fun winsLossesAndTrophiesUpdateWithFloor() {
        FarmStatsTracker.start(0L)
        FarmStatsTracker.recordMatch(MatchResultKind.VICTORY, 8)
        FarmStatsTracker.recordMatch(MatchResultKind.DEFEAT, -3)
        FarmStatsTracker.recordMatch(MatchResultKind.DRAW, 0)
        var s = FarmStatsTracker.snapshot(1_000L)
        assertEquals(1, s.wins)
        assertEquals(1, s.losses)
        assertEquals(5, s.earnedTrophies)
        assertEquals(50.0, s.winRatePercent, 0.0001)
        FarmStatsTracker.recordMatch(MatchResultKind.DEFEAT, -10)
        s = FarmStatsTracker.snapshot(1_000L)
        assertEquals(0, s.earnedTrophies)
        assertEquals(100.0 / 3.0, s.winRatePercent, 0.0001)
    }

    @Test fun resetsWhenStartingNewSession() {
        FarmStatsTracker.start(100L)
        FarmStatsTracker.recordMatch(MatchResultKind.VICTORY, 12)
        FarmStatsTracker.start(500L)
        val s = FarmStatsTracker.snapshot(500L)
        assertEquals(0, s.wins)
        assertEquals(0, s.losses)
        assertEquals(0, s.earnedTrophies)
        assertEquals(0L, s.activeMillis)
    }
}
