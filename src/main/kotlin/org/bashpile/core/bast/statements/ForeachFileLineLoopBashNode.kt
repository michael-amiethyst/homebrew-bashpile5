package org.bashpile.core.bast.statements

import org.bashpile.core.LinuxProcess
import org.bashpile.core.LinuxProcess.Companion.SCRIPT_SUCCESS
import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.VariableReferenceBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.engine.TypeEnum.EMPTY

/**
 * for(FirstName: string, LastName: string in 'src/test/resources/example.csv')
 */
class ForeachFileLineLoopBashNode(
    children: List<BastNode> = listOf(),
    val doubleQuotedFilepath: String,
    val columns: List<VariableReferenceBastNode>,
    comments: List<BastNode> = listOf()) : StatementBastNode(children.toMutableList(), comments = comments)
{
    companion object {
        val sed: String = if (LinuxProcess("which gsed").run().second == SCRIPT_SUCCESS) "gsed" else "sed"
    }

    init {
        require(!columns.map { it.id!! }.any { it.contains("\\s".toRegex()) }) {
            "Whitespace not allowed in column names"
        }
        check(doubleQuotedFilepath.startsWith("\"") || doubleQuotedFilepath.endsWith("\"")) {
            "Filepath should be quoted"
        }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): ForeachFileLineLoopBashNode {
        return ForeachFileLineLoopBashNode(nextChildren.map { it.deepCopy() }, doubleQuotedFilepath, columns)
    }

    override fun render(options: RenderOptions): String {
        // Read each line with `read -r`, if more than 1 column parse each line as a CSV
        callStack.use { stack ->
            stack.pushStackframe()

            columns.forEach {
                callStack.addVariableInfo(it.id!!, it.majorType(), EMPTY, readonly = true)
            }

            val lineVariableName = if (columns.size > 1) { "__bp_line" } else { columns[0].id!! }
            val commentText = if (comments.isNotEmpty()) {
                " " + comments.map { it.render(options) }.joinToString(" ")
            } else { "" }
            val setLoopVariables = if (columns.size > 1) {
                columns.mapIndexed { i, it -> """
                    ${it.id}=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $${i + 1}}');""".trimIndent()
                }.joinToString(prefix = "    # loop variables (Bashpile generated)\n    ", separator = " ", postfix = "\n")
            } else { "" }
            val childRenderList = children.map { child ->
                child.render(RenderOptions.UNQUOTED).lines().filter { it.isNotBlank() }.map {
                    "    $it"
                }.joinToString("\n", postfix = "\n")
            }
            val childRenders = childRenderList.joinToString("").removeSuffix("\n")
            return """
                cat $doubleQuotedFilepath | ${mungeStream()} | while IFS='' read -r $lineVariableName; do$commentText
                $setLoopVariables   # body
                $childRenders
                done
    
            """.trimIndent()
        }
    }

    private fun mungeStream(): String {
        // 1 (line) delete to skip CSV headers
        val skipFirstLine = if (columns.size > 1) "-e '1d' " else ""

        // Convert '\r\n' to '\n'
        val convertWindowsLineEndings = "-e 's/\\r\\n/\\n/g'"

        // -z causes '$' to means EOF, not end of line.
        // for files that do not end with a newline ('/\n$/!s') replace EOF with \n EOF
        val appendTrailingNewline = """
                -ze '/\n$/!s/$/\n$/g'""".trimIndent()

        // need to have two gsed calls due to the -z option
        return "$sed ${skipFirstLine}${convertWindowsLineEndings} | $sed $appendTrailingNewline"
    }
}
