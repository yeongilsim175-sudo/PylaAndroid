package com.pyla.ai.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class FarmStatsTrackerTest {
    @Test fun elapsedTimeExcludesPause() {
        FarmStatsTracker.start(1_000L)
        FarmStatsTracker.setPaused(true, 6_000L)
        assertEquals(5_000L, FarmStatsTracker.snapshot(21_000L).activeMillis)
        FarmStatsTracker.setPaused(false, 25_000L)
        FarmStatsTracker.stop(30_000L)
        assertEquals(10_000L, FarmStatsTracker.snapshot(80_000L).activeMillis)
    }

    @Test fun winRateAndTrophiesClampAtZero() {
        FarmStatsTracker.start(0L)
        FarmStatsTracker.recordMatch(MatchResultKind.VICTORY, 8)
        FarmStatsTracker.recordMatch(MatchResultKind.DEFEAT, -3)
        FarmStatsTracker.recordMatch(MatchResultKind.DRAW, 0)
        var stats = FarmStatsTracker.snapshot(1_000L)
        assertEquals(1, stats.wins)
        assertEquals(1, stats.losses)
        assertEquals(50.0, stats.winRatePercent, 0.001)
        assertEquals(5, stats.earnedTrophies)
        FarmStatsTracker.recordMatch(MatchResultKind.DEFEAT, -10)
        stats = FarmStatsTracker.snapshot(1_000L)
        assertEquals(0, stats.earnedTrophies)
        assertEquals(100.0 / 3.0, stats.winRatePercent, 0.001)
    }

    @Test fun newRunResetsCounters() {
        FarmStatsTracker.start(100L)
        FarmStatsTracker.recordMatch(MatchResultKind.VICTORY, 12)
        FarmStatsTracker.stop(200L)
        FarmStatsTracker.start(500L)
        val stats = FarmStatsTracker.snapshot(500L)
        assertEquals(0, stats.wins)
        assertEquals(0, stats.losses)
        assertEquals(0, stats.earnedTrophies)
        assertEquals(0L, stats.activeMillis)
    }
}
