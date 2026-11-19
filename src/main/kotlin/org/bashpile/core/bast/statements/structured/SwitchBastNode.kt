package org.bashpile.core.bast.statements.structured

import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.statements.StatementBastNode
import org.bashpile.core.engine.RenderOptions

/**
 * A Bash `case` statement with Bashpile lexical scoping.
 *
 * [matchOn] is rendered in the surrounding scope. Each entry in [cases], including the default case, is rendered in
 * a separate lexical stack frame. Declarations remain available to later statements in the same case, but are not
 * visible from sibling cases or after this switch. Declarations from the surrounding scope remain visible in every
 * case.
 */
class SwitchBastNode(val matchOn: BastNode, val cases: List<BastNode>, val expressionComments: List<BastNode>)
    : StatementBastNode(listOf(matchOn) + cases, comments = expressionComments)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return SwitchBastNode(matchOn.deepCopy(), cases.map { it.deepCopy() }, comments)
    }

    override fun render(options: RenderOptions): String {
        val matchOnRender = matchOn.render(options)
        val commentsRender = expressionComments.joinToString(" ") { it.render(options) }
        val caseRenders = cases.joinToString("\n") { case ->
            callStack.use { stack ->
                stack.pushStackframe()
                case.render(options)
            }
        }
        return """
            case "$matchOnRender" in $commentsRender
            $caseRenders
            esac
            
        """.trimIndent()
    }
}
