package com.geedkajannada.data

import androidx.annotation.StringRes
import com.geedkajannada.R

enum class AdhkarPeriod(val key: String) { SUBAX("subax"), MASAA("masaa") }

/** Meesha qoraalka ka yimaado: aayado Quraan ah (dataset-ka offline) ama qoraal dikhri gaaban. */
sealed interface AdhkarSource {
    data class QuranRange(val surah: Int, val from: Int, val to: Int) : AdhkarSource
    data class Text(val arabic: String) : AdhkarSource
}

data class AdhkarItem(
    val id: String,
    @StringRes val titleRes: Int,
    val count: Int,
    val periods: Set<AdhkarPeriod>,
    val source: AdhkarSource,
)

object AdhkarCatalog {
    private val both = setOf(AdhkarPeriod.SUBAX, AdhkarPeriod.MASAA)

    val items: List<AdhkarItem> = listOf(
        AdhkarItem("kursi", R.string.adhkar_kursi, 1, both, AdhkarSource.QuranRange(2, 255, 255)),
        AdhkarItem("ikhlaas", R.string.adhkar_ikhlaas, 3, both, AdhkarSource.QuranRange(112, 1, 4)),
        AdhkarItem("falaq", R.string.adhkar_falaq, 3, both, AdhkarSource.QuranRange(113, 1, 5)),
        AdhkarItem("naas", R.string.adhkar_naas, 3, both, AdhkarSource.QuranRange(114, 1, 6)),
        AdhkarItem(
            "baqara_end", R.string.adhkar_baqara_end, 1, setOf(AdhkarPeriod.MASAA),
            AdhkarSource.QuranRange(2, 285, 286),
        ),
        AdhkarItem("tasbih", R.string.adhkar_tasbih, 100, both, AdhkarSource.Text("سُبْحَانَ ٱللَّهِ وَبِحَمْدِهِ")),
        AdhkarItem("istighfar", R.string.adhkar_istighfar, 100, both, AdhkarSource.Text("أَسْتَغْفِرُ ٱللَّهَ")),
    )

    fun forPeriod(p: AdhkarPeriod): List<AdhkarItem> = items.filter { p in it.periods }

    /** Subax ilaa 15:00, kadib Masaa. */
    fun periodAt(hourOfDay: Int): AdhkarPeriod = if (hourOfDay < 15) AdhkarPeriod.SUBAX else AdhkarPeriod.MASAA
}
