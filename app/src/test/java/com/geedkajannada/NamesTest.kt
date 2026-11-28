package com.geedkajannada

import com.geedkajannada.feature.names.NamesData
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class NamesTest {
    private val names = NamesData.parse(File("src/main/assets/names.json").readText())

    @Test fun ninetyNineUniqueNamesInOrder() {
        assertEquals((1..99).toList(), names.map { it.id })
        assertEquals(99, names.map { it.arabic }.toSet().size)
        assertEquals(99, names.map { it.latin }.toSet().size)
    }

    @Test fun searchByNumberNameMeaningAndArabicWithoutHarakat() {
        assertEquals(listOf(1), NamesData.filter(names, "1").map { it.id })
        assertEquals(listOf(4), NamesData.filter(names, "Qudduus").map { it.id })
        assertEquals(listOf(3), NamesData.filter(names, "boqorka").map { it.id })
        assertEquals(1, NamesData.filter(names, "الرحيم").size)
    }
}
