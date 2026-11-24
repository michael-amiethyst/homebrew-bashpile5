package org.bashpile.core.antlr

import org.antlr.v4.runtime.misc.ParseCancellationException

/** Tracks indentation levels for one Bashpile lexer instance. */
class IndentationState {
    private val normalizedIndentationColumns = ArrayDeque(listOf(0))
    private val rawIndentationColumns = ArrayDeque(listOf(0))

    /**
     * @param normalizedColumn Tabs have already been converted to whitespaces (e.g. a \t is typically 4 or 8 wide)
     * @param rawColumn The column width without tab expansion (e.g. \t\t is 2 instead of 8 or 16)
     */
    internal fun update(normalizedColumn: Int, rawColumn: Int, line: Int) {
        val prevNormalizedColumn = normalizedIndentationColumns.last()
        val prevRawColumn = rawIndentationColumns.last()
        val ex = "Indent column mismatch at $line"

        if (normalizedColumn > prevNormalizedColumn) { // if indenting
            parseAssert(MixedIndentParseException(ex)) { rawColumn >= prevRawColumn }
            normalizedIndentationColumns.addLast(normalizedColumn)
            rawIndentationColumns.addLast(rawColumn)
        } else if (normalizedColumn == prevNormalizedColumn) { // if no indent changes
            parseAssert(MixedIndentParseException(ex)) { rawColumn == prevRawColumn }
        } else { // if dedenting
            // clear dedented column data
            while (normalizedColumn < normalizedIndentationColumns.last()) {
                // drop last column and recheck
                normalizedIndentationColumns.removeLast()
                rawIndentationColumns.removeLast()
            }

            parseAssert(MixedIndentParseException(ex)) {
                normalizedColumn == normalizedIndentationColumns.last()
            }

            parseAssert(ParseCancellationException("Parse Error: $line")) {
                rawColumn == rawIndentationColumns.last()
            }
        }
    }

    private fun parseAssert(throwable: Throwable, test: () -> Boolean) = if (!test()) { throw throwable } else {}
}
