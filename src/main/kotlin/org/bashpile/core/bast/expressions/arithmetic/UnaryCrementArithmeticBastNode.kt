package org.bashpile.core.bast.expressions.arithmetic

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.literals.Literal
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.RenderOptions.Companion.INTEGER_ARITHMETIC
import org.bashpile.core.engine.TypeEnum.INTEGER

/** For Pre/Post increment/decrement operations.  E.g. ++2, i++, --i and i-- */
class UnaryCrementArithmeticBastNode(
    private val expressionNode: BastNode,
    private val operator: String,
    /** Is this a preincrement or predecrement? */
    private val precrement: Boolean = false
) : ArithmeticBastNode(
    mutableListOf(expressionNode),
    majorType = INTEGER)
{
    override fun render(options: RenderOptions): String {
        check (expressionNode.coercesTo(INTEGER)) {
            "Post-increment/decrement can only be applied to integers, not ${expressionNode.majorType()}"
        }
        check (expressionNode !is Literal) { "Literals cannot be incremented or decremented" }
        val childRender = expressionNode.render(INTEGER_ARITHMETIC)
        val innerText = if (precrement) { "${operator}$childRender"} else { "${childRender}$operator" }
        return "$(($innerText))"
    }

    override fun replaceChildren(nextChildren: List<BastNode>): UnaryCrementArithmeticBastNode {
        return UnaryCrementArithmeticBastNode(nextChildren[0].deepCopy(), operator, precrement)
    }
}
