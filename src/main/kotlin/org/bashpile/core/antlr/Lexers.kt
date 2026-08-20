package org.bashpile.core.antlr

import com.google.common.annotations.VisibleForTesting
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.misc.Interval
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bashpile.core.LinuxProcess
import org.bashpile.core.LinuxProcess.Companion.SCRIPT_SUCCESS
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Hashtable
import java.util.Locale
import java.util.regex.Pattern
import javax.annotation.Nonnull

/**
 * Helper class for BashpileLexer.
 */
class Lexers {
    companion object {
        /**
         * Maps a Bash Command to if it is valid (installed, executable and reachable) or not.
         * <br></br>
         * Is a Hashtable to support testing in parallel.
         */
        private val COMMAND_TO_VALIDITY_CACHE: MutableMap<String, Boolean> = Hashtable(100)

        /** A regex for a valid Bash identifier  */
        private val COMMAND_PATTERN: Pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*")

        private val WINDOWS_FILE_PATTERN: Pattern = Pattern.compile("^([A-Za-z]):\\\\([a-zA-Z_.][a-zA-Z0-9_\\-.]*)+")

        private val POXIX_FILE_PATTERN: Pattern = Pattern.compile("^(?:/?[a-zA-Z_.-][a-zA-Z0-9_.\\\\-]*)+")

        private val OSX_FILE_PATTERN: Pattern = Pattern.compile("^(?:/?[a-zA-Z0-9_.-][a-zA-Z0-9_.@\\\\-]*)+")

        /** A regex for a Bash assignment  */
        private val ASSIGN_PATTERN: Pattern =
            Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*=(\"[^\"]*\"|'[^']*'|[^ ]+|[0-9]+)+\\s*")

        /** File is really a command  */
        private val COMMAND_TYPES = listOf("alias", "function", "builtin", "file")

        /** Should be excluded from being a Linux command  */
        private val BASHPILE_KEYWORDS = listOf("return", "readonly", "unset", "else-if")

        private val LOG: Logger = LogManager.getLogger(Lexers::class.java)

        /**
         * Checks if the command portion of the input Bash line is a valid Bash command.
         *
         * Running 'type' to verify is expensive, so we both check if the command is valid with a Regex and cache results.
         *
         * @param charStream From the `_input` of a Semantic Predicate in the BashpileLexer
         * @return Checks if the parsed command is valid.
         */
        @JvmStatic
        fun isLinuxCommand(@Nonnull charStream: CharStream): Boolean {
            // guard
            if (charStream.size() == 0) {
                return false
            }

            // body
            var startOfLine = true
            // scan backwards until at start, the last newline or a character besides space or newline
            var i = charStream.index() - 1
            while (i >= 0 && charStream.getText(Interval.of(i, i)) != "\n") {
                val curr = charStream.getText(Interval.of(i, i))
                if (curr != " ") {
                    startOfLine = false
                    break
                }
                i--
            }
            return if (startOfLine) {
                // chop off everything before charStream's index
                _isLinuxCommand(charStream.getText(Interval.of(charStream.index(), charStream.size())))
            } else {
                false
            }
        }

        /**
         * Checks if the command portion of the input Bash line is a valid Bash command.
         * Accepts Windows style filenames for when we are running under WSL.
         * <br></br>
         * Running 'type' to verify is slow so we both check if the command is valid with a Regex and cache results.
         *
         * @param bashLineIn A line of Bash script to check.
         * @return Checks if the parsed command is valid.
         */
        @VisibleForTesting
        @JvmStatic
        @Suppress("functionName")
        fun _isLinuxCommand(@Nonnull bashLineIn: String): Boolean {
            // guard
            var bashLine = bashLineIn
            if (StringUtils.isBlank(bashLine) || bashLine.startsWith(" ")) {
                return false
            }

            // check for var=value preambles and remove
            var match = ASSIGN_PATTERN.matcher(bashLine)
            while (match.find()) {
                bashLine = match.replaceFirst("")
                match = ASSIGN_PATTERN.matcher(bashLine)
            }

            // split on whitespace or Bash command separator
            val parts = bashLine.split("[ \n;]".toRegex()).dropLastWhile { it.isEmpty() }
            if (parts.isEmpty()) {
                return false
            }
            var command = parts.toTypedArray()[0]

            if (COMMAND_TO_VALIDITY_CACHE.containsKey(command)) {
                return COMMAND_TO_VALIDITY_CACHE[command]!!
            }

            val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
            val isWindows = osName.startsWith("windows")
            if (isWindows) {
                // change paths like C:\filename to /mnt/c/filename for WSL
                command = FilenameUtils.separatorsToUnix(command)
                val matcher = WINDOWS_FILE_PATTERN.matcher(command)
                if (matcher.find()) {
                    val driveLetter = matcher.group(1).lowercase(Locale.getDefault())
                    val relativePathFromDriveRoot = command.substring(3)
                    command = "/mnt/$driveLetter/$relativePathFromDriveRoot"
                }
            }

            try {
                val filePattern = if (!osName.contains("mac")) {
                    POXIX_FILE_PATTERN
                } else { OSX_FILE_PATTERN }
                // may need a 'and not find with createsStatementRegex' when we add file path recognition to shell lines
                if (COMMAND_PATTERN.matcher(command).matches() || filePattern.matcher(command).matches()) {
                    LOG.trace("Running external 'type' command on {}", command)
                    val results = LinuxProcess("type -t $command").run()

                    // exclude keywords like 'function'
                    val typeResults: String = results.first.trim()
                    val ret = results.second == SCRIPT_SUCCESS
                            && COMMAND_TYPES.contains(typeResults) && !BASHPILE_KEYWORDS.contains(command)
                    COMMAND_TO_VALIDITY_CACHE[command] = ret
                    return ret
                } else if (filePattern.matcher(command).matches() && !BASHPILE_KEYWORDS.contains(command)) {
                    val path = Path.of(command)
                    val valid = Files.exists(path) && Files.isRegularFile(path) && Files.isExecutable(path)
                    COMMAND_TO_VALIDITY_CACHE[command] = valid
                    return valid
                } else {
                    COMMAND_TO_VALIDITY_CACHE[command] = false
                    return false
                }
            } catch (_: IOException) {
                return false
            }
        }
    }
}
