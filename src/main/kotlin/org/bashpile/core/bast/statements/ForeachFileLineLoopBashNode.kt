package org.bashpile.core.bast.statements

import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.SCRIPT_SUCCESS
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.TypeEnum.EMPTY
import org.bashpile.core.bast.expressions.VariableReferenceBastNode
import org.bashpile.core.engine.RenderOptions
import org.bashpile.core.runCommand

/**
 * for(FirstName: string, LastName: string in 'src/test/resources/example.csv')
 */
class ForeachFileLineLoopBashNode(
    children: List<BastNode> = listOf(),
    val doubleQuotedFilepath: String,
    val columns: List<VariableReferenceBastNode>) : StatementBastNode(children.toMutableList())
{
    companion object {
        val sed: String = if ("which gsed".runCommand().second == SCRIPT_SUCCESS) "gsed" else "sed"
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
        callStack.use { stack ->
            stack.pushStackframe()

            columns.forEach {
                callStack.addVariableInfo(it.id!!, it.majorType(), EMPTY, readonly = true)
            }

            val columnNamesJoined = columns.map { it.id }.joinToString(" ")
            val childRenderList = children.map { child ->
                child.render(RenderOptions.UNQUOTED).lines().filter { it.isNotBlank() }.map {
                    "    $it"
                }.joinToString("\n", postfix = "\n")
            }
            val childRenders = childRenderList.joinToString("").removeSuffix("\n")
            val ifs = if (doubleQuotedFilepath.endsWith(".csv\"")) "," else ""
            return """
                cat $doubleQuotedFilepath | ${mungeStream()} | while IFS='$ifs' read -r $columnNamesJoined; do
                $childRenders
                done
    
            """.trimScriptIndent("                ")
        }
    }

    private fun mungeStream(): String {
        // 1 (line) delete
        val skipFirstLine = if (doubleQuotedFilepath.endsWith(".csv\"")) "-e '1d' " else ""
        // remove all '\r'
        val convertWindowsLineEndings = "-e 's/\\r//g'"

        // -z causes '$' to means EOF, not end of line.
        // for files that do not end with a newline ('/\n$/!s') replace EOF with \n EOF
        val appendTrailingNewline = """
                -ze '/\n$/!s/$/\n$/g'""".trimIndent()

        // need to have two gsed calls due to the -z option
        return "$sed ${skipFirstLine}${convertWindowsLineEndings} | $sed $appendTrailingNewline"
    }
}
