package org.bashpile.core

import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtensionsKtTest {
    @Test
    fun stripFirstLineTest() {
        assertEquals("""
            one
            two
            three
        """.trimIndent().stripFirstLine(), """
            two
            three
        """.trimIndent())
    }

}
