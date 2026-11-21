package com.pyla.ai.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchResultCountGateTest {
    @Test fun onlyOneResultAndTrophyDeltaPerMatch() {
        val gate = MatchResultCountGate()
        assertTrue(gate.claimResult())
        assertFalse(gate.claimResult())
        assertTrue(gate.claimTrophies())
        assertFalse(gate.claimTrophies())
        gate.observeState("end_ranked")
        gate.observeState("lobby")
        gate.observeState("unknown")
        assertFalse(gate.claimResult())
        gate.observeState("match_making")
        assertTrue(gate.claimResult())
        assertTrue(gate.claimTrophies())
    }

    @Test fun parsesOnlyUnambiguousSignedTrophies() {
        assertEquals(8, TrophyDeltaParser.parse("Rank: 1\n+8"))
        assertEquals(-5, TrophyDeltaParser.parse("Trophies\n− 5"))
        assertEquals(10, TrophyDeltaParser.parse("＋10"))
        assertNull(TrophyDeltaParser.parse("Rank: 4\n1845"))
        assertNull(TrophyDeltaParser.parse("+8\n-5"))
        assertNull(TrophyDeltaParser.parse("+520"))
    }

    @Test fun confirmedTrophiesClampAtZero() {
        FarmStatsTracker.start(0L)
        FarmStatsTracker.recordMatch(MatchResultKind.VICTORY, 0)
        FarmStatsTracker.addConfirmedTrophyDelta(7)
        FarmStatsTracker.addConfirmedTrophyDelta(-100)
        assertEquals(0, FarmStatsTracker.snapshot(100L).earnedTrophies)
        assertEquals(1, FarmStatsTracker.snapshot(100L).wins)
        FarmStatsTracker.stop(200L)
    }
}
