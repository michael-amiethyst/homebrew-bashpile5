package org.bashpile.core.bast.expressions.arithmetic

import org.bashpile.core.engine.TypeEnum.INTEGER
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions


/** See [FloatArithmeticBastNode] */
class IntegerArithmeticBastNode(children: List<BastNode> = listOf())
    : ArithmeticBastNode(children, majorType = INTEGER)
{
    constructor(vararg child: BastNode) : this(child.toList())

    override fun render(options: RenderOptions): String {
        val valueOptions = options.asValue()
        val childRenders = children.map { it.render(valueOptions.integerArithmetic()) }.joinToString(" ")
        return if (!valueOptions.arithmeticContext) { "$(($childRenders))" } else { childRenders }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): IntegerArithmeticBastNode {
        return IntegerArithmeticBastNode(nextChildren.map { it.deepCopy() })
    }
}
