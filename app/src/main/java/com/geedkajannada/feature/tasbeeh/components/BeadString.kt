package com.geedkajannada.feature.tasbeeh.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.White

private const val STRING_BEADS = 15
private const val WHEEL_DEGREES_PER_TAP = 10f

/** Mawqifka xabbad t (0..1) ee xadhigga qalooca ah. */
private fun curve(t: Float, w: Float, h: Float) = Offset(
    x = w * (0.04f + 0.92f * t),
    y = h * 0.08f + h * 0.30f * 4f * t * (1f - t),
)

/** Xariiqda biilalka ee qalooca ah iyo giraangirta leh pointer. Taabasho kasta: 300ms ease-out, 10°. */
@Composable
fun BeadString(steps: Int, color: Color, modifier: Modifier = Modifier) {
    val shift by animateFloatAsState(steps.toFloat(), tween(300, easing = LinearOutSlowInEasing), label = "beadShift")
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val string = Path().apply {
            val start = curve(0f, w, h)
            moveTo(start.x, start.y)
            for (i in 1..40) {
                val p = curve(i / 40f, w, h)
                lineTo(p.x, p.y)
            }
        }
        drawPath(string, BrownMid, style = Stroke(3f))
        val r = w / (STRING_BEADS * 3f)
        val phase = shift % 1f
        for (i in 0 until STRING_BEADS) {
            val p = curve((i + phase) / STRING_BEADS, w, h)
            drawCircle(color, r, p)
            drawCircle(White.copy(alpha = 0.45f), r * 0.35f, Offset(p.x - r * 0.3f, p.y - r * 0.3f))
        }

        val center = Offset(w / 2f, h * 0.72f)
        val wheel = h * 0.25f
        drawCircle(White, wheel, center)
        drawCircle(BrownMid, wheel, center, style = Stroke(6f))
        rotate(shift * WHEEL_DEGREES_PER_TAP, center) {
            for (i in 0 until 8) {
                rotate(i * 45f, center) {
                    drawLine(Line, center, Offset(center.x, center.y - wheel * 0.88f), 4f)
                }
            }
            drawLine(BrownInk, Offset(center.x, center.y + wheel * 0.3f), Offset(center.x, center.y - wheel * 0.68f), 8f)
            val tip = Path().apply {
                moveTo(center.x, center.y - wheel * 0.92f)
                lineTo(center.x - wheel * 0.16f, center.y - wheel * 0.62f)
                lineTo(center.x + wheel * 0.16f, center.y - wheel * 0.62f)
                close()
            }
            drawPath(tip, BrownInk)
        }
        drawCircle(color, wheel * 0.14f, center)
    }
}
