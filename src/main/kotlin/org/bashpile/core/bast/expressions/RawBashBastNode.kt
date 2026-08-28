package org.bashpile.core.bast.expressions

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/**
 * This is for literal Bash -- not necessarily a Subshell or using Command Substitution.
 * The literal Bash MAY contain a Subshell or Command Substitution though.
 *
 * `b(cd)` becomes `cd`
 */
class RawBashBastNode(children: List<BastNode> = listOf()) : BastNode(children.toMutableList()) {
    override fun render(options: RenderOptions): String {
        return children.joinToString("") { it.render(options.unquoted()) }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): RawBashBastNode {
        return RawBashBastNode(nextChildren.map { it.deepCopy() })
    }
}
