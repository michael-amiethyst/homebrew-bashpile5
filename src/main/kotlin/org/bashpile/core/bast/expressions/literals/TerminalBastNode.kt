package org.bashpile.core.bast.expressions.literals

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** Created by [org.bashpile.core.antlr.AstConvertingVisitor.visitTerminal] */
open class TerminalBastNode(private val text: String, majorType: TypeEnum)
    : BastNode(mutableListOf(), majorType = majorType), Literal
{
    override fun render(options: RenderOptions): String {
        return text
    }

    override fun replaceChildren(nextChildren: List<BastNode>): TerminalBastNode {
        return TerminalBastNode(text, majorType())
    }

    fun isAddition(): Boolean {
        // Should be the same symbol as for BashpileLexer.Add
        return text == "+"
    }
}
