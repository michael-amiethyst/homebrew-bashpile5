package org.bashpile.core.bast

import org.bashpile.core.engine.HolderNode
import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.engine.TypeEnum.UNKNOWN
import org.bashpile.core.engine.RenderOptions


/**
 * For inner nodes in the tree data structure sense.  Used as a "holder node".
 *
 * See [Wikipedia - Internal Node](https://en.wikipedia.org/wiki/Tree_(abstract_data_type)#:~:text=An%20internal%20node)
 */
class InternalBastNode(
    children: List<BastNode> = listOf(), majorType: TypeEnum = UNKNOWN, val renderSeparator: String = "")
    : BastNode(children.toMutableList(), majorType = majorType), HolderNode
{
    /** Convenience constructor */
    constructor(vararg children: BastNode) : this(children.toList())

    override fun render(options: RenderOptions): String {
        return children.joinToString(renderSeparator) { it.render(options) }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): InternalBastNode {
        return InternalBastNode(nextChildren.map { it.deepCopy() }, majorType(), renderSeparator)
    }
}
