package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

/**
 * This is for literal Bash -- not a Subshell or using Command Substitution.
 *
 * `b(cd)` becomes `cd`
 */
// TODO subclass BastNode directly
class RawBashBastNode(children: List<BastNode> = listOf()) : ShellStringBastNode(children) {
    override fun render(options: RenderOptions): String {
        return children.joinToString("") { it.render(options) }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): RawBashBastNode {
        return RawBashBastNode(nextChildren.map { it.deepCopy() })
    }
}
