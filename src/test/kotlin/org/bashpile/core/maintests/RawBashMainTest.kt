package org.bashpile.core.maintests

import kotlin.test.Test

/**
 * Tests Shell Strings and Shell Lines
 */
class RawBashMainTest : MainTest() {

    override val testName = "RawBashTest"

    @Test
    fun rawBash_statement_works() {
        val script = """
            b(IFS=" ")
            print("NCC-1701")

            """.trimIndent().createRender()
        assertRenderEquals("""
            IFS=" "
            printf "NCC-1701"

            """.trimIndent(), script
        ).assertRenderProduces("NCC-1701\n")
    }

    @Test
    fun rawBash_expression_works() {
        val script = """
            print(b($(expr 1 + 1)))

            """.trimIndent().createRender()
        assertRenderEquals("""
            printf "$(expr 1 + 1)"

            """.trimIndent(), script
        ).assertRenderProduces("2\n")
    }
}
