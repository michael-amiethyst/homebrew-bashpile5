package org.bashpile.core.maintests

import com.github.ajalt.clikt.testing.test
import org.bashpile.core.Main
import org.bashpile.core.Main.Companion.SHEBANG_HEADER
import org.bashpile.core.SCRIPT_SUCCESS
import org.bashpile.core.antlr.AstConvertingVisitor.Companion.STRICT_HEADER
import kotlin.test.*

/**
 * Tests Clikt and [Main._getBast], does not test logging.
 * See SystemTest for logger framework related tests (e.g. for `-vv` option).
 */
class BasicMainTest: MainTest() {

    override val testName = "BasicTest"

    @Test
    fun main_withoutScript_printsHelp() {
        val output = fixture.test(arrayOf(""))
        assertNotEquals(SCRIPT_SUCCESS, output.statusCode)
        assertTrue(output.stdout.startsWith("Usage:"))
    }

    @Test
    fun main_withoutScript_withOption_printsHelp() {
        val output = fixture.test(arrayOf("--name", "Jordi", ""))
        assertNotEquals(SCRIPT_SUCCESS, output.statusCode)
        assertTrue(output.stderr.startsWith("Usage:"))
    }

    @Test
    fun main_withScript_works() {
        val output = fixture.test(arrayOf("src/test/resources/bpsScripts/hello.bps"))
        assertEquals(SCRIPT_SUCCESS, output.statusCode)
        assertTrue(output.stderr.isEmpty())
        assertEquals(SHEBANG_HEADER + STRICT_HEADER + """
            printf "Hello Bashpile!"
            
            """.trimIndent(), output.stdout
        )
    }

    @Test
    fun main_withConcatScript_works() {
        val output = fixture.test(arrayOf("src/test/resources/bpsScripts/helloWithConcat.bps"))
        assertEquals(SCRIPT_SUCCESS, output.statusCode)
        assertTrue(output.stderr.isEmpty())
        assertEquals(SHEBANG_HEADER + STRICT_HEADER + """
            printf "Hello Bashpile!"
            
            """.trimIndent(), output.stdout
        )
    }

    @Test
    fun main_withBadScript_fails() {
        val output = fixture.test(arrayOf("non_existent_script.bps"))
        assertNotEquals(SCRIPT_SUCCESS, output.statusCode)
        assertTrue(output.stdout.startsWith("Usage:"))
    }

    // getBast tests

    @Test
    fun getBast_printBool_works() {
        val render = "print(true)".createRender()
        assertRenderEquals("""
            printf "true"
            
            """.trimIndent(), render
        )
    }

    @Test
    fun getBast_printBool_withParens_works() {
        val render = "print(((true)))".createRender()
        assertRenderEquals("""
            printf "true"
            
            """.trimIndent(), render
        )
    }

    @Test
    fun getBast_printString_tripleConcat_works() {
        val render = """
            print("Hello" + " " + "Bashpile!")""".trimIndent().createRender()
        assertRenderEquals("""
            printf "Hello Bashpile!"
            
            """.trimIndent(), render
        )
    }

    @Test
    fun getBast_printString_tripleConcat_withParen_works() {
        val render = """
            print((("Hello" + " " + "Bashpile!")))""".trimIndent().createRender()
        assertRenderEquals("""
            printf "Hello Bashpile!"
            
            """.trimIndent(), render
        )
    }

    @Test
    fun getBast_printString_tripleConcat_withMoreParens_works() {
        val render = """
            print(((("Hello" + " " + "Bashpile!" ) + "  It's " + "awesome!")))""".trimIndent().createRender()
        assertRenderEquals("""
            printf "Hello Bashpile!  It's awesome!"
            
            """.trimIndent(), render
        )
    }

    @Test
    fun getBast_printFloat_works() {
        val render = "print(1.0)".createRender()
        assertRenderEquals("""
            printf "%s" "1.0"
            
            """.trimIndent(), render
        )
    }

    @Test
    fun getBast_printFloat_plusString_fails() {
        assertFailsWith<UnsupportedOperationException> {
            "print(1.0 + \" apples please\")".createRender()
        }
    }

    @Test
    fun getBast_printString_plusBool_fails() {
        assertFailsWith<UnsupportedOperationException> {
            "print(\"You can't handle the \" + true)".createRender()
        }
    }
}
