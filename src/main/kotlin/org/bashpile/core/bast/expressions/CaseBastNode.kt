package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.literals.TerminalBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum.STRING

/** See also [org.bashpile.core.bast.statements.SwitchBastNode] */
class CaseBastNode(val expression: List<BastNode>, val statements: MutableList<BastNode>)
    : BastNode((expression + statements) as MutableList<BastNode>)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return CaseBastNode(expression, statements)
    }

    override fun render(options: RenderOptions): String {
        // this is rendered in the SwitchBastNode with 3 tabs as indents
        val tabs = TAB.repeat(5) // base of 3 tabs, one for the case and one for the statements
        val matcher = expression.map { it.render(options) }.joinToString("")
        val matchRender = "$TAB$matcher)\n"
        statements.addLast(TerminalBastNode(";;", STRING))
        val statementBlock: List<String> = statements.map { tabs + it.render(options) }
        return matchRender + statementBlock.joinToString("")
    }
}
