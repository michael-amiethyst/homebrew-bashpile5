package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.literals.TerminalBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum.STRING

/** See also [org.bashpile.core.bast.statements.structured.SwitchBastNode] */
class CaseBastNode(val expression: List<BastNode>, val statements: MutableList<BastNode>, val comments: List<BastNode>)
    : BastNode((expression + statements) as MutableList<BastNode>)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return CaseBastNode(expression, statements, comments)
    }

    override fun render(options: RenderOptions): String {
        val matcher = expression.joinToString("") { it.render(options) }
        val commentsRender = comments.joinToString(" ", postfix = "\n") { it.render(options) }.ifBlank { "" }
        val matchRender = "$matcher)\n$commentsRender"
        val statementsWithTerminator = statements + TerminalBastNode(";;", STRING)
        val statementBlock: List<String> = statementsWithTerminator.map { it.render(options) }
        return matchRender + statementBlock.joinToString("")
    }
}
