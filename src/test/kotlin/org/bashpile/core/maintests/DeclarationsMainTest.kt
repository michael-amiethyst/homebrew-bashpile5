package org.bashpile.core.maintests

import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Tests Declarations, Assignments and Typing
 */
class DeclarationsMainTest : MainTest() {

    override val testName = "DeclarationsTest"

    @Test
    fun getBast_declare_bool_works() {
        val bashScript = "b: boolean = true".createRender()
        assertRenderEquals("""
            declare b
            b=true

            """.trimIndent(), bashScript
        ).assertRenderProduces("\n")
    }

    @Test
    fun getBast_declare_readonlyExported_string_works() {
        val bashScript = "b: readonly exported string = 'A_STRING'".createRender()
        assertRenderEquals("""
            declare -x b
            b="A_STRING"

            """.trimIndent(), bashScript
        ).assertRenderProduces("\n")
    }

    @Test
    fun getBast_declare_readonlyExported_string_withPrintWorks() {
        val bashScript = """
            b: readonly exported string = 'A_STRING'
            print(b)
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare -x b
            b="A_STRING"
            printf "%s" "${'$'}{b}"
        
            """.trimIndent(), bashScript
        ).assertRenderProduces("A_STRING\n")
    }

    @Test
    fun getBast_reassign_works() {
        val bashScript = """
            b: exported string = 'A_STRING'
            b="B_STRING"
            print(b)
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare -x b
            b="A_STRING"
            b="B_STRING"
            printf "%s" "${'$'}{b}"
        
            """.trimIndent(), bashScript
        ).assertRenderProduces("B_STRING\n")
    }

    @Test
    fun getBast_reassign_withLiteralQuotes_works() {
        val bashScript = """
            b: exported string = 'A_STRING'
            b="'B_STRING'"
            print(b)
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare -x b
            b="A_STRING"
            b="'B_STRING'"
            printf "%s" "${'$'}{b}"

            """.trimIndent(), bashScript
        ).assertRenderProduces("'B_STRING'\n")
    }

    @Test
    fun getBast_reassign_readonly_fails() {
        assertFailsWith<IllegalStateException> {
            """
                b: readonly exported string = 'A_STRING'
                b = "B_STRING"
                print(b)
                """.trimIndent().createRender()
        }
    }

    @Test
    fun getBast_reassign_wrongType_fails() {
        assertFailsWith<IllegalStateException> {
            """
                b: exported string = 'A_STRING'
                i: integer = 0
                b = i
                print(b)
                """.trimIndent().createRender()
        }
    }
}
