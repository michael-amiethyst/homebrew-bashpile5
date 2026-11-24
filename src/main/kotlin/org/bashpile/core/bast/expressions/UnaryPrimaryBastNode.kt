package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** See also [BinaryPrimaryBastNode] */
class UnaryPrimaryBastNode(operator: String, private val rightExpression: BastNode)
    : PrimaryBastNode(null, operator, rightExpression)
{
    override fun render(options: RenderOptions): String {
        val valueOptions = options.asValue()
        val rightRendered = rightExpression.render(valueOptions.quoted())
        val bashOperator = when (operator) {
            // string operators
            "isEmpty" -> "-z"
            "isNotEmpty" -> "-n"
            // file operators
            "exists" -> "-e"
            "doesNotExist" -> "! -e"
            "regularFileExists" -> "-f"
            "directoryExists" -> "-d"
            // for native Bash operators
            else -> operator
        }

        return "[ $bashOperator $rightRendered ]"
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return UnaryPrimaryBastNode(operator, rightExpression.deepCopy())
    }
}
