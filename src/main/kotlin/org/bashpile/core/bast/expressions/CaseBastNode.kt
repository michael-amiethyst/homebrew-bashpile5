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
        // TODO factor out the hard-coded 5 -- create test for switch in a block
        val matchRender = "$TAB${expression.render(options)})\n${TAB.repeat(5)}"
        statements.addLast(TerminalBastNode("${TAB.repeat(5)};;", STRING))
        val statementBlock: List<String> = statements.map { it.render(options) }
        return matchRender + statementBlock.joinToString("")
    }
}
