package com.geedkajannada.data

/** Caawiyeyaal saafi ah oo loogu talagalay muuqaalka mushaf-ka. */
object QuranText {
    /** Aayadaha waxaa loo qaybiyaa baloogyo si suuradaha dhaadheer aanay u culusaan. */
    const val VERSES_PER_BLOCK = 10

    /** Lambarka aayadda oo tirooyinka Carabiga (٠-٩) ah. */
    fun arabicDigits(number: Int): String = number.toString().map { '\u0660' + (it - '0') }.joinToString("")

    fun blockOf(verse: Int): Int = (verse - 1).coerceAtLeast(0) / VERSES_PER_BLOCK

    fun firstVerseOfBlock(block: Int): Int = block * VERSES_PER_BLOCK + 1

    fun blocks(verseCount: Int): List<IntRange> =
        (1..verseCount).chunked(VERSES_PER_BLOCK) { it.first()..it.last() }
}
