package org.bashpile.core.bast.statements.structured

import com.google.common.collect.Streams.zip
import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.statements.StatementBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.RenderOptions.Companion.IGNORE_OUTPUT
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED
import java.util.stream.Collectors

/** If-elseif-else */
class ConditionalBastNode(
    val ifClause: IfClause, val conditions: List<BastNode>, val blockBodies: List<List<BastNode>>,
    val elseBody: List<BastNode>, val elseComments: List<BastNode>
) : StatementBastNode(conditions + blockBodies.flatten() + elseBody, comments = ifClause.comments) {
    init {
        // conditions may only be equal to or one less than blockBodies
        require(conditions.size <= blockBodies.size)
        require(conditions.size >= blockBodies.size - 1)
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return ConditionalBastNode(ifClause, conditions.map { it.deepCopy() }, blockBodies.map { statements ->
            statements.map { it.deepCopy()}
        }, elseBody, elseComments)
    }

    override fun render(options: RenderOptions): String {
        val formattedBodiesRenders = blockBodies.map { block ->
            callStack.use { stack ->
                stack.pushStackframe()

                block.joinToString("\n") { statement ->
                    statement.render(UNQUOTED).trimEnd().prependIndent(TAB)
                }
            }
        }
        val renderedConditions = conditions.map { it.render(IGNORE_OUTPUT) }
        val renderedIfBody = callStack.use { stack ->
            stack.pushStackframe()

            ifClause.body.joinToString("\n") { statement ->
                statement.render(UNQUOTED).trimEnd().prependIndent(TAB)
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

                elseBody.joinToString("\n") { statement ->
                    statement.render(UNQUOTED)
                        .trimEnd()
                        .prependIndent(TAB)
                }
            }

            "\nelse$renderedElseComment\n$renderedStatements"
        } else {
            ""
        }

        // final render
        val renderedElseIfBodies: String = zip(
            renderedConditions.stream(),
            formattedBodiesRenders.stream()
        ) {
            first, second -> "\nelif ${first}; then\n${second}"
        }.collect(Collectors.joining(" ")).removeSuffix("\n$TAB")
        return "if ${ifClause.condition.render(IGNORE_OUTPUT)}; then$renderedComments\n" +
                "$renderedIfBody$renderedElseIfBodies$renderedElseBody\n" +
                "fi\n"
    }
}
