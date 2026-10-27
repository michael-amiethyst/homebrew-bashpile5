package org.bashpile.core.bast.statements.structured

import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.statements.StatementBastNode
import org.bashpile.core.engine.RenderOptions

/** If-elseif-else */
class ConditionalBastNode(
    val ifClause: IfClause, val elseIfClauses: List<IfClause>,
    /** The else body and its leading comments, respectively. */
    val elseClause: Pair<List<BastNode>, List<BastNode>>
) : StatementBastNode(ifClause.toBastList() + elseIfClauses.flatMap { it.toBastList() } + elseClause.first, comments = ifClause.comments) {

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return ConditionalBastNode(ifClause, elseIfClauses.map { it.deepCopy() }, elseClause)
    }

    override fun render(options: RenderOptions): String {
        val renderedIfBody = callStack.use { stack ->
            stack.pushStackframe()
            ifClause.body.joinToString("\n") { statement ->
                statement.render(options.unquoted()).trimEnd()
            }
        }
        val renderedComments = if (comments.isNotEmpty()) {
            " " + comments.joinToString(" ") { it.render(options) }
        } else { "" }
        val (elseBody, elseComments) = elseClause
        val renderedElseBody = if (elseBody.isNotEmpty()) {
            val renderedElseComment = if (elseComments.isNotEmpty()) {
                " " + elseComments.joinToString(" ") { it.render(options) }
            } else { "" }

            val renderedStatements = callStack.use { stack ->
                stack.pushStackframe()
                elseBody.joinToString("\n") {
                    statement -> statement.render(options.unquoted()).trimEnd()
                }
            }

            "\nelse$renderedElseComment\n$renderedStatements"
        } else {
            ""
        }

        // final render
        val renderedElseIfBodies = elseIfClauses.joinToString("") { elseIf ->
            callStack.use { stack ->
                stack.pushStackframe()
                val condition = elseIf.condition.render(options.ignoreOutput())
                val comments = elseIf.comments
                    .joinToString(" ") { it.render(options.unquoted()) }
                    .let { if (it.isBlank()) "" else " $it" }
                val body = elseIf.body.joinToString("\n") { statement ->
                    statement.render(options.unquoted()).trimEnd()
                }

                "\nelif $condition; then$comments\n$body"
            }
        }
        return "if ${ifClause.condition.render(options.ignoreOutput())}; then$renderedComments\n" +
                "$renderedIfBody$renderedElseIfBodies$renderedElseBody\n" +
                "fi\n"
    }
}
