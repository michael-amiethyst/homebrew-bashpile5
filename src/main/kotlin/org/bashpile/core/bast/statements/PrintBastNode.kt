package org.bashpile.core.bast.statements

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.VariableReferenceBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum.*

/** This is a Print Statement node */
class PrintBastNode(children: List<BastNode> = listOf()) : StatementBastNode(children) {

    constructor(vararg child: BastNode) : this(child.toList())

    /** Combines all children into a single string as a pre-computation for Bash */
    override fun render(options: RenderOptions): String {
        val printfArguments = mutableListOf<String>()
        val childRenders = children.map {
            val render = it.render(RenderOptions.UNQUOTED)
            val notNumeric = !immediateImportantDescendants().areNumbers()
            if (it !is VariableReferenceBastNode && notNumeric) {
                render
            } else {
                printfArguments.add(render)
                // print numbers as strings to avoid a bug with negative numbers
                "%s"
            }
        }.joinToString("")

        val renderedArguments = printfArguments.joinToString("") { " \"$it\"" }
        return "printf \"$childRenders\"$renderedArguments\n"
    }

    override fun replaceChildren(nextChildren: List<BastNode>): PrintBastNode {
        return PrintBastNode(nextChildren.map { it.deepCopy() })
    }

    private fun List<BastNode>.areNumbers(): Boolean {
        val isInteger = any { it.majorType() == INTEGER } &&
                // will only be integer if all coerce to integers
                map { it.majorType() }.fold(UNKNOWN) { acc, n -> acc.fold(n) } == INTEGER

        val isFloat = any { it.majorType() == FLOAT } &&
                map { it.majorType() }.fold(UNKNOWN) { acc, n -> acc.fold(n) } == FLOAT
        return isInteger || isFloat
    }
}
