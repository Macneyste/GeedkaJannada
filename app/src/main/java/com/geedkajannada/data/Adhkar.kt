package com.geedkajannada.data

import androidx.annotation.StringRes
import com.geedkajannada.R

enum class AdhkarPeriod(val key: String, @StringRes val labelRes: Int) {
    SUBAX("subax", R.string.adhkar_subax),
    MASAA("masaa", R.string.adhkar_masaa),
    SALAAD("salaad", R.string.adhkar_after_prayer),
    HURDO("hurdo", R.string.adhkar_sleep),
    MAALINTA("maalinta", R.string.adhkar_daily),
}

/** Meesha qoraalka ka yimaado: aayado Quraan ah (dataset-ka offline) ama qoraal dikhri gaaban. */
sealed interface AdhkarSource {
    data class QuranRange(val surah: Int, val from: Int, val to: Int) : AdhkarSource
    data class Text(val arabic: String) : AdhkarSource
}

data class AdhkarItem(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descRes: Int,
    val count: Int,
    val period: AdhkarPeriod,
    val source: AdhkarSource,
)

object AdhkarCatalog {
    private fun q(s: Int, a: Int, b: Int = a) = AdhkarSource.QuranRange(s, a, b)
    private fun t(s: String) = AdhkarSource.Text(s)

    private const val ISTIGHFAR = "أَسْتَغْفِرُ ٱللَّهَ"
    private const val TASBIH = "سُبْحَانَ ٱللَّهِ"
    private const val TAHMID = "ٱلْحَمْدُ لِلَّهِ"
    private const val TAKBIR = "ٱللَّهُ أَكْبَرُ"
    private const val TAHLIL =
        "لَا إِلَٰهَ إِلَّا ٱللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ ٱلْمُلْكُ وَلَهُ ٱلْحَمْدُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ"

    private val S = AdhkarPeriod.SUBAX
    private val M = AdhkarPeriod.MASAA
    private val P = AdhkarPeriod.SALAAD
    private val H = AdhkarPeriod.HURDO
    private val D = AdhkarPeriod.MAALINTA

    private fun morningEvening(): List<AdhkarItem> = listOf(S, M).flatMap { p ->
        val k = p.key
        listOf(
            AdhkarItem("kursi_$k", R.string.adhkar_kursi, R.string.desc_kursi, 1, p, q(2, 255)),
            AdhkarItem("ikhlaas_$k", R.string.adhkar_ikhlaas, R.string.desc_protect, 3, p, q(112, 1, 4)),
            AdhkarItem("falaq_$k", R.string.adhkar_falaq, R.string.desc_protect, 3, p, q(113, 1, 5)),
            AdhkarItem("naas_$k", R.string.adhkar_naas, R.string.desc_protect, 3, p, q(114, 1, 6)),
            AdhkarItem(
                "noharm_$k", R.string.adhkar_noharm, R.string.desc_noharm, 3, p,
                t("بِسْمِ ٱللَّهِ ٱلَّذِي لَا يَضُرُّ مَعَ ٱسْمِهِ شَيْءٌ فِي ٱلْأَرْضِ وَلَا فِي ٱلسَّمَاءِ وَهُوَ ٱلسَّمِيعُ ٱلْعَلِيمُ"),
            ),
            AdhkarItem(
                "radiitu_$k", R.string.adhkar_radiitu, R.string.desc_radiitu, 3, p,
                t("رَضِيتُ بِٱللَّهِ رَبًّا، وَبِٱلْإِسْلَامِ دِينًا، وَبِمُحَمَّدٍ نَبِيًّا"),
            ),
            AdhkarItem("tasbih_$k", R.string.adhkar_tasbih, R.string.desc_bihamdihi, 100, p, t("سُبْحَانَ ٱللَّهِ وَبِحَمْدِهِ")),
            AdhkarItem("istighfar_$k", R.string.adhkar_istighfar, R.string.desc_istighfar, 100, p, t(ISTIGHFAR)),
        ) + if (p == M) listOf(
            AdhkarItem("baqara_end_$k", R.string.adhkar_baqara_end, R.string.desc_baqara_end, 1, p, q(2, 285, 286)),
        ) else emptyList()
    }

    private val afterPrayer = listOf(
        AdhkarItem("p_istighfar", R.string.adhkar_istighfar, R.string.desc_istighfar, 3, P, t(ISTIGHFAR)),
        AdhkarItem(
            "p_salaam", R.string.adhkar_salaam, R.string.desc_salaam, 1, P,
            t("ٱللَّهُمَّ أَنْتَ ٱلسَّلَامُ، وَمِنْكَ ٱلسَّلَامُ، تَبَارَكْتَ يَا ذَا ٱلْجَلَالِ وَٱلْإِكْرَامِ"),
        ),
        AdhkarItem("p_tasbih", R.string.adhkar_subxaanallaah, R.string.desc_tasbih, 33, P, t(TASBIH)),
        AdhkarItem("p_tahmid", R.string.adhkar_alxamdulillaah, R.string.desc_tahmid, 33, P, t(TAHMID)),
        AdhkarItem("p_takbir", R.string.adhkar_allaahu_akbar, R.string.desc_takbir, 33, P, t(TAKBIR)),
        AdhkarItem("p_tahlil", R.string.adhkar_tahlil, R.string.desc_tahlil, 1, P, t(TAHLIL)),
        AdhkarItem("p_kursi", R.string.adhkar_kursi, R.string.desc_kursi, 1, P, q(2, 255)),
        AdhkarItem("p_ikhlaas", R.string.adhkar_ikhlaas, R.string.desc_protect, 1, P, q(112, 1, 4)),
        AdhkarItem("p_falaq", R.string.adhkar_falaq, R.string.desc_protect, 1, P, q(113, 1, 5)),
        AdhkarItem("p_naas", R.string.adhkar_naas, R.string.desc_protect, 1, P, q(114, 1, 6)),
    )

    private val sleep = listOf(
        AdhkarItem("h_kursi", R.string.adhkar_kursi, R.string.desc_kursi, 1, H, q(2, 255)),
        AdhkarItem("h_baqara_end", R.string.adhkar_baqara_end, R.string.desc_baqara_end, 1, H, q(2, 285, 286)),
        AdhkarItem("h_ikhlaas", R.string.adhkar_ikhlaas, R.string.desc_protect, 3, H, q(112, 1, 4)),
        AdhkarItem("h_falaq", R.string.adhkar_falaq, R.string.desc_protect, 3, H, q(113, 1, 5)),
        AdhkarItem("h_naas", R.string.adhkar_naas, R.string.desc_protect, 3, H, q(114, 1, 6)),
        AdhkarItem("h_tasbih", R.string.adhkar_subxaanallaah, R.string.desc_tasbih, 33, H, t(TASBIH)),
        AdhkarItem("h_tahmid", R.string.adhkar_alxamdulillaah, R.string.desc_tahmid, 33, H, t(TAHMID)),
        AdhkarItem("h_takbir", R.string.adhkar_allaahu_akbar, R.string.desc_takbir, 34, H, t(TAKBIR)),
        AdhkarItem("h_bismika", R.string.adhkar_bismika, R.string.desc_bismika, 1, H, t("بِٱسْمِكَ ٱللَّهُمَّ أَمُوتُ وَأَحْيَا")),
    )

    private val daily = listOf(
        AdhkarItem(
            "d_wake", R.string.adhkar_wake, R.string.desc_wake, 1, D,
            t("ٱلْحَمْدُ لِلَّهِ ٱلَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ ٱلنُّشُورُ"),
        ),
        AdhkarItem("d_eat_before", R.string.adhkar_eat_before, R.string.desc_eat_before, 1, D, t("بِسْمِ ٱللَّهِ")),
        AdhkarItem(
            "d_eat_after", R.string.adhkar_eat_after, R.string.desc_eat_after, 1, D,
            t("ٱلْحَمْدُ لِلَّهِ ٱلَّذِي أَطْعَمَنِي هَٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ"),
        ),
        AdhkarItem("d_hawqala", R.string.adhkar_hawqala, R.string.desc_hawqala, 10, D, t("لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِٱللَّهِ")),
        AdhkarItem(
            "d_salawat", R.string.adhkar_salawat, R.string.desc_salawat, 10, D,
            t("ٱللَّهُمَّ صَلِّ وَسَلِّمْ عَلَىٰ نَبِيِّنَا مُحَمَّدٍ"),
        ),
        AdhkarItem("d_rabbana", R.string.adhkar_rabbana, R.string.desc_rabbana, 1, D, q(2, 201)),
    )

    val items: List<AdhkarItem> = morningEvening() + afterPrayer + sleep + daily

    fun forPeriod(p: AdhkarPeriod): List<AdhkarItem> = items.filter { it.period == p }

    /** Subax ilaa 15:00, kadib Masaa. */
    fun periodAt(hourOfDay: Int): AdhkarPeriod = if (hourOfDay < 15) AdhkarPeriod.SUBAX else AdhkarPeriod.MASAA
}
