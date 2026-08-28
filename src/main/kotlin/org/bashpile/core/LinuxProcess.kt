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

        fun shfmt(unformattedBash: String): String {
            val brewBinPaths = listOf("/home/linuxbrew/.linuxbrew/bin", "/opt/homebrew/bin", "/usr/local/bin")
            val pathDirectories = System.getenv("PATH").orEmpty().split(File.pathSeparator) + brewBinPaths
            val shfmtAbsolutePath = pathDirectories
                .asSequence()
                .map { File(it, "shfmt") }
                .firstOrNull { it.isFile && it.canExecute() }
                ?.absolutePath
                ?: error("shfmt not found in PATH or known Homebrew directories")

            // run command
            val (processOutput, exitValue) =
                LinuxProcess($$"$$shfmtAbsolutePath -ln bash -i 4 -ci -").run(stdin = unformattedBash)
            check(exitValue == SCRIPT_SUCCESS) {
                "shfmt exited with code ${exitValue}:\n$processOutput"
            }

            return processOutput
        }
    }

    private val executors = Executors.newFixedThreadPool(8)

    /**
     * Returns stdout/stderr and the exit code.
     *
     * @param arguments Args to send to a Bash block, for a single command bake them into the receiver string.
     * @param stdin Text to write to the process's standard input.
     */
    fun run(
        workingDir: File? = File(System.getProperty("user.dir")),
        arguments: List<String> = listOf(),
        stdin: String = ""
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

            // Write stdin concurrently with draining output. A process such as `cat` may block writing
            // stdout before it has consumed all stdin, so handling either stream synchronously can deadlock.
            val input = executors.submit {
                process.outputStream.bufferedWriter().use { it.write(stdin) }
            }

            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor()
            }

            input.get(10, TimeUnit.SECONDS)

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