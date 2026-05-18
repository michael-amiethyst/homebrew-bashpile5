package org.bashpile.core.bast.statements

import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.RenderOptions.Companion.IGNORE_OUTPUT
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED

/** If-elseif-else */
class ConditionalBastNode(
    val conditions: List<BastNode>, val blockBodies: List<List<BastNode>>, comments: List<BastNode> = listOf()
) : StatementBastNode(conditions + blockBodies.flatMap { it }, comments = comments) {
    init {
        // conditions may only be equal to or one less than blockBodies
        require(conditions.size <= blockBodies.size)
        require(conditions.size >= blockBodies.size - 1)
    }

    override fun replaceChildren(nextChildren: List<BastNode>): BastNode {
        return ConditionalBastNode(conditions.map { it.deepCopy() }, blockBodies.map { statements ->
            statements.map { it.deepCopy()}
        })
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
        return when (formattedBodiesRenders.size) {
            1 -> { """
                if ${renderedConditions.first()}; then$renderedComments
                $renderedIfBody
                fi
                
                """.trimScriptIndent("                ")
            }
            2 -> {
                val renderedElseBody = formattedBodiesRenders.last()
                """
                if ${renderedConditions.first()}; then$renderedComments
                $renderedIfBody
                else
                $renderedElseBody
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
