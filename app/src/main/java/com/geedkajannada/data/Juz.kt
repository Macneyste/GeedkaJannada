package com.geedkajannada.data

/** Bilowga juz kasta: (suurad, aayad). Jadwal caadi ah oo 30 juz ah. */
object Juz {
    val starts: List<Pair<Int, Int>> = listOf(
        1 to 1, 2 to 142, 2 to 253, 3 to 93, 4 to 24, 4 to 148, 5 to 82, 6 to 111, 7 to 88, 8 to 41,
        9 to 93, 11 to 6, 12 to 53, 15 to 1, 17 to 1, 18 to 75, 21 to 1, 23 to 1, 25 to 21, 27 to 56,
        29 to 46, 33 to 31, 36 to 28, 39 to 32, 41 to 47, 46 to 1, 51 to 31, 58 to 1, 67 to 1, 78 to 1,
    )

    const val COUNT = 30

    /** Juz-ka aayaddan ku jirto (1..30). */
    fun of(surah: Int, verse: Int): Int {
        var juz = 1
        for ((i, s) in starts.withIndex()) {
            if (surah > s.first || (surah == s.first && verse >= s.second)) juz = i + 1
        }
        return juz
    }
}
