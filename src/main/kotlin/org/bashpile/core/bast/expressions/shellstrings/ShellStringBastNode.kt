package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.literals.TerminalBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.Subshell
import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.engine.TypeEnum.STRING

/**
 * A Shell String is the Bashpile equivalent of a Bash subshell (i.e., $() syntax).  It represents an expression.
 * A full line of bash is a [org.bashpile.core.bast.statements.ShellLineBastNode].
 */
open class ShellStringBastNode(children: List<BastNode> = listOf(), majorType: TypeEnum = STRING)
    : BastNode(children.toMutableList(), majorType = majorType), Subshell
{
    constructor(vararg child: BastNode) : this(child.toList())
    constructor(contents: String) : this(TerminalBastNode(contents, STRING).asList())

    override fun render(options: RenderOptions): String {
        val childRenders = children.map { it.render(options) }.joinToString("")
        val subshell = if (options.ignoreOutput) {
            "($childRenders) >/dev/null 2>&1"
        } else { "$($childRenders)" }
        return if (options.quoted) { "\"$subshell\"" } else { subshell }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): ShellStringBastNode {
        return ShellStringBastNode(nextChildren.map { it.deepCopy() }, majorType())
    }

}
