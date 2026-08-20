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
fun String.runCommand(
    workingDir: File? = File(System.getProperty("user.dir")),
    arguments: List<String> = listOf()
): Pair<String, Int> {
    val tempFilePath =
        Files.createTempFile("", "").writeString(this).makeExecutable().absolutePathString()
    var proc: Process? = null
    try {
        val callable: Callable<Process> = Callable {
            val argumentsString = arguments.joinToString(" ")
            val proc2 = ProcessBuilder(
                listOf("bash", "-c", "$tempFilePath $argumentsString"))
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

fun String.shfmt(): String {
    val script = Files.createTempFile("bashpile-", ".sh")
    val diagnostics = Files.createTempFile("bashpile-shfmt-", ".log")

    try {
        // different directories for OSX Apple and Intel CPUs
        val brewBinPaths = listOf("/home/linuxbrew/.linuxbrew/bin", "/opt/homebrew/bin", "/usr/local/bin")
        val pathDirectories = System.getenv("PATH").orEmpty().split(File.pathSeparator) + brewBinPaths

        val shfmtAbsolutePath = pathDirectories
            .asSequence()
            .map { File(it, "shfmt") }
            .firstOrNull { it.isFile && it.canExecute() }
            ?.absolutePath
            ?: error("shfmt not found in PATH or known Homebrew directories")
        Files.writeString(script, this)

        val process = ProcessBuilder(
            shfmtAbsolutePath, "-ln", "bash", "-i", "4", "-ci", "-w", script.toString()
        )
            .redirectErrorStream(true)
            .redirectOutput(diagnostics.toFile())
            .start()

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            process.waitFor()
            error("shfmt timed out after 30 seconds")
        }

        val messages = Files.readString(diagnostics)
        check(process.exitValue() == SCRIPT_SUCCESS) {
            "shfmt exited with code ${process.exitValue()}:\n$messages"
        }

        return Files.readString(script)
    } finally {
        Files.deleteIfExists(script)
        Files.deleteIfExists(diagnostics)
    }
}