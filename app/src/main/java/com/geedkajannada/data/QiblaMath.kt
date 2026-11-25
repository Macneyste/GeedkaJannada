package com.geedkajannada.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Xisaabta jihada Qibla (bearing weyn oo goobaha u dhexeeya Kacbada). Offline, location ma dirto. */
object QiblaMath {
    const val KAABA_LAT = 21.4225
    const val KAABA_LON = 39.8262

    /** Jihada Qibla ee darajooyin ka bilaabma woqooyiga (0..360). */
    fun bearing(lat: Double, lon: Double): Double {
        val p1 = Math.toRadians(lat)
        val p2 = Math.toRadians(KAABA_LAT)
        val dl = Math.toRadians(KAABA_LON - lon)
        val y = sin(dl) * cos(p2)
        val x = cos(p1) * sin(p2) - sin(p1) * cos(p2) * cos(dl)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /** Farqiga u dhexeeya jihada telefoonka iyo Qibla (-180..180). 0 = si sax ah. */
    fun relative(qibla: Double, heading: Double): Double {
        var d = (qibla - heading) % 360.0
        if (d > 180) d -= 360
        if (d < -180) d += 360
        return d
    }
}
