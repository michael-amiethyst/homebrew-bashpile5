package org.bashpile.core

import ch.qos.logback.classic.Level.DEBUG
import ch.qos.logback.classic.Level.INFO
import ch.qos.logback.classic.LoggerContext
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.optionalValue
import com.github.ajalt.clikt.parameters.options.versionOption
import com.google.common.annotations.VisibleForTesting
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.logging.log4j.LogManager
import org.bashpile.core.antlr.AstConvertingVisitor
import org.bashpile.core.antlr.ThrowingErrorListener
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.CallStack
import org.bashpile.core.engine.RenderOptions
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path


fun main(args: Array<String>) = Main().main(args)

/**
 * Main entry point for the Bashpile compiler.
 * This is a simple command line interface (CLI) that takes a Bashpile script as the input and outputs a Bash script.
 * This class is primarily responsible for parsing command line arguments.
 * See `SystemTest` in `src/intTest/kotlin` for systems integration tests.
 */
class Main : CliktCommand() {

    companion object {
        const val VERBOSE_ENABLED_MESSAGE = "Double verbose (DEBUG) logging enabled"
        /** As in source/sink -> generates a startup message given a script filename */
        const val STARTUP_MESSAGE = "Running Bashpile compiler with script: "
        const val VERSION = "0.19.0"
        const val SHEBANG_HEADER = "#!/usr/bin/env bash\n\n"
        /** Singleton per Main() instance */
        lateinit var callStack: CallStack
    }

    /** Uses a backing field and a getter because setting in init block not supported by Clikt */
    private val scriptArgument: String
        get() = _scriptArgument ?: ""

    /** Backs [scriptArgument] */
    private val _scriptArgument: String? by argument(help = "The script to compile").optional()

    private val verboseOption: Int by option("-v", "--verbose",
        help = "Show more logs, may be specified twice with -vv").counted(limit=2, clamp=true)

    private val commandOption: String
        get() = _commandOption ?: ""

    /** Backs [commandOption] */
    private val _commandOption: String? by option("-c", "--command",
        help = "Runs the literal command or from STDIN if no argument").optionalValue("")

    private val logger = LogManager.getLogger(Main::javaClass)

    init {
        versionOption(VERSION, names = setOf("--version"), help = "Show the application version and exit.",
            message = { it })
        callStack = CallStack()
    }

    /**
     * The main entry point for the Bashpile compiler.
     * This method is called by the `main` function by Clikt.
     */
    override fun run() {
        // guard, etc
        val scriptPath = Path.of(scriptArgument)
        val badPath = !Files.exists(scriptPath) || !Files.isRegularFile(scriptPath)
        val badCommand = commandOption.isEmpty() && System.`in`.available() == 0
        if (badPath && badCommand) {
            throw PrintHelpMessage(this.currentContext, true, SCRIPT_ERROR__GENERIC)
        }

        // configure logging
        if (verboseOption > 0) {
            configureLogging(verboseOption)
        }
        logger.info(STARTUP_MESSAGE + scriptArgument) // first logging call after configureLogging() call

        // get and render the BAST tree
        val script: String = if (!commandMode()) {
            Files.readString(scriptPath).stripShebang()
        } else commandOption.ifEmpty {
            // read all stdin lines
            generateSequence(::readLine).joinToString("\n")
        }
        val bastRoot: BastNode = _getBast(script.byteInputStream())
        val bash: String = bastRoot.render(RenderOptions.UNQUOTED)
        if (!commandMode()) {
            logger.debug("Writing to STDOUT in regular (non-command) mode")
            echo(SHEBANG_HEADER + bash, false)
        } else {
            logger.debug("Executing compiled command mode script")
            val commandResults = bash.runCommand()
            check(commandResults.second == SCRIPT_SUCCESS) {
                "Compile failed with return code ${commandResults.second} and text:\n${commandResults.first}"
            }
            logger.trace("Writing to STDOUT in command mode")
            echo(commandResults.first)
        }

        endProgram()
    }

    private fun commandMode(): Boolean = scriptArgument.isEmpty()

    /** The initial shebang line isn't part of the Bashpile script. */
    private fun String.stripShebang(): String {
        val shebang = "#!"
        return if (this.startsWith(shebang)) {
            this.stripFirstLine()
        } else {
            this
        }
    }

    /** Invokes ANTLR to parse the script and convert it to a Bashpile AST (BAST) */
    @VisibleForTesting
    @Suppress("functionName")
    internal fun _getBast(stream: InputStream): BastNode {
        // setup lexer
        val charStream = stream.use { CharStreams.fromStream(it) }
        val lexer = BashpileLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener())

        // setup parser
        val tokens = CommonTokenStream(lexer)
        val parser = BashpileParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener())

        // handle ASTs and render
        val antlrAst = parser.program()
        val bast = AstConvertingVisitor().visitProgram(antlrAst)
        return FinishedBastFactory().transform(bast)
    }

    /**
     * Defaults in src/main/resources/logback.xml
     *
     * We went with Logback because Log4j wasn't compatible with Graal nativeCompile.
     */
    private fun configureLogging(verbosity: Int) {
        val logbackLevel = when (verbosity) {
            1 -> INFO
            2 -> DEBUG
            else -> throw IllegalStateException("Unexpected verbosity level: $verbosity")
        }
        val context: LoggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        context.getLogger("org.bashpile").level = logbackLevel
        logger.debug(VERBOSE_ENABLED_MESSAGE)
    }

    /**
     * We need to explicitly exit to avoid some Clikt hangs with nested [String.runCommand] calls.
     */
    private fun endProgram() {
        logger.trace("Exiting Bashpile")
        // Clikt calls System.exit(0) when it catches this "error"
        throw CliktError(null, null, SCRIPT_SUCCESS, false)
    }
}
