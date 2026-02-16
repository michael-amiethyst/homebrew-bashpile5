package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** See also [org.bashpile.core.bast.statements.SwitchBastNode] */
class CaseBastNode(val expression: BastNode, val statements: List<BastNode>)
    : BastNode((expression.asList() + statements) as MutableList<BastNode>)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return CaseBastNode(expression, statements)
    }

    override fun render(options: RenderOptions): String {
        val statementRenders = statements.map { it.render(options) }
        val matchRender = "    ${expression.render(options)})\n                    "
        return matchRender + statementRenders.joinToString("\n                    ").trimEnd()
    }
}
