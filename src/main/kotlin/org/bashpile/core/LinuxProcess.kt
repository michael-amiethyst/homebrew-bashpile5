package org.bashpile.core

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

class LinuxProcess(val command: String) {

    companion object {
        /** Shell script success (0), all other numbers are errors.  Generally 1-255. */
        const val SCRIPT_SUCCESS = 0
        const val SCRIPT_ERROR__GENERIC = 1
    }

    private val executors = Executors.newFixedThreadPool(8)

    /**
     * Returns stdout/stderr and the exit code.
     *
     * @param arguments Args to send to a Bash block, for a single command bake them into the receiver string.
     */
    fun run(
        workingDir: File? = File(System.getProperty("user.dir")),
        arguments: List<String> = listOf()
    ): Pair<String, Int> {
        val tempFilePath =
            Files.createTempFile("", "").writeString(command).makeExecutable().absolutePathString()
        var proc: Process? = null
        try {
            val argumentsString = arguments.joinToString(" ")
            val process = ProcessBuilder(
                listOf("bash", "-c", "$tempFilePath $argumentsString"))
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectErrorStream(true)
                .start()
            proc = process

            // Drain the merged stdout/stderr pipe to avoid a full buffer, leading to a timeout
            val output = executors.submit<String> {
                process.inputStream.bufferedReader().use { it.readText() }
            }

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor()
            }

            // strip out blank lines and lines from sdkman, add newline back
            val text = output.get(10, TimeUnit.SECONDS).trim()
            val lines = text.split("\n")
            val filteredText = lines
                .filter { !it.contains("Using java version") }
                .joinToString("\n") + "\n"
            return Pair(filteredText, process.exitValue())

        } catch(e: IOException) {
            return Pair(e.stackTraceToString(), proc?.exitValue() ?: -1)
        }
    }
}