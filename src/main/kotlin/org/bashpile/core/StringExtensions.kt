package org.bashpile.core

import java.io.File


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