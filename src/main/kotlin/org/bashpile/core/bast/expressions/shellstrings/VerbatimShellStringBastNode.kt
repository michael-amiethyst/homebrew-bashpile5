package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.Subshell

/**
 * This is for literal Bash -- not a Subshell or using Command Substitution.
 *
 * `v#(cd)` becomes `cd`
 */
class VerbatimShellStringBastNode(children: List<BastNode> = listOf()) : ShellStringBastNode(children), Subshell {
    override fun render(options: RenderOptions): String {
        return children.map { it.render(options) }.joinToString("")
    }

    override fun replaceChildren(nextChildren: List<BastNode>): VerbatimShellStringBastNode {
        return VerbatimShellStringBastNode(nextChildren.map { it.deepCopy() })
    }
}
