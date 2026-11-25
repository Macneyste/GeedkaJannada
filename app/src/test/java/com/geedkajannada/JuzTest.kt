package com.geedkajannada

import com.geedkajannada.data.Juz
import org.junit.Assert.assertEquals
import org.junit.Test

class JuzTest {
    @Test fun thirtyStartsInOrder() {
        assertEquals(30, Juz.starts.size)
        val sorted = Juz.starts.sortedWith(compareBy({ it.first }, { it.second }))
        assertEquals(sorted, Juz.starts)
    }

    @Test fun juzOfKnownVerses() {
        assertEquals(1, Juz.of(1, 1))
        assertEquals(1, Juz.of(2, 141))
        assertEquals(2, Juz.of(2, 142))
        assertEquals(6, Juz.of(4, 148))
        assertEquals(30, Juz.of(112, 1))
        assertEquals(30, Juz.of(114, 6))
    }
}
