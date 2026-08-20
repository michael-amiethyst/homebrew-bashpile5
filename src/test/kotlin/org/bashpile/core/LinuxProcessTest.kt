package org.bashpile.core

import org.bashpile.core.LinuxProcess.Companion.SCRIPT_SUCCESS
import org.bashpile.core.antlr.AstConvertingVisitor.Companion.STRICT_HEADER
import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxProcessTest {
    @Test
    fun run_withMultiline_works() {
        val result = LinuxProcess("echo 'Hello \n world'").run()
        assertEquals("Hello \n world\n", result.first)
        assertEquals(SCRIPT_SUCCESS, result.second)
    }

    @Test
    fun run_withDoubleQuotes_works() {
        val result = LinuxProcess("echo \"Hello world\"").run()
        assertEquals("Hello world\n", result.first)
        assertEquals(SCRIPT_SUCCESS, result.second)
    }

    /** Compiled 'print("Hello World")' @ Version 0.14.0 */
    @Test
    fun run_withFullProgram_works() {
        val result = LinuxProcess("""
            declare -i s
            trap 's=$?; echo "Error (exit code ${'$'}s) found on line ${'$'}LINENO of generated Bash.\
              Command was: ${'$'}BASH_COMMAND"; exit ${'$'}s' ERR
            declare __bp_old_options
            __bp_old_options=$(set +o)
            set -euo pipefail
            printf "Hello World"
        """.trimIndent()).run()
        assertEquals("Hello World\n", result.first)
        assertEquals(SCRIPT_SUCCESS, result.second)
    }

    @Test
    fun run_withForeachFileline_works() {
        val result = LinuxProcess("""
            declare -i s
            trap 's=$?; echo "Error (exit code ${'$'}s) found on line ${'$'}LINENO of generated Bash.\
              Command was: ${'$'}BASH_COMMAND"; exit ${'$'}s' ERR
            declare __bp_old_options
            __bp_old_options=$(set +o)
            set -euo pipefail
            cat src/test/resources/data/example.csv | while IFS=',' read -r FirstName LastName Email Phone; do
                printf "${'$'}FirstName ${'$'}LastName ${'$'}Email ${'$'}Phone\n"
            done
        """.trimIndent()).run()
        assertEquals("""
            FirstName LastName Email Phone
            Alice Smith alice.smith@email.com 555-1234
            Bob Johnson bob.j@email.com 555-5678
            Charlie Williams c.williams@email.com 555-9012
            
        """.trimIndent(), result.first)
        assertEquals(SCRIPT_SUCCESS, result.second)
    }

    @Test
    fun run_withIf_works() {
        val result = LinuxProcess("""
            $STRICT_HEADER
            if [ 1 -ge 0 ]; then
                printf "Math is mathing\n"
            fi
        """.trimIndent()).run()
        assertEquals("""
            Math is mathing
            
        """.trimIndent(), result.first)
        assertEquals(SCRIPT_SUCCESS, result.second)
    }
}