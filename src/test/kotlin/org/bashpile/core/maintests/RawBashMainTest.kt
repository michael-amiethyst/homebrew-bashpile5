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
    fun rawBash_expression_withSubshell_works() {
        val script = $$"""
            b(true && ) + b((export HELLO="Hello World"; printf "%s" "$HELLO"))
            b(set +u; printf "%s" "$HELLO"; set -u) // should be blank

            """.trimIndent().createRender()
        assertRenderEquals($$"""
            true && (
                export HELLO="Hello World"
                printf "%s" "$HELLO"
            )
            set +u
            printf "%s" "$HELLO"
            set -u

            """.trimIndent(), script
        ).assertRenderProduces("Hello World\n")
    }
}
