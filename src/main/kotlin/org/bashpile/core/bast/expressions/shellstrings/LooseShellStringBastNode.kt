package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.Subshell

/** [org.bashpile.core.FinishedBastFactory] adds the logic to disable Strict Mode */
class LooseShellStringBastNode(children: List<BastNode> = listOf()) : ShellStringBastNode(children), Subshell {
    override fun replaceChildren(nextChildren: List<BastNode>): LooseShellStringBastNode {
        return LooseShellStringBastNode(nextChildren.map { it.deepCopy() })
    }
}
