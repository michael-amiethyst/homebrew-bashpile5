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
        // TODO feature/switch - refactor to avoid need to remove newlines at end, change statementRenders to List<List<String>> and have
        // .joinToString on the list-list
        val statementRenders = statements.map { it.render(options) } + "\n${TAB.repeat(5)};;"
        val matchRender = "$TAB${expression.render(options)})\n${TAB.repeat(5)}"
        val ret = matchRender + statementRenders.joinToString("\n${TAB.repeat(5)}").trimEnd()
        return ret.lines().filter { it.isNotBlank() }.joinToString("\n").trimEnd()
    }
}
