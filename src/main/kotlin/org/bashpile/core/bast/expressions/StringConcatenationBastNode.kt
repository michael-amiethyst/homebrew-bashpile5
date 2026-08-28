package org.bashpile.core.bast.expressions

import org.bashpile.core.engine.TypeEnum.STRING
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

class StringConcatenationBastNode(children: List<BastNode>)
    : BastNode(children.toMutableList(), majorType = STRING)
{
    // TODO check if other option overrides should be modifications instead
    override fun render(options: RenderOptions): String {
        val childRenders = children.joinToString("") { it.render(options.unquoted()) }
        return if (options.quoted) {
            """
                "$childRenders"
            """.trimIndent()
        } else { childRenders }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): StringConcatenationBastNode {
        return StringConcatenationBastNode(nextChildren.map { it.deepCopy() })
    }
}
