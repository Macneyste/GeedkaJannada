package com.geedkajannada.ui.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.data.QiblaMath
import com.geedkajannada.ui.BackHeader
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Sun
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(onBack: () -> Unit, vm: QiblaViewModel = appViewModel { QiblaViewModel(it.prayers) }) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val sm = remember { ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    var heading by remember { mutableFloatStateOf(0f) }
    var hasReading by remember { mutableStateOf(false) }

    DisposableEffect(sensor) {
        val listener = object : SensorEventListener {
            private val rot = FloatArray(9)
            private val ori = FloatArray(3)
            override fun onSensorChanged(e: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rot, e.values)
                SensorManager.getOrientation(rot, ori)
                heading = ((Math.toDegrees(ori[0].toDouble()) + 360.0) % 360.0).toFloat()
                hasReading = true
            }
            override fun onAccuracyChanged(s: Sensor?, accuracy: Int) = Unit
        }
        if (sensor != null) sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    Column(Modifier.fillMaxSize()) {
        BackHeader(stringResource(R.string.qibla_title), onBack)
        Column(
            Modifier
                .fillMaxSize()
                .padding(Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing),
        ) {
            val bearing = state.bearing
            state.city?.let { Text(stringResource(R.string.qibla_city, it.nameSo), color = BrownMid) }
            Text(stringResource(R.string.qibla_bearing, bearing.roundToInt()), style = MaterialTheme.typography.titleLarge, color = Indigo)

            if (sensor == null) {
                Text(stringResource(R.string.qibla_no_sensor), textAlign = TextAlign.Center)
            } else {
                // Dial-ka wuxuu u rogmaa si woqooyigu u tilmaamo meesha dhabta ah; fallaadhu waa Qibla.
                val diff = QiblaMath.relative(bearing, heading.toDouble())
                val aligned = hasReading && abs(diff) < 5
                Canvas(Modifier.size(260.dp)) {
                    val c = center
                    val r = size.minDimension / 2
                    drawCircle(Line, r, c, style = Stroke(6f))
                    rotate(-heading, c) {
                        drawLine(BrownMid, c, Offset(c.x, c.y - r), 6f)
                    }
                    rotate((bearing - heading).toFloat(), c) {
                        val p = Path().apply {
                            moveTo(c.x, c.y - r * 0.85f)
                            lineTo(c.x - 22f, c.y)
                            lineTo(c.x + 22f, c.y)
                            close()
                        }
                        drawPath(p, if (aligned) Sun else Indigo)
                    }
                }
                Text(
                    when {
                        !hasReading -> stringResource(R.string.qibla_hint)
                        aligned -> stringResource(R.string.qibla_aligned)
                        diff > 0 -> stringResource(R.string.qibla_turn_right, abs(diff).roundToInt())
                        else -> stringResource(R.string.qibla_turn_left, abs(diff).roundToInt())
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(stringResource(R.string.qibla_hint), color = BrownMid, textAlign = TextAlign.Center)
            }
        }
    }
}
