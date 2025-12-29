package org.bashpile.core.bast.expressions.literals

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

class StringLiteralBastNode(val text: String
) : BastNode(mutableListOf(), majorType = TypeEnum.STRING), Literal {
    override fun render(options: RenderOptions): String {
        return if (options.quoted) { "\"$text\"" } else { text }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): StringLiteralBastNode {
        return StringLiteralBastNode(text)
    }
}
