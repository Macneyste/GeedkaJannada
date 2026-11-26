package com.geedkajannada

import com.geedkajannada.data.QuranText
import org.junit.Assert.assertEquals
import org.junit.Test

class QuranTextTest {
    @Test fun markerUsesArabicIndicDigits() {
        assertEquals("۝١", QuranText.verseMarker(1))
        assertEquals("۝٢٥٥", QuranText.verseMarker(255))
    }

    @Test fun blocksCoverEveryVerseOnce() {
        val blocks = QuranText.blocks(23)
        assertEquals(listOf(1..10, 11..20, 21..23), blocks)
        assertEquals(2, QuranText.blockOf(21))
        assertEquals(21, QuranText.firstVerseOfBlock(2))
    }
}
