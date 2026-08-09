package org.bashpile.core.bast.statements

import com.google.common.collect.Streams.zip
import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.RenderOptions.Companion.IGNORE_OUTPUT
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED
import java.util.stream.Collectors
import java.util.stream.Stream

/** If-elseif-else */
class ConditionalBastNode(
    val conditions: List<BastNode>, val blockBodies: List<List<BastNode>>, val ifComments: List<BastNode> = listOf(),
    val elseBody: List<BastNode>, val elseComments: List<BastNode>
) : StatementBastNode(conditions + blockBodies.flatten() + elseBody, comments = ifComments) {
    init {
        // conditions may only be equal to or one less than blockBodies
        require(conditions.size <= blockBodies.size)
        require(conditions.size >= blockBodies.size - 1)
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return ConditionalBastNode(conditions.map { it.deepCopy() }, blockBodies.map { statements ->
            statements.map { it.deepCopy()}
        }, ifComments, elseBody, elseComments)
    }

    override fun render(options: RenderOptions): String {
        val formattedBodiesRenders = blockBodies.map { block ->
            callStack.use { stack ->
                stack.pushStackframe()
                block.flatMap { statement ->
                    statement.render(UNQUOTED).lines().map { "    $it" }
                }.joinToString("\n").removeSuffix("\n")
            }
        }
        val renderedConditions = conditions.map { it.render(IGNORE_OUTPUT) }
        val renderedIfBody = formattedBodiesRenders.first()
        val renderedComments = if (comments.isNotEmpty()) {
            " " + comments.map { it.render(options) }.joinToString(" ")
        } else { "" }
        val renderedElseBody = if (elseBody.isNotEmpty()) {
            val renderedElseComment = if (elseComments.isNotEmpty()) {
                " " + elseComments.joinToString(" ") { it.render(options) }
            } else ""
            "\nelse$renderedElseComment\n$TAB" + elseBody.joinToString("\n$TAB") { it.render(options) }
        } else ""
        return when (formattedBodiesRenders.size) {
            // TODO now merge our handling of one, two and more
            1 -> { """
                if ${renderedConditions.first()}; then$renderedComments
                $renderedIfBody$renderedElseBody
                fi
                
                """.trimScriptIndent("                ")
            }
            2 -> {
                val bodyStream: Stream<String> = formattedBodiesRenders.subList(1, formattedBodiesRenders.size).stream()
                val conditionStream: Stream<String> = renderedConditions.subList(1, renderedConditions.size).stream()
                val renderedElseIfBodies: String? = zip(
                    conditionStream,
                    bodyStream
                ) { first, second -> "\nelif ${first}; then\n${second}" }.collect(Collectors.joining(" "))
                """
                if ${renderedConditions.first()}; then$renderedComments
                $renderedIfBody$renderedElseIfBodies$renderedElseBody
                fi
                
                """.trimScriptIndent("                ")
            }
            else -> {
                // not first or last
                val elseIfs: List<String> = formattedBodiesRenders.subList(1, formattedBodiesRenders.size - 1)
                val renderedElseIfBlocks: String = elseIfs.mapIndexed { index, it ->
                    // offset by one to skip the initial if condition
                    val renderedElseIfCondition = renderedConditions[index + 1]
                    """
                    elif $renderedElseIfCondition; then
                    $it
                    """.trimScriptIndent("                    ")
                }.joinToString("\n")
                val renderedElseBody = formattedBodiesRenders.last()
                """
                if ${renderedConditions.first()}; then$renderedComments
                $renderedIfBody
                $renderedElseIfBlocks
                else
                $renderedElseBody
                fi
                
                """.trimScriptIndent("                ")
            }
        }
    }
}
