package com.geedkajannada.data

import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

/** Magaalo la dooran karo; goobta waa go'an (lama isticmaalo location device-ka). */
data class City(val id: String, val nameSo: String, val lat: Double, val lng: Double)

object Cities {
    val all: List<City> = listOf(
        City("muqdisho", "Muqdisho", 2.0469, 45.3182),
        City("hargeysa", "Hargeysa", 9.5600, 44.0650),
        City("boosaaso", "Boosaaso", 11.2842, 49.1816),
        City("kismaayo", "Kismaayo", -0.3582, 42.5454),
        City("baydhabo", "Baydhabo", 3.1138, 43.6469),
        City("garoowe", "Garoowe", 8.4054, 48.4845),
        City("gaalkacyo", "Gaalkacyo", 6.7697, 47.4308),
        City("berbera", "Berbera", 10.4396, 45.0143),
        City("beledweyne", "Beledweyne", 4.7358, 45.2036),
        City("jowhar", "Jowhar", 2.7809, 45.5005),
        City("laascaanood", "Laascaanood", 8.4778, 47.3589),
        City("burco", "Burco", 9.5221, 45.5336),
        City("boorama", "Boorama", 9.9361, 43.1828),
        City("marka", "Marka", 1.7167, 44.7706),
        City("jabuuti", "Jabuuti", 11.5721, 43.1456),
    )
    val default: City = all.first()
    fun byId(id: String?): City = all.firstOrNull { it.id == id } ?: default
}

/**
 * Xisaabinta waqtiyada salaadda ee astronomical (offline).
 * Habka: Muslim World League (Fajr 18°, Isha 17°), Asr shadow = 1 (Shaafici), Maghrib marka qorraxdu dhacdo (0.833°).
 * Wixii ka duwan waqtiyada masaajidka maxalliga ah waa ay yihiin xisaab; isticmaaluhu wuu gacanta u beddeli karaa.
 */
object PrayerTimesCalc {
    /** Saacadda qalabka (Muqdisho = UTC+3). Waqtiyada waa waqtiga maxalliga ah ee qalabka. */
    val ZONE: ZoneId get() = ZoneId.systemDefault()

    private fun rad(d: Double) = Math.toRadians(d)
    private fun deg(r: Double) = Math.toDegrees(r)
    private fun fix(h: Double): Double = ((h % 24) + 24) % 24

    /** Waxay soo celisaa daqiiqooyinka min habeenbadh ee salaad kasta. */
    fun compute(date: LocalDate, city: City, utcOffsetHours: Double = 3.0): Map<Prayer, Int> {
        val jd = date.toEpochDay() + 2440588.0 // 12:00 UT
        val d = jd - 2451545.0
        val g = rad(357.529 + 0.98560028 * d)
        val q = 280.459 + 0.98564736 * d
        val l = rad(q + 1.915 * sin(g) + 0.020 * sin(2 * g))
        val e = rad(23.439 - 0.00000036 * d)
        val ra = fix(deg(atan2(cos(e) * sin(l), cos(l))) / 15.0)
        val decl = asin(sin(e) * sin(l))
        val eqt = q / 15.0 - ra
        val dhuhr = 12 + utcOffsetHours - city.lng / 15.0 - normalizeEqt(eqt)
        val lat = rad(city.lat)

        fun t(angleDeg: Double): Double {
            val x = (-sin(rad(angleDeg)) - sin(decl) * sin(lat)) / (cos(decl) * cos(lat))
            return deg(acos(x.coerceIn(-1.0, 1.0))) / 15.0
        }

        val asrAngle = -deg(atan(1.0 / (1.0 + tan(abs(lat - decl)))))

        fun minutes(h: Double) = (fix(h) * 60).roundToInt() % (24 * 60)
        return mapOf(
            Prayer.SUBAX to minutes(dhuhr - t(18.0)),
            Prayer.DUHUR to minutes(dhuhr),
            Prayer.CASAR to minutes(dhuhr + t(asrAngle)),
            Prayer.MAGHRIB to minutes(dhuhr + t(0.833)),
            Prayer.CISHE to minutes(dhuhr + t(17.0)),
        )
    }

    /** q/15 - RA waa u dhow eber (hour); hubi inay ku dhacdo [-12, 12). */
    private fun normalizeEqt(x: Double): Double {
        var v = x % 24
        if (v >= 12) v -= 24
        if (v < -12) v += 24
        return v
    }
}
