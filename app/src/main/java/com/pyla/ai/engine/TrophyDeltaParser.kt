package com.pyla.ai.engine

/** Fail-closed trophy delta parser: requires a visible explicit +/- sign. */
object TrophyDeltaParser {
    private val regex = Regex("""(?<![0-9])([+＋\-−–])\s*([0-9]{1,3})(?![0-9])""")

    fun parse(text: String): Int? {
        val values = regex.findAll(text).mapNotNull { match ->
            val amount = match.groupValues[2].toIntOrNull() ?: return@mapNotNull null
            if (amount > 200) return@mapNotNull null
            val sign = match.groupValues[1]
            if (sign == "+" || sign == "＋") amount else -amount
        }.distinct().toList()
        return values.singleOrNull()
    }
}
