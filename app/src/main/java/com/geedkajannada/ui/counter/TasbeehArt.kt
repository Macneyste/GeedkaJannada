package com.geedkajannada.ui.counter

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.White

private const val STRING_BEADS = 15

/** Mawqifka xabbad t (0..1) ee xadhigga qalooca ah. */
private fun curve(t: Float, w: Float, h: Float) = Offset(
    x = w * (0.04f + 0.92f * t),
    y = h * 0.08f + h * 0.30f * 4f * t * (1f - t),
)

/**
 * Tusbax qaloocan iyo giraangir leh fallaar qori ah. Taabasho kasta xabbad ayaa dhaqaaqda (0.3s ease-out)
 * giraangirtuna wax yar ayay wareegtaa.
 */
@Composable
fun BeadsAndWheel(steps: Int, color: Color, modifier: Modifier = Modifier) {
    val shift by animateFloatAsState(steps.toFloat(), tween(300, easing = FastOutSlowInEasing), label = "beadShift")
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val string = Path().apply {
            moveTo(curve(0f, w, h).x, curve(0f, w, h).y)
            for (i in 1..40) {
                val p = curve(i / 40f, w, h)
                lineTo(p.x, p.y)
            }
        }
        drawPath(string, BrownMid, style = Stroke(3f))
        val r = w / (STRING_BEADS * 3f)
        val phase = (shift % 1f)
        for (i in 0 until STRING_BEADS) {
            val p = curve((i + phase) / STRING_BEADS, w, h)
            drawCircle(color, r, p)
            drawCircle(White.copy(alpha = 0.45f), r * 0.35f, Offset(p.x - r * 0.3f, p.y - r * 0.3f))
        }

        val center = Offset(w / 2f, h * 0.72f)
        val wheel = h * 0.25f
        drawCircle(White, wheel, center)
        drawCircle(BrownMid, wheel, center, style = Stroke(6f))
        rotate(shift * (360f / 33f), center) {
            for (i in 0 until 8) {
                rotate(i * 45f, center) {
                    drawLine(Line, center, Offset(center.x, center.y - wheel * 0.88f), 4f)
                }
            }
            // fallaar qori ah
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

/** Dhalo muraayad ah oo xabbaduhu ku dul tuuran yihiin (spring marka ay dhacaan). */
@Composable
fun JarArt(count: Int, goal: Int, color: Color, modifier: Modifier = Modifier) {
    // Ugu badnaan 33 xabbadood ayaa la sawiraa, xitaa haddii yoolku ka weyn yahay.
    val capacity = minOf(goal, 33).coerceAtLeast(1)
    val beads = if (goal <= 0) 0 else (minOf(count, goal) * capacity + goal - 1) / goal
    val drop by animateFloatAsState(
        beads.toFloat(),
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "jarDrop",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val neck = h * 0.12f
        // afka dhalada
        drawRoundRect(color, Offset(w * 0.22f, 0f), Size(w * 0.56f, neck * 0.7f), CornerRadius(8f))
        val body = Size(w * 0.92f, h - neck)
        val origin = Offset(w * 0.04f, neck)
        drawRoundRect(color.copy(alpha = 0.14f), origin, body, CornerRadius(w * 0.22f))
        drawRoundRect(color, origin, body, CornerRadius(w * 0.22f), style = Stroke(4f))

        val perRow = 5
        val d = body.width * 0.8f / perRow
        val rows = (capacity + perRow - 1) / perRow
        val rowH = minOf(d, body.height * 0.9f / rows)
        for (i in 0 until beads) {
            val col = i % perRow
            val row = i / perRow
            val restY = h - rowH * (row + 0.5f) - body.height * 0.04f
            // xabbadda ugu dambaysay ayaa ka soo dhacda afka
            val y = if (i == beads - 1) {
                val t = (drop - (beads - 1)).coerceIn(0f, 1.15f)
                neck + (restY - neck) * t
            } else restY
            val x = origin.x + body.width * 0.1f + d * (col + 0.5f)
            drawCircle(color, rowH * 0.45f, Offset(x, y))
            drawCircle(White.copy(alpha = 0.4f), rowH * 0.14f, Offset(x - rowH * 0.15f, y - rowH * 0.15f))
        }
        // iftiinka muraayadda
        drawLine(White.copy(alpha = 0.7f), Offset(w * 0.14f, neck + h * 0.12f), Offset(w * 0.14f, h * 0.7f), 5f)
    }
}
