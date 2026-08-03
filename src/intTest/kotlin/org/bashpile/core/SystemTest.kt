package org.bashpile.core

import org.bashpile.core.antlr.AstConvertingVisitor.Companion.STRICT_HEADER
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import kotlin.io.path.exists
import kotlin.io.path.isExecutable


/**
 * Overall System integration test for [Main].
 */
class SystemTest {
    companion object {
        private const val BASHPILE_EXECUTABLE = "build/native/nativeCompile/bashpile"
        private const val HELLO_FILENAME = "src/test/resources/bpsScripts/hello.bps"
        private const val SHEBANG_SCRIPT_FILENAME = "build/resources/test/bpsScripts/shebang.bps"

        @JvmStatic
        @BeforeAll
        fun requireNativeBinary() {
            assumeTrue(Files.isExecutable(Path.of(BASHPILE_EXECUTABLE)))
        }
    }

    @Test
    fun system_withBadFilename_printsHelp() {
        val output = "$BASHPILE_EXECUTABLE BAD_FILENAME".runCommand()
        assertNotEquals(SCRIPT_SUCCESS, output.second)
        assertTrue(output.first.startsWith("Usage:"))
    }

    @Test
    fun system_withNoFilename_printsHelp() {
        val output = BASHPILE_EXECUTABLE.runCommand()
        assertNotEquals(SCRIPT_SUCCESS, output.second)
        assertTrue(output.first.startsWith("Usage:"))
    }

    @Test
    fun systemWorks() {
        val output = "$BASHPILE_EXECUTABLE '$HELLO_FILENAME'".runCommand()
        assertEquals(SCRIPT_SUCCESS, output.second)
        val expected = Main.SHEBANG_HEADER + STRICT_HEADER + "printf \"Hello Bashpile!\"\n"
        assertEquals(expected, output.first)
    }

    @Test
    fun system_withVerbose_works() {
        val output = "$BASHPILE_EXECUTABLE --verbose '$HELLO_FILENAME'".runCommand()
        assertEquals(SCRIPT_SUCCESS, output.second)
        assertTrue(output.first.contains(Main.STARTUP_MESSAGE), "Output: ${output.first}")
        assertFalse(output.first.contains(Main.VERBOSE_ENABLED_MESSAGE), "Output: ${output.first}")
        assertTrue(output.first.endsWith("printf \"Hello Bashpile!\"\n"))
    }

    @Test
    fun system_withDoubleVerbose_works() {
        val output = "$BASHPILE_EXECUTABLE -vv '$HELLO_FILENAME'".runCommand()
        assertEquals(SCRIPT_SUCCESS, output.second)
        assertTrue(output.first.contains(Main.STARTUP_MESSAGE), "Output: ${output.first}")
        assertTrue(output.first.contains(Main.VERBOSE_ENABLED_MESSAGE), "Output: ${output.first}")
        assertTrue(output.first.endsWith("printf \"Hello Bashpile!\"\n"))
    }

    @Test
    fun system_withVersion_works() {
        val output = "$BASHPILE_EXECUTABLE --version".runCommand()
        assertEquals(SCRIPT_SUCCESS, output.second)
        assertEquals(Main.VERSION + "\n", output.first)
    }

    @Test
    fun system_withCommandString_works() {
        val output = "$BASHPILE_EXECUTABLE -c \"print('Hello World')\"".runCommand()
        assertEquals("Hello World\n", output.first)
        assertEquals(SCRIPT_SUCCESS, output.second)
    }

    @Test
    fun system_withCommandStdin_works() {
        val output = "$BASHPILE_EXECUTABLE -c <<< \"print('Hello World')\"".runCommand()
        assertEquals("Hello World\n", output.first)
        assertEquals(SCRIPT_SUCCESS, output.second)
    }

    @Test
    fun system_withCommandPrintfStdin_works() {
        val output = "printf \"print('Hello World')\" | $BASHPILE_EXECUTABLE -c".runCommand()
        assertEquals("Hello World\n", output.first)
        assertEquals(SCRIPT_SUCCESS, output.second)
    }

    @Test
    fun system_shebang_works() {
        val shebangPath = Path.of(SHEBANG_SCRIPT_FILENAME)
        assumeTrue(shebangPath.exists(), "Shebang test file not found")
        val path = shebangPath.makeExecutable()
        assumeTrue(path.isExecutable(), "Shebang test file not executable")
        val output = path.toString().runCommand()
        assertEquals(SCRIPT_SUCCESS, output.second, "Script not successful, output was: ${output.first}")
        val resultLines = output.first.split("\n").filter { it.isNotBlank() }
        assertEquals("printf \"Hello Shebang!\"", resultLines.last())
    }

    @Test
    fun system_withEval_works() {
        val output = "eval \"$($SHEBANG_SCRIPT_FILENAME | tail -n 1)\"".runCommand()
        assertEquals(SCRIPT_SUCCESS, output.second)
        assertEquals("Hello Shebang!\n", output.first)
    }

    private fun Path.makeExecutable(): Path {
        if (!this.isExecutable()) {
            val perms = Files.getPosixFilePermissions(this)
            perms.add(OWNER_EXECUTE)
            Files.setPosixFilePermissions(this, perms)
        }
        return this
    }
}
