package com.geedkajannada.data

import androidx.annotation.StringRes
import com.geedkajannada.R

data class Dhikr(
    val id: String,
    @StringRes val nameRes: Int,
    val arabic: String,
    val defaultGoal: Int,
)

object Dhikrs {
    val all: List<Dhikr> = listOf(
        Dhikr("subxaanallaah", R.string.dhikr_subxaanallaah, "سُبْحَانَ ٱللَّهِ", 33),
        Dhikr("alxamdulillaah", R.string.dhikr_alxamdulillaah, "ٱلْحَمْدُ لِلَّهِ", 33),
        Dhikr("allaahu_akbar", R.string.dhikr_allaahu_akbar, "ٱللَّهُ أَكْبَرُ", 33),
    )

    fun byId(id: String?): Dhikr = all.firstOrNull { it.id == id } ?: all.first()
}
