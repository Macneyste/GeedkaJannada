package com.geedkajannada.feature.tasbeeh.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.geedkajannada.R
import com.geedkajannada.feature.tasbeeh.Jar
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.White

/** Saddexda dhalo: mid kasta midabkiisa, tiradiisa iyo calaamad marka uu buuxsamo. */
@Composable
fun JarRow(jars: List<Jar>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        jars.forEach { jar ->
            val color = Color(jar.dhikr.color)
            val name = stringResource(jar.dhikr.nameRes)
            val desc = stringResource(R.string.tas_jar_desc, name, jar.count, jar.goal)
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics(mergeDescendants = true) { contentDescription = desc },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                JarArt(jar.count, jar.goal, color, Modifier.fillMaxWidth().weight(1f))
                Box(Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                    if (jar.full) {
                        Icon(
                            painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
                Text(
                    if (jar.full) stringResource(R.string.tas_done) else name,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrownInk,
                    maxLines = 1,
                )
                Text("${jar.count} / ${jar.goal}", style = MaterialTheme.typography.labelLarge, color = BrownInk)
            }
        }
    }
}

/** Dhalo muraayad ah; xabbadda ugu dambaysay waxay ku dhacdaa spring bounce (damping 0.6). */
@Composable
private fun JarArt(count: Int, goal: Int, color: Color, modifier: Modifier = Modifier) {
    val drop by animateFloatAsState(
        count.toFloat(),
        spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "jarDrop",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val neck = h * 0.12f
        drawRoundRect(color, Offset(w * 0.22f, 0f), Size(w * 0.56f, neck * 0.7f), CornerRadius(8f))
        val body = Size(w * 0.92f, h - neck)
        val origin = Offset(w * 0.04f, neck)
        drawRoundRect(color.copy(alpha = 0.14f), origin, body, CornerRadius(w * 0.22f))
        drawRoundRect(color, origin, body, CornerRadius(w * 0.22f), style = Stroke(4f))

        val perRow = 5
        val d = body.width * 0.8f / perRow
        val rows = (goal + perRow - 1) / perRow
        val rowH = minOf(d, body.height * 0.9f / rows)
        for (i in 0 until count) {
            val restY = h - rowH * (i / perRow + 0.5f) - body.height * 0.04f
            val y = if (i == count - 1) {
                val t = (drop - (count - 1)).coerceIn(0f, 1.15f)
                neck + (restY - neck) * t
            } else restY
            val x = origin.x + body.width * 0.1f + d * (i % perRow + 0.5f)
            drawCircle(color, rowH * 0.45f, Offset(x, y))
            drawCircle(White.copy(alpha = 0.4f), rowH * 0.14f, Offset(x - rowH * 0.15f, y - rowH * 0.15f))
        }
        drawLine(White.copy(alpha = 0.7f), Offset(w * 0.14f, neck + h * 0.12f), Offset(w * 0.14f, h * 0.7f), 5f)
    }
}
