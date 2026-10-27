package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum

/** See also [UnaryPrimaryBastNode] */
class BinaryPrimaryBastNode(val left: BastNode, operator: String, val right: BastNode)
    : PrimaryBastNode(left, operator, right)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return BinaryPrimaryBastNode(left, operator, right)
    }

    override fun render(options: RenderOptions): String {
        val integers = left.majorType().coercesTo(TypeEnum.INTEGER) && right.majorType().coercesTo(TypeEnum.INTEGER)
        val floats = left.majorType().coercesTo(TypeEnum.FLOAT) && right.majorType().coercesTo(TypeEnum.FLOAT)
        val strings = left.majorType().coercesTo(TypeEnum.STRING) && right.majorType().coercesTo(TypeEnum.STRING)
        require(integers || floats || strings) { "Mismatched types: ${left.majorType()} and ${right.majorType()}" }

        val valueOptions = options.asValue()

        val translatedOperator = if (!integers) {
            operator
        } else {
            when(operator) {
                ">" -> "-gt"
                ">=" -> "-ge"
                "<" -> "-lt"
                "<=" -> "-le"
                "==" -> "-eq"
                "!=" -> "-ne"
                else -> throw IllegalStateException("Unknown operator: $operator")
            }
        }
        return if (integers || strings) {
            val leftRender = left.render(valueOptions.quoted())
            val rightRender = right.render(valueOptions.quoted())
            "[ $leftRender $translatedOperator $rightRender ]"
        } else {
            // floats
            val leftRenders = left.render(valueOptions.unquoted())
            val rightRenders = right.render(valueOptions.unquoted())
            "bc -l <<< \"$leftRenders $translatedOperator $rightRenders\" > /dev/null"
        }
    }
}
