package org.bashpile.core.bast.statements

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** Renders a Bashpile expression as a statement while preserving value syntax such as string quotes. */
class ExpressionStatementBastNode(children: List<BastNode>) : StatementBastNode(children) {
    init {
        require(children.isNotEmpty()) { "An expression statement requires an expression" }
    }

    override fun render(options: RenderOptions): String {
        val expressionRender = children.first().render(options.quoted())
        val trailingRender = children.drop(1).joinToString("") { it.render(options.unquoted()) }
        return expressionRender + trailingRender
    }

    override fun replaceChildren(nextChildren: List<BastNode>): ExpressionStatementBastNode {
        return ExpressionStatementBastNode(nextChildren.map { it.deepCopy() })
    }
}
