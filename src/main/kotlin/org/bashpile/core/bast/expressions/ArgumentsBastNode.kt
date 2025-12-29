package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum

class ArgumentsBastNode(val brackedText: String = "", val all: Boolean = false, val splat: Boolean = false)
    : BastNode(mutableListOf(), majorType = TypeEnum.UNKNOWN)
{
    override fun render(options: RenderOptions): String {
        return if (brackedText != "") {
            "$$brackedText"
        } else if (all) {
            "$@"
        } else if (splat) {
            "$*"
        } else {
            throw UnsupportedOperationException("Must have bracked text set or a flag as true")
        }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return ArgumentsBastNode(brackedText, all, splat)
    }
}
