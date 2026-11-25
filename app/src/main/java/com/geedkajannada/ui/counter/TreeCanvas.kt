package com.geedkajannada.ui.counter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Sand
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.IndigoLight
import com.geedkajannada.ui.theme.IndigoLight
import com.geedkajannada.ui.theme.CreamPanel
import kotlin.math.floor

/** Cabbirrada geedka ee 220 viewBox. */
object TreeGeometry {
    fun trunkHeight(r: Float) = 30f + 100f * r
    fun top(r: Float) = 200f - trunkHeight(r)
    fun crownScale(r: Float) = 0.45f + 0.45f * r
    fun visibleFruit(r: Float) = floor(r * 6f).toInt().coerceIn(0, 6)
}

private data class Blob(val dx: Float, val dy: Float, val radius: Float, val layer: Int)

private val blobs = listOf(
    Blob(0f, 0f, 34f, 0), Blob(-30f, 10f, 26f, 0), Blob(30f, 10f, 26f, 0),
    Blob(-14f, -24f, 26f, 1), Blob(18f, -22f, 24f, 1), Blob(0f, 22f, 24f, 1),
    Blob(-34f, -6f, 16f, 2), Blob(8f, -34f, 15f, 2), Blob(34f, -2f, 14f, 2), Blob(-6f, 8f, 14f, 2),
)

// Meelaha 6 timirood ee dhexda taajka (qaabka ku salaysan crown point).
private val fruitSpots = listOf(
    Offset(-22f, -8f), Offset(20f, -16f), Offset(2f, 14f),
    Offset(-6f, -30f), Offset(30f, 8f), Offset(-32f, 14f),
)

@Composable
fun TreeCanvas(ratio: Float, modifier: Modifier = Modifier) {
    val r by animateFloatAsState(ratio.coerceIn(0f, 1f), tween(500), label = "treeRatio")
    Canvas(modifier) {
        val s = minOf(size.width, size.height) / 220f
        val ox = (size.width - 220f * s) / 2f
        val oy = (size.height - 220f * s) / 2f
        fun p(x: Float, y: Float) = Offset(ox + x * s, oy + y * s)

        // Iftiin dahab ah (radial)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Sun.copy(alpha = 0.45f), Sun.copy(alpha = 0f)),
                center = p(110f, 100f),
                radius = 105f * s,
            ),
            radius = 105f * s,
            center = p(110f, 100f),
        )
        // Dhulka
        drawOval(CreamPanel, topLeft = p(40f, 194f), size = Size(140f * s, 24f * s))

        // Geedka
        val top = TreeGeometry.top(r)
        val trunk = Path().apply {
            moveTo(p(100f, 204f).x, p(100f, 204f).y)
            lineTo(p(120f, 204f).x, p(120f, 204f).y)
            lineTo(p(114f, top).x, p(114f, top).y)
            lineTo(p(106f, top).x, p(106f, top).y)
            close()
        }
        drawPath(trunk, BrownMid)
        val branchY = top + TreeGeometry.trunkHeight(r) * 0.45f
        drawLine(BrownMid, p(110f, branchY), p(110f - 24f * r - 6f, branchY - 22f - 10f * r), 5f * s, StrokeCap.Round)
        drawLine(BrownMid, p(110f, branchY + 8f), p(110f + 24f * r + 6f, branchY - 14f - 10f * r), 5f * s, StrokeCap.Round)

        // Taajka
        val k = TreeGeometry.crownScale(r)
        val cx = 110f
        val cy = top
        val layerColors = listOf<Color>(Indigo, IndigoLight, IndigoLight.copy(alpha = 0.85f))
        for (layer in 0..2) {
            for (b in blobs.filter { it.layer == layer }) {
                drawCircle(layerColors[layer], b.radius * k * s, p(cx + b.dx * k, cy + b.dy * k))
            }
        }
        // Timirta
        val shown = TreeGeometry.visibleFruit(r)
        for (i in 0 until shown) {
            val f = fruitSpots[i]
            val c = p(cx + f.x * k, cy + f.y * k)
            drawCircle(Sun, 6f * k * s + 1.5f * s, c)
            drawCircle(Sand.copy(alpha = 0.7f), 1.8f * s, Offset(c.x - 2f * s, c.y - 2f * s))
            drawCircle(BrownMid.copy(alpha = 0.35f), 6f * k * s + 1.5f * s, c, style = Stroke(width = 0.8f * s))
        }
    }
}
