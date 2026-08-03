package org.bashpile.core

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString


/** Shell script success (0), all other numbers are errors.  Generally 1-255. */
const val SCRIPT_SUCCESS = 0
const val SCRIPT_ERROR__GENERIC = 1

private val executors = Executors.newFixedThreadPool(8)

/** Strip initial logging line */
fun String.stripFirstLine(): String = this.lines().drop(1).joinToString("\n")

/**
 * Returns stdout/stderr and the exit code.
 */
fun String.runCommand(workingDir: File? = null, arguments: List<String> = listOf()): Pair<String, Int> {
    val cwd = File(System.getProperty("user.dir"))
    val tempFile = Files.createTempFile("", "").writeString(this).makeExecutable()
    val commandResult = tempFile.absolutePathString().runCommandImpl(workingDir ?: cwd, arguments)
    return commandResult
}

private fun String.runCommandImpl(workingDir: File, arguments: List<String>): Pair<String, Int> {
    var proc: Process? = null
    try {
        val callable: Callable<Process> = Callable {
            val argumentsString = arguments.joinToString(" ")
            val proc2 = ProcessBuilder(
                listOf("bash", "-c", "$this $argumentsString"))
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectErrorStream(true)
                .start()

            if (!proc2.waitFor(10, TimeUnit.SECONDS)) {
                proc2.destroyForcibly()
            }
            return@Callable proc2
        }

        proc = executors.submit(callable).get(10, TimeUnit.SECONDS)

        // strip out blank lines and lines from sdkman, add newline back
        val text = proc.inputStream.bufferedReader().readText().trim()
        val lines = text.split("\n")
        val filteredText = lines
            .filter { !it.contains("Using java version") }
            .joinToString("\n") + "\n"
        return Pair(filteredText, proc.exitValue())

    } catch(e: IOException) {
        return Pair(e.stackTraceToString(), proc?.exitValue() ?: -1)
    }
}
