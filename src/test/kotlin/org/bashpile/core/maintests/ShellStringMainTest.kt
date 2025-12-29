package org.bashpile.core.maintests

import org.bashpile.core.SCRIPT_ERROR__GENERIC
import org.bashpile.core.SCRIPT_SUCCESS
import org.bashpile.core.runCommand
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests Shell Strings and Shell Lines
 */
// TODO 0.21.0 -- make $() a subshell string, l$() a loose subshell string and #() a verbatim shellstring
class ShellStringMainTest : MainTest() {

    override val testName = "ShellStringTest"

    @Test
    fun getBast_shellLine_printf_works() {
        val script = "printf \"true\"".createRender()
        assertRenderEquals("""
            printf "true"
            
            """.trimIndent(), script
        )
    }

    @Test
    fun getBast_shellLine_or_works() {
        val renderedBash = """
            ls some_random_file_that_does_not_exist.txt >/dev/null 2>&1 || true
            """.trimIndent().createRender()
        assertRenderEquals("""
            ls some_random_file_that_does_not_exist.txt >/dev/null 2>&1 || true
            
            """.trimIndent(), renderedBash
        )
        val commandResult = renderedBash.runCommand()
        assertEquals("\n", commandResult.first)
        assertEquals(SCRIPT_SUCCESS, commandResult.second)
    }

    @Test
    fun getBast_shellLine_initialVar_works() {
        val script = "test_var=5 printf \"\$test_var\"".createRender()
        assertRenderEquals("""
            test_var=5 printf "${'$'}test_var"
            
            """.trimIndent(), script
        )
    }

    @Test
    fun getBast_shellLine_literalNewline_works() {
        val script = "printf \"newline\"".createRender()
        assertRenderEquals("""
            printf "newline"
            
            """.trimIndent(), script
        )
    }

    @Test
    fun getBast_shellstring_works() {
        val script = "#(printf \"newline\")".createRender()
        assertRenderEquals("""
            $(printf "newline")
            
            """.trimIndent(), script
        )
    }

    @Test
    fun getBast_looseShellstring_works() {
        val script = "l#(printf \"newline\"; exit 1)".createRender()
        assertRenderEquals("""
            eval "${'$'}__bp_old_options"
            $(printf "newline"; exit 1)
            set -euo pipefail
            
            """.trimIndent(), script
        )
    }

    @Test
    fun getBast_shellstring_withConcat_works() {
        val script = """
            print("Hello " + #(printf 'shellstring!'))""".trim().createRender()
        assertRenderEquals("""
            printf "Hello $(printf 'shellstring!')"
            """.trimIndent() + "\n", script
        )
    }

    @Test
    fun getBast_shellstring_nestedSubshells_works() {
        val script = """
            print(#(ls $(echo '.')))""".trim().createRender()
        assertRenderEquals("""
            declare __bp_var0
            __bp_var0="$(echo '.')"
            printf "$(ls ${'$'}{__bp_var0})"
            """.trimIndent() + "\n", script
        )

        script.assertRenderProduces({
            it.contains("bin")
        })
    }

    @Test
    fun getBast_shellstring_nestedSubshells_withInnerError_fails() {
        val pathname = "src/test/resources/bpsScripts/nestedSubshells.bps"
        val script =  File(pathname).readText().trim().createRender()
        assertRenderEquals("""
            set -euo pipefail
            declare __bp_var0
            __bp_var0="$(echo '.'; exit ${SCRIPT_ERROR__GENERIC})"
            printf "$(ls ${'$'}{__bp_var0})"
            """.trimIndent() + "\n", script
        )
        script.assertRenderProduces({
            it.contains("Error (exit code ${SCRIPT_ERROR__GENERIC}) found") }, SCRIPT_ERROR__GENERIC)
    }

    @Test
    fun getBast_shellstring_withShellStringConcat_works() {
        val script = """
            print(#(printf "$(printf 'Hello ') $(printf 'shellstring!')"))""".trim().createRender()
        assertRenderEquals("""
            declare __bp_var0
            __bp_var0="$(printf 'Hello ')"
            declare __bp_var1
            __bp_var1="$(printf 'shellstring!')"
            printf "$(printf "${'$'}{__bp_var0} ${'$'}{__bp_var1}")"
            """.trimIndent() + "\n", script
        ).assertRenderProduces(null, SCRIPT_SUCCESS)

        // confirm succeeds with strict mode
        "set -euo pipefail\n$script".assertRenderProduces(null, SCRIPT_SUCCESS)
    }

    @Test
    fun verboseShellString_works() {
        val script = """
            v#(IFS=" ")
            print("NCC-1701")

            """.trimIndent().createRender()
        assertRenderEquals("""
            IFS=" "
            printf "NCC-1701"

            """.trimIndent(), script
        ).assertRenderProduces("NCC-1701\n", arguments = listOf("first", "second", "third"))
    }
}
