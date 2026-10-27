package org.bashpile.core.bast.expressions.shellstrings

import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.literals.TerminalBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.ShellStringMode
import org.bashpile.core.engine.Subshell
import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.engine.TypeEnum.STRING

/**
 * A Shell String is the Bashpile equivalent of a Bash Command Substitution (i.e., $() syntax).
 * It represents an expression.
 * A full line of bash is a [org.bashpile.core.bast.statements.ShellLineBastNode].
 */
open class ShellStringBastNode(children: List<BastNode> = listOf(), majorType: TypeEnum = STRING)
    : BastNode(children.toMutableList(), majorType = majorType), Subshell
{
    constructor(vararg child: BastNode) : this(child.toList())
    constructor(contents: String) : this(TerminalBastNode(contents, STRING).asList())

    override fun render(options: RenderOptions): String {
        val childRenders = children.joinToString("") { it.render(options) }
        val subshell = when (options.shellStringMode) {
            ShellStringMode.QUIET_COMMAND -> "($childRenders) >/dev/null 2>&1"
            ShellStringMode.CAPTURE -> "$($childRenders)"
            ShellStringMode.CAPTURE_QUIETLY -> {
                // Redirect the whole command group so every command's stderr is suppressed,
                // while command substitution still captures its stdout.
                check(childRenders.isNotBlank())
                val commandGroup = if (!childRenders.contains("[\n;]".toRegex())) {
                    childRenders
                } else { "{\n$childRenders\n}" }
                "$( $commandGroup 2>/dev/null )"
            }
        }
        return if (options.quoted && options.shellStringMode != ShellStringMode.QUIET_COMMAND) {
            "\"$subshell\""
        } else {
            subshell
        }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): ShellStringBastNode {
        return ShellStringBastNode(nextChildren.map { it.deepCopy() }, majorType())
    }

}
