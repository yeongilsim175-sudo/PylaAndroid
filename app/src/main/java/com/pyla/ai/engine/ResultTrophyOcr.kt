package com.pyla.ai.engine

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Rect
import java.util.concurrent.TimeUnit

/** OCR only on confirmed result screens; unsigned or ambiguous results are ignored. */
object ResultTrophyOcr {
    private const val TAG = "PylaResultOCR"

    fun readSignedDelta(screen: Mat): Int? {
        if (screen.empty() || screen.cols() < 80 || screen.rows() < 80) return null
        // Trophy is in the upper-left part of a result screen. Exclude the
        // player rankings and the rest of the arena to reduce spurious digits.
        val crop = Mat(screen, Rect(0, 0,
            (screen.cols() * 0.48).toInt().coerceAtLeast(1),
            (screen.rows() * 0.48).toInt().coerceAtLeast(1)))
        val bitmap = Bitmap.createBitmap(crop.cols(), crop.rows(), Bitmap.Config.ARGB_8888)
        try {
            Utils.matToBitmap(crop, bitmap)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                val recognized = Tasks.await(
                    recognizer.process(InputImage.fromBitmap(bitmap, 0)),
                    1500L, TimeUnit.MILLISECONDS,
                )
                val delta = TrophyDeltaParser.parse(recognized.text)
                Log.i(TAG, "signed delta OCR: ${if (delta != null) "confirmed" else "unavailable"}")
                return delta
            } finally {
                recognizer.close()
            }
        } catch (t: Throwable) {
            Log.w(TAG, "signed delta OCR unavailable: ${t.javaClass.simpleName}")
            return null
        } finally {
            bitmap.recycle()
            crop.release()
        }
    }
}
