package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.literals.TerminalBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum.STRING

/** See also [org.bashpile.core.bast.statements.SwitchBastNode] */
class CaseBastNode(val expression: BastNode, val statements: MutableList<BastNode>)
    : BastNode((expression.asList() + statements) as MutableList<BastNode>)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return CaseBastNode(expression, statements)
    }

    override fun render(options: RenderOptions): String {
        // this is rendered in the SwitchBastNode with 3 tabs as indents
        val tabs = TAB.repeat(5) // base of 3 tabs, one for the case and one for the statements
        val matchRender = "$TAB${expression.render(options)})\n$tabs"
        statements.addLast(TerminalBastNode("$tabs;;", STRING))
        val statementBlock: List<String> = statements.map { it.render(options) }
        return matchRender + statementBlock.joinToString("")
    }
}
