package com.geedkajannada.data

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

/** Taariikhda Hijri (xisaab Umm al-Qura, offline). */
data class Hijri(val year: Int, val month: Int, val day: Int) {
    companion object {
        fun of(date: LocalDate): Hijri {
            val h = HijrahDate.from(date)
            return Hijri(h.get(ChronoField.YEAR), h.get(ChronoField.MONTH_OF_YEAR), h.get(ChronoField.DAY_OF_MONTH))
        }
    }
}

val HijriMonthNames: List<Int> = listOf(
    com.geedkajannada.R.string.hijri_m1, com.geedkajannada.R.string.hijri_m2, com.geedkajannada.R.string.hijri_m3,
    com.geedkajannada.R.string.hijri_m4, com.geedkajannada.R.string.hijri_m5, com.geedkajannada.R.string.hijri_m6,
    com.geedkajannada.R.string.hijri_m7, com.geedkajannada.R.string.hijri_m8, com.geedkajannada.R.string.hijri_m9,
    com.geedkajannada.R.string.hijri_m10, com.geedkajannada.R.string.hijri_m11, com.geedkajannada.R.string.hijri_m12,
)
