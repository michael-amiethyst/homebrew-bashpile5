package org.bashpile.core.maintests

import org.bashpile.core.LinuxProcess
import org.bashpile.core.LinuxProcess.Companion.SCRIPT_ERROR__GENERIC
import org.bashpile.core.LinuxProcess.Companion.SCRIPT_SUCCESS
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests Shell Strings and Shell Lines
 */
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
        val commandResult = LinuxProcess(renderedBash).run()
        assertEquals("\n", commandResult.first)
        assertEquals(SCRIPT_SUCCESS, commandResult.second)
    }

    /**
     * Render produces the blank string because Bash is weird.
     * [https://mywiki.wooledge.org/BashFAQ/104](Bash FAQ)
     */
    @Test
    fun getBast_shellLine_initialVar_works() {
        val script = """
            test_var=0
            test_var=1 printf "${'$'}test_var" """.trimIndent().createRender()
        assertRenderEquals("""
            test_var=0
            test_var=1 printf "${'$'}test_var"
            
            """.trimIndent(), script
        ).assertRenderProduces("0\n")
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
        val script = "'ls \"' + $(printf \".\") + '\"'".createRender()
        assertRenderEquals("""
            ls "$(printf ".")"
            
            """.trimIndent(), script
        ).assertRenderProduces({it.contains("bin")})
    }

    @Test
    fun getBast_looseShellstring_works() {
        // loose unsets '-o pipefail' so `exit 1` is ignored
        val script = "'ls \"' + l$(printf \".\"; exit 1) + '\"'".createRender()
        assertRenderEquals("""
            ls "$(
                eval "${'$'}__bp_old_options"
                printf "."
                exit 1
            )"
            
            """.trimIndent(), script
        ).assertRenderProduces({ it.contains("bin") })
    }

    @Test
    fun getBast_looseShellstring_looseIsScoped() {
        val script = """
            'ls "' + l$(printf "."; exit 1) + '"'
            printf "%s" "${'$'}undefinedVar"
            """.trimIndent().createRender()
        assertRenderEquals("""
            ls "$(
                eval "${'$'}__bp_old_options"
                printf "."
                exit 1
            )"
            printf "%s" "${'$'}undefinedVar"
            
            """.trimIndent(), script
        ).assertRenderProduces({ it.contains("bin") }, SCRIPT_ERROR__GENERIC)
    }

    @Test
    fun getBast_looseShellstring_twoInOneLine_works() {
        // TODO 0.22.0 - make test with escaped double quotes.  E.g. include `+ "\""`
        val script = """
            'ls "' + l$(printf "%s" "-all") + '" "' + l$(printf "."; exit 1) + '"'
            """.trimIndent().createRender()
        assertRenderEquals(
            """
            ls "$(
                eval "${'$'}__bp_old_options"
                printf "%s" "-all"
            )" "$(
                eval "${'$'}__bp_old_options"
                printf "."
                exit 1
            )"

            """.trimIndent(), script
        ).assertRenderProduces({ it.contains("bin") })
    }

    @Test
    fun getBast_shellstring_withConcat_works() {
        val script = """
            print("Hello " + $(printf 'shellstring!'))""".trim().createRender()
        assertRenderEquals("""
            printf "Hello $(printf 'shellstring!')"
            """.trimIndent() + "\n", script
        )
    }

    @Test
    fun getBast_shellstring_nestedSubshells_works() {
        val script = """
            print($(ls "$(printf '.')"))""".trim().createRender()
        assertRenderEquals("""
            declare __bp_var0
            __bp_var0="$(printf '.')"
            printf "$(ls "${'$'}{__bp_var0}")"
            """.trimIndent() + "\n", script
        )

        script.assertRenderProduces({ it.contains("bin") })
    }

    @Test
    fun getBast_shellstring_nestedSubshells_withInnerError_fails() {
        val pathname = "src/test/resources/bpsScripts/nestedSubshells.bps"
        val script =  File(pathname).readText().trim().createRender()
        assertRenderEquals(
            $$"""
            set -euo pipefail
            declare __bp_var0
            __bp_var0="$(
                echo '.'
                exit $$SCRIPT_ERROR__GENERIC
            )"
            printf "$(ls "${__bp_var0}")"
            """.trimIndent() + "\n", script
        )
        script.assertRenderProduces({
            it.contains("Error (exit code ${SCRIPT_ERROR__GENERIC}) found") }, SCRIPT_ERROR__GENERIC)
    }

    @Test
    fun getBast_shellstring_withShellStringConcat_works() {
        val script = """
            print($(printf "$(printf 'Hello ') $(printf 'shellstring!')"))""".trim().createRender()
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
}
