package com.geedkajannada

import com.geedkajannada.data.QuranParser
import com.geedkajannada.feature.dua.DuaParser
import com.geedkajannada.feature.dua.DuaSearch
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DuaTest {
    private val library = DuaParser.parse(File("src/main/assets/dua.json").readText())
    private val quran = QuranParser.parse(File("src/main/assets/quran.json").readText())

    @Test fun everyDuaPointsAtARealVerseRangeAndCategory() {
        val categories = library.categories.map { it.id }.toSet()
        for (d in library.duas) {
            val surah = quran.first { it.surah.id == d.surah }
            assertTrue(d.id, d.from >= 1 && d.to >= d.from && d.to <= surah.verses.size)
            assertTrue(d.id, d.categoryId in categories)
            assertTrue(d.id, d.repeat >= 1)
        }
    }

    @Test fun idsAreUniqueAndNoCategoryIsEmpty() {
        assertEquals(library.duas.size, library.duas.map { it.id }.toSet().size)
        for (c in library.categories) assertTrue(c.id, library.duas.any { it.categoryId == c.id })
    }

    @Test fun translationsStayFlaggedUntilReviewed() {
        assertTrue(library.duas.all { it.needsReview })
    }

    @Test fun searchIgnoresHarakatAndMatchesSomaliAndEnglish() {
        val withText = library.duas.map { d ->
            d.copy(arabic = quran.first { it.surah.id == d.surah }.verses.subList(d.from - 1, d.to).joinToString(" "))
        }
        assertTrue(DuaSearch.filter(withText, "احد").any { it.id == "q112_1" })
        assertTrue(DuaSearch.filter(withText, "cilmi").any { it.id == "q20_114" })
        assertTrue(DuaSearch.filter(withText, "knowledge").any { it.id == "q20_114" })
        assertEquals(listOf("q2_201"), DuaSearch.filter(withText, "2:201").map { it.id })
        assertTrue(DuaSearch.filter(withText, "zzzz").isEmpty())
    }
}
