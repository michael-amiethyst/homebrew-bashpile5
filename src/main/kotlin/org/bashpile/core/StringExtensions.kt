package org.bashpile.core

import org.bashpile.core.LinuxProcess.Companion.SCRIPT_SUCCESS
import java.io.File
import java.nio.file.Files


/** Strip initial logging line */
fun String.stripFirstLine(): String = this.lines().drop(1).joinToString("\n")

/**
 * Returns stdout/stderr and the exit code.
 *
 * @param arguments Args to send to a Bash block, for a single command bake them into the receiver string.
 */
fun String.runCommand(
    workingDir: File? = File(System.getProperty("user.dir")),
    arguments: List<String> = listOf()
): Pair<String, Int> {
    return LinuxProcess(this).run(workingDir, arguments)
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

        // run command
        val (messages, exitValue) = LinuxProcess($$"$$shfmtAbsolutePath -ln bash -i 4 -ci -w $$script").run()
        check(exitValue == SCRIPT_SUCCESS) {
            "shfmt exited with code ${exitValue}:\n$messages"
        }

        return Files.readString(script)
    } finally {
        Files.deleteIfExists(script)
        Files.deleteIfExists(diagnostics)
    }
}