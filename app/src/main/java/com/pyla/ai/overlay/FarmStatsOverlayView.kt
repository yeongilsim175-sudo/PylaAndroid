package com.pyla.ai.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.pyla.ai.engine.FarmStatsSnapshot
import java.util.Locale
import kotlin.math.roundToInt

/** Translucent-safe, non-interactive HUD with text only, no card or icons. */
internal class FarmStatsOverlayView(context: Context) : View(context) {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        style = Paint.Style.FILL
    }
    private val outline = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
    }
    private var pad = 0f
    private var lineStep = 0f
    private var lines = listOf(
        "파밍 시간: 00:00:00", "승리: 0", "패배: 0", "획득 트로피: 0", "승률: 0.0%",
    )

    init {
        setWillNotDraw(false)
        reconfigure()
    }

    fun reconfigure() {
        val d = resources.displayMetrics.density
        val sp = resources.displayMetrics.scaledDensity
        pad = 5f * d
        fill.textSize = 14f * sp
        outline.textSize = fill.textSize
        outline.strokeWidth = 2.7f * d
        lineStep = 21f * sp
        invalidate()
    }

    fun updateStats(stats: FarmStatsSnapshot, maxWidth: Int): Pair<Int, Int> {
        val seconds = stats.activeMillis.coerceAtLeast(0L) / 1000L
        val h = seconds / 3600L
        val m = (seconds / 60L) % 60L
        val s = seconds % 60L
        lines = listOf(
            "파밍 시간: ${String.format(Locale.US, "%02d:%02d:%02d", h, m, s)}",
            "승리: ${stats.wins}",
            "패배: ${stats.losses}",
            "획득 트로피: ${stats.earnedTrophies}",
            "승률: ${String.format(Locale.US, "%.1f", stats.winRatePercent)}%",
        )
        val sp = resources.displayMetrics.scaledDensity
        fill.textSize = 14f * sp
        val maxTextWidth = (maxWidth - 2f * pad).coerceAtLeast(1f)
        val measured = lines.maxOf { fill.measureText(it) }.coerceAtLeast(1f)
        if (measured > maxTextWidth) fill.textSize *= maxTextWidth / measured
        outline.textSize = fill.textSize
        lineStep = fill.textSize * 1.45f
        val needed = (lines.maxOf { fill.measureText(it) } + 2f * pad).roundToInt()
        val widthPx = needed.coerceIn(1, maxWidth.coerceAtLeast(1))
        val heightPx = (2f * pad + 5f * lineStep).roundToInt().coerceAtLeast(1)
        contentDescription = lines.joinToString(", ")
        invalidate()
        return widthPx to heightPx
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val base = pad - fill.fontMetrics.ascent
        lines.forEachIndexed { index, line ->
            val y = base + index * lineStep
            canvas.drawText(line, pad, y, outline)
            canvas.drawText(line, pad, y, fill)
        }
    }
}
