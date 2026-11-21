package com.pyla.ai.engine

/** Statistics for the current farming run, not lifetime/account totals. */
data class FarmStatsSnapshot(
    val activeMillis: Long = 0L,
    val wins: Int = 0,
    val losses: Int = 0,
    val earnedTrophies: Int = 0,
) {
    val winRatePercent: Double
        get() {
            val decided = wins.toLong() + losses.toLong()
            return if (decided == 0L) 0.0 else wins.toDouble() * 100.0 / decided
        }
}

/** Cross-thread session statistics, measured using Android's elapsed realtime clock. */
object FarmStatsTracker {
    private var started = false
    private var running = false
    private var activeStartMs: Long? = null
    private var accumulatedMs = 0L
    private var wins = 0
    private var losses = 0
    private var earnedTrophies = 0

    @Synchronized fun start(nowMs: Long) {
        started = true
        running = true
        activeStartMs = nowMs
        accumulatedMs = 0L
        wins = 0
        losses = 0
        earnedTrophies = 0
    }

    @Synchronized fun setPaused(paused: Boolean, nowMs: Long) {
        if (!started || !running) return
        if (paused) settleActiveTime(nowMs)
        else if (activeStartMs == null) activeStartMs = nowMs
    }

    @Synchronized fun stop(nowMs: Long) {
        if (!started || !running) return
        settleActiveTime(nowMs)
        running = false
    }

    @Synchronized fun recordMatch(result: MatchResultKind, actualTrophyDelta: Int) {
        if (!started || !running) return
        when (result) {
            MatchResultKind.VICTORY -> if (wins < Int.MAX_VALUE) wins++
            MatchResultKind.DEFEAT -> if (losses < Int.MAX_VALUE) losses++
            MatchResultKind.DRAW -> Unit
        }
        earnedTrophies = (earnedTrophies.toLong() + actualTrophyDelta.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }

    /** Only signed trophy changes actually read from the result screen enter the HUD. */
    @Synchronized fun addConfirmedTrophyDelta(delta: Int) {
        if (!started || !running) return
        earnedTrophies = (earnedTrophies.toLong() + delta.toLong())
            .coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    }

    @Synchronized fun snapshot(nowMs: Long): FarmStatsSnapshot {
        val activeDelta = activeStartMs?.let { (nowMs - it).coerceAtLeast(0L) } ?: 0L
        return FarmStatsSnapshot(
            activeMillis = accumulatedMs + activeDelta,
            wins = wins,
            losses = losses,
            earnedTrophies = earnedTrophies,
        )
    }

    private fun settleActiveTime(nowMs: Long) {
        val start = activeStartMs ?: return
        accumulatedMs += (nowMs - start).coerceAtLeast(0L)
        activeStartMs = null
    }
}
