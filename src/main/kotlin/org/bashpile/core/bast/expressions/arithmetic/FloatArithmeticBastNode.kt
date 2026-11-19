package org.bashpile.core.bast.expressions.arithmetic

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.Subshell
import org.bashpile.core.engine.TypeEnum.FLOAT

/** See [IntegerArithmeticBastNode]. */
class FloatArithmeticBastNode(children: List<BastNode> = listOf())
    : ArithmeticBastNode(children, majorType = FLOAT), Subshell
{
    constructor(vararg child: BastNode) : this(child.toList())

    override fun render(options: RenderOptions): String {
        val valueOptions = options.asValue()
        val childRenders = children.map { it.render(valueOptions.arithmetic()) }.joinToString(" ")
        val bcSubshell = if (!valueOptions.arithmeticContext) { "$(bc -l <<< \"$childRenders\")" } else { childRenders }
        // output of `bc` may have spaces
        return if (valueOptions.quoted) { "\"$bcSubshell\"" } else { bcSubshell }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): FloatArithmeticBastNode {
        return FloatArithmeticBastNode(nextChildren.map { it.deepCopy() })
    }
}
