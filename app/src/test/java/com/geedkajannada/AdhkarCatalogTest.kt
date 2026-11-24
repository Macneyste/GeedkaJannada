package com.geedkajannada

import com.geedkajannada.data.AdhkarCatalog
import com.geedkajannada.data.AdhkarPeriod
import com.geedkajannada.data.AdhkarSource
import com.geedkajannada.data.QuranParser
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdhkarCatalogTest {
    private val surahs = QuranParser.parse(File("src/main/assets/quran.json").readText(Charsets.UTF_8))

    @Test fun quranRangesExistInDataset() {
        for (item in AdhkarCatalog.items) {
            val s = item.source as? AdhkarSource.QuranRange ?: continue
            val verses = surahs.first { it.surah.id == s.surah }.verses
            assertTrue("${item.id} range", s.from >= 1 && s.to <= verses.size && s.from <= s.to)
        }
    }

    @Test fun datasetHas114SurahsWithMatchingVerseCounts() {
        assertEquals(114, surahs.size)
        assertTrue(surahs.all { it.verses.size == it.surah.verseCount })
        assertEquals(286, surahs[1].verses.size)
    }

    @Test fun periodsFilterAndDefault() {
        assertTrue(AdhkarCatalog.forPeriod(AdhkarPeriod.MASAA).any { it.id == "baqara_end" })
        assertTrue(AdhkarCatalog.forPeriod(AdhkarPeriod.SUBAX).none { it.id == "baqara_end" })
        assertEquals(AdhkarPeriod.SUBAX, AdhkarCatalog.periodAt(7))
        assertEquals(AdhkarPeriod.MASAA, AdhkarCatalog.periodAt(17))
    }
}
