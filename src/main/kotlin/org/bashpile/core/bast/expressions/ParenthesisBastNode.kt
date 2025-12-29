package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.expressions.arithmetic.ArithmeticBastNode
import org.bashpile.core.engine.HolderNode
import org.bashpile.core.engine.RenderOptions

class ParenthesisBastNode(children: List<BastNode> = listOf(), majorType: TypeEnum = TypeEnum.UNKNOWN)
    : BastNode(children.toMutableList(), majorType = majorType), HolderNode {

    override fun render(options: RenderOptions): String {
        val childRenders = children.map { it.render(options) }.joinToString(" ")
        return if (parents().any { it is ArithmeticBastNode }) {
            "($childRenders)"
        } else {
            childRenders
        }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): ParenthesisBastNode {
        return ParenthesisBastNode(nextChildren.map { it.deepCopy() })
    }
}
