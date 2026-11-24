package org.bashpile.core.antlr

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.bashpile.core.Main
import kotlin.test.Test
import kotlin.test.assertFailsWith


class IndentationStateTest {

    @Test
    fun twoSpaceIndentationParses() {
        parseIndentedBlock("  ")
    }

    @Test
    fun fourSpaceIndentationParses() {
        parseIndentedBlock("    ")
    }

    @Test
    fun tabIndentationParses() {
        parseIndentedBlock("\t")
    }

    @Test
    fun mixedTabAndSpaceIndentationFails() {
        // A tab and eight spaces reach the same column under Python's tab-stop rule,
        // but mixing them for the same block indentation is ambiguous.
        val script = "if (true):\n\tprint(\"tab\")\n        print(\"spaces\")\n"

        assertFailsWith<MixedIndentParseException> {
            Main()._getBast(script.byteInputStream())
        }
    }

    @Test
    fun lineCommentDoesNotAffectIndentationState() {
        assertCommentDoesNotAffectIndentation("// comment")
    }

    @Test
    fun docCommentDoesNotAffectIndentationState() {
        assertCommentDoesNotAffectIndentation("/** doc comment */")
    }

    @Test
    fun nonDocBlockCommentPrefixStillChecksIndentation() {
        val state = IndentationState()
        normalizeNewline(state, "  ", "/* block comment */")

        assertFailsWith<ParseCancellationException> {
            normalizeNewline(state, "\t", "print")
        }
    }

    private fun parseIndentedBlock(indentation: String) {
        val script = "if (true):\n${indentation}print(\"ok\")\n"
        Main()._getBast(script.byteInputStream())
    }

    private fun assertCommentDoesNotAffectIndentation(comment: String) {
        val state = IndentationState()
        normalizeNewline(state, "  ", comment)
        normalizeNewline(state, "\t", "print")
    }

    private fun normalizeNewline(state: IndentationState, indentation: String, nextLine: String) {
        val token = CommonToken(0, "\n$indentation")
        Lexers.normalizeIndentation(token, CharStreams.fromString(nextLine), state)
    }

}
