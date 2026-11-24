package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.antlr.AstConvertingVisitor.Companion.OLD_OPTIONS
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.Subshell

class LooseShellStringBastNode(children: List<BastNode> = listOf()) : ShellStringBastNode(children), Subshell {
    override fun render(options: RenderOptions): String {
        val strictRender = super.render(options.unquoted())
        val regex = "^(\\$?\\()".toRegex() // match start of string, possibly a '$' and a '('
        val looseRender = strictRender.replaceFirst(regex, "$1eval \"\\$$OLD_OPTIONS\"; ")
        return if (options.quoted) { "\"$looseRender\"" } else { looseRender }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): LooseShellStringBastNode {
        return LooseShellStringBastNode(nextChildren.map { it.deepCopy() })
    }
}
