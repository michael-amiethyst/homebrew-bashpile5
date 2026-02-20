package org.bashpile.core.bast.statements

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** If-elseif-else */
class SwitchBastNode(val matchOn: BastNode, val cases: List<BastNode>)
    : StatementBastNode(listOf(matchOn) + cases)
{
    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return SwitchBastNode(matchOn.deepCopy(), cases.map { it.deepCopy() })
    }

    override fun render(options: RenderOptions): String {
        val matchOnRender = matchOn.render(options)
        val caseRenders = cases.map { it.render(options) }.joinToString("\n" + TAB.repeat(3))
        return """
            case "$matchOnRender" in
            $caseRenders
                    ;;
            esac
        """.trimIndent()
    }
}
