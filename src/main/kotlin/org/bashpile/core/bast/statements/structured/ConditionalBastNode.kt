package org.bashpile.core.bast.statements.structured

import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.statements.StatementBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.RenderOptions.Companion.IGNORE_OUTPUT
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED

/** If-elseif-else */
class ConditionalBastNode(
    // TODO change else to double-record
    val ifClause: IfClause, val elseIfClauses: List<IfClause>,
    val elseBody: List<BastNode>, val elseComments: List<BastNode>
) : StatementBastNode(ifClause.toBastList() + elseIfClauses.flatMap { it.toBastList() } + elseBody, comments = ifClause.comments) {

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return ConditionalBastNode(ifClause, elseIfClauses.map { it.deepCopy() }, elseBody, elseComments)
    }

    override fun render(options: RenderOptions): String {
        val renderedIfBody = callStack.use { stack ->
            stack.pushStackframe()
            ifClause.body.joinToString("\n") { statement ->
                statement.render(UNQUOTED).trimEnd()
            }
        }
        val renderedComments = if (comments.isNotEmpty()) {
            " " + comments.joinToString(" ") { it.render(options) }
        } else { "" }
        val renderedElseBody = if (elseBody.isNotEmpty()) {
            val renderedElseComment = if (elseComments.isNotEmpty()) {
                " " + elseComments.joinToString(" ") { it.render(options) }
            } else { "" }

            val renderedStatements = callStack.use { stack ->
                stack.pushStackframe()
                elseBody.joinToString("\n") {
                    statement -> statement.render(UNQUOTED).trimEnd()
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
                val condition = elseIf.condition.render(IGNORE_OUTPUT)
                val comments = elseIf.comments
                    .joinToString(" ") { it.render(UNQUOTED) }
                    .let { if (it.isBlank()) "" else " $it" }
                val body = elseIf.body.joinToString("\n") { statement ->
                    statement.render(UNQUOTED).trimEnd()
                }

                "\nelif $condition; then$comments\n$body"
            }
        }
        return "if ${ifClause.condition.render(IGNORE_OUTPUT)}; then$renderedComments\n" +
                "$renderedIfBody$renderedElseIfBodies$renderedElseBody\n" +
                "fi\n"
    }
}
