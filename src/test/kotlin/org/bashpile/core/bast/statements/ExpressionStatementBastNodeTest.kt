package org.bashpile.core.bast.statements

import org.bashpile.core.bast.expressions.RawBashBastNode
import org.bashpile.core.bast.expressions.StringConcatenationBastNode
import org.bashpile.core.bast.expressions.literals.StringLiteralBastNode
import org.bashpile.core.bast.expressions.literals.TerminalBastNode
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED
import org.bashpile.core.engine.TypeEnum.STRING
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionStatementBastNodeTest {
    @Test
    fun render_withStringAndRawBash_quotesOnlyString() {
        val concatenation = StringConcatenationBastNode(listOf(
            StringLiteralBastNode("true && "),
            RawBashBastNode(listOf(TerminalBastNode("(true)", STRING)))
        ))
        val fixture = ExpressionStatementBastNode(listOf(concatenation))

        assertEquals(
            "\"true && \"(true)",
            fixture.render(UNQUOTED)
        )
    }
}
