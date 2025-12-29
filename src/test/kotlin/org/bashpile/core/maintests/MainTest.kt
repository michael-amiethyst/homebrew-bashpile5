package org.bashpile.core.maintests

import org.bashpile.core.Main
import org.bashpile.core.antlr.AstConvertingVisitor.Companion.STRICT_HEADER
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED
import org.bashpile.core.runCommand
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.function.Predicate
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// TODO 0.20.0 -- run shellcheck on generated scripts (/build/shellcheck) in a GHA
abstract class MainTest {

    companion object {
        const val DIRECTORY_PREFIX = "build/shellcheck"
        // framework makes a separate MainTest instance per test, so this needs to be here
        private var filenameCounter = 0
    }

    protected lateinit var fixture: Main

    abstract val testName: String


    @BeforeTest
    fun init() {
        fixture = Main()
        Files.createDirectories(Paths.get(DIRECTORY_PREFIX))
    }

    protected fun String.createRender(): String {
        val scriptStream = this.byteInputStream()
        return fixture._getBast(scriptStream).render(UNQUOTED)
    }

    protected fun assertRenderEquals(expectedBash: String, renderedBash: String): String {
        assertTrue("No STRICT_HEADER?") { renderedBash.startsWith(STRICT_HEADER) }
        assertEquals(expectedBash, renderedBash.removePrefix(STRICT_HEADER))

        val filename = Paths.get("$DIRECTORY_PREFIX/$testName${filenameCounter++}.bpc")
        Files.writeString(filename, renderedBash,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)
        return renderedBash
    }

    protected fun String.assertRenderProduces(expectedStdout: String?, expectedExitCode: Int = 0, arguments: List<String> = listOf()) {
        val results = runCommand(arguments = arguments)
        if (expectedStdout != null) { assertEquals(expectedStdout, results.first) }
        assertEquals(expectedExitCode, results.second)
    }

    protected fun String.assertRenderProduces(test: Predicate<String>, expectedExitCode: Int = 0) {
        val results = runCommand()
        assertTrue(test.test(results.first))
        assertEquals(expectedExitCode, results.second)
    }
}
