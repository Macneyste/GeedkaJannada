package com.geedkajannada.data

import androidx.annotation.StringRes
import com.geedkajannada.R

/** Hal dhikri oo silsiladda Tasbeeh ah. `color` waa ARGB-ga dhalada. */
data class Dhikr(
    val id: String,
    @StringRes val nameRes: Int,
    val arabic: String,
    val defaultGoal: Int,
    @StringRes val englishRes: Int,
    @StringRes val meaningEnRes: Int,
    @StringRes val meaningSoRes: Int,
    val color: Long,
)

object Dhikrs {
    val all: List<Dhikr> = listOf(
        Dhikr(
            "subxaanallaah", R.string.dhikr_subxaanallaah, "سُبْحَانَ ٱللَّهِ", 33,
            R.string.tas_en_1, R.string.tas_mean_en_1, R.string.tas_mean_1, 0xFF4CAF7A,
        ),
        Dhikr(
            "alxamdulillaah", R.string.dhikr_alxamdulillaah, "ٱلْحَمْدُ لِلَّهِ", 33,
            R.string.tas_en_2, R.string.tas_mean_en_2, R.string.tas_mean_2, 0xFFD9546A,
        ),
        Dhikr(
            "allaahu_akbar", R.string.dhikr_allaahu_akbar, "ٱللَّهُ أَكْبَرُ", 33,
            R.string.tas_en_3, R.string.tas_mean_en_3, R.string.tas_mean_3, 0xFFE0A526,
        ),
    )

    fun byId(id: String?): Dhikr = all.firstOrNull { it.id == id } ?: all.first()
}
