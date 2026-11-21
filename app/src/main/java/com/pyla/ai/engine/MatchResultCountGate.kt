package com.pyla.ai.engine

/**
 * One result and one confirmed trophy delta per match. Only a new positive
 * match/matchmaking screen arms the next round; UI transitions never do.
 */
class MatchResultCountGate {
    private var resultCounted = false
    private var trophyCounted = false

    @Synchronized fun observeState(state: String?) {
        if (state == "match" || state == "match_start" || state == "match_making") {
            resultCounted = false
            trophyCounted = false
        }
    }

    @Synchronized fun hasResult(): Boolean = resultCounted
    @Synchronized fun hasTrophies(): Boolean = trophyCounted

    @Synchronized fun claimResult(): Boolean {
        if (resultCounted) return false
        resultCounted = true
        return true
    }

    @Synchronized fun claimTrophies(): Boolean {
        if (trophyCounted) return false
        trophyCounted = true
        return true
    }
}
