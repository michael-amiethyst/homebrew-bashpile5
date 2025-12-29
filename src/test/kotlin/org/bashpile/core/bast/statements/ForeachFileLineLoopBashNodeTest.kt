package org.bashpile.core.bast.statements

import org.bashpile.core.engine.TypeEnum.EMPTY
import org.bashpile.core.engine.TypeEnum.STRING
import org.bashpile.core.bast.expressions.VariableReferenceBastNode
import org.bashpile.core.bast.expressions.literals.StringLiteralBastNode
import org.bashpile.core.bast.statements.ForeachFileLineLoopBashNode.Companion.sed
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED
import kotlin.test.Test
import kotlin.test.assertEquals

class ForeachFileLineLoopBashNodeTest {
    @Test
    fun render_withPrint_works() {
        val child = PrintBastNode(VariableReferenceBastNode("col1", STRING))
        val fixture = ForeachFileLineLoopBashNode(
            child.asList(),"\"file.csv\"", listOf(VariableReferenceBastNode("col1", STRING)))
        assertEquals(
            """
            cat "file.csv" | $sed -e '1d' -e 's/\r//g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS=',' read -r col1; do
                printf "%s" "${'$'}{col1}"
            done

        """.trimIndent(), fixture.render(UNQUOTED))
    }

    /** Statement in block needs 2 Bash lines to render */
    @Test
    fun render_withVariableDeclaration_works() {
        val child = VariableDeclarationBastNode(
            "col1", STRING, EMPTY, child = StringLiteralBastNode("exampleValue"))
        val fixture = ForeachFileLineLoopBashNode(
            child.asList(),"\"file.csv\"", listOf(VariableReferenceBastNode("col1", STRING)))
        assertEquals(
            """
            cat "file.csv" | $sed -e '1d' -e 's/\r//g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS=',' read -r col1; do
                declare col1
                col1="exampleValue"
            done

        """.trimIndent(), fixture.render(UNQUOTED))
    }
}
