package org.bashpile.core.bast.expressions.literals

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode

class OpeningParenthesisTerminalBastNode : TerminalBastNode("(", TypeEnum.STRING), Literal {
    override fun replaceChildren(nextChildren: List<BastNode>): OpeningParenthesisTerminalBastNode {
        return OpeningParenthesisTerminalBastNode()
    }
}
