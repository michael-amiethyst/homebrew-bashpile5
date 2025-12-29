package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.Subshell

// TODO 0.20.0 - render should surround with "set +euo pipefail/set -euo pipefail"
class LooseShellStringBastNode(children: List<BastNode> = listOf()) : ShellStringBastNode(children), Subshell {
    override fun replaceChildren(nextChildren: List<BastNode>): LooseShellStringBastNode {
        return LooseShellStringBastNode(nextChildren.map { it.deepCopy() })
    }
}
