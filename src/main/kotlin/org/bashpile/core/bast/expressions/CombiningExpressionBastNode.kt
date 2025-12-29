package org.bashpile.core.bast.expressions

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** Combines two boolean expressions with `and` or `or`, also takes literal booleans */
class CombiningExpressionBastNode(private val left: BastNode, private val operator: String, private val right: BastNode)
    : BastNode(mutableListOf(left, right), majorType = TypeEnum.BOOLEAN)
{
    override fun render(options: RenderOptions): String {
        val bashOperator = when(operator) {
            "and" -> "&&"
            "or" -> "||"
            else -> throw IllegalArgumentException("Unknown combining operator: $operator")
        }
        return "${left.render(RenderOptions.IGNORE_OUTPUT)} $bashOperator ${right.render(RenderOptions.IGNORE_OUTPUT)}"
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return CombiningExpressionBastNode(nextChildren[0].deepCopy(), operator, nextChildren[1].deepCopy())
    }
}
