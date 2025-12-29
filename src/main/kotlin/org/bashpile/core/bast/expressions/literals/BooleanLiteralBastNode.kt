package org.bashpile.core.bast.expressions.literals

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/** May be true or false, not null */
class BooleanLiteralBastNode(private val bool: Boolean)
    : BastNode(mutableListOf(), majorType = TypeEnum.BOOLEAN), Literal
{
    override fun render(options: RenderOptions): String {
        return bool.toString()
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BooleanLiteralBastNode {
        return BooleanLiteralBastNode(bool)
    }
}
