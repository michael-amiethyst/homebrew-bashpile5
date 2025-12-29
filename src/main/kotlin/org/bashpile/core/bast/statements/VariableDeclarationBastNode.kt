package org.bashpile.core.bast.statements

import org.bashpile.core.Main
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.engine.RenderOptions

/** Created by [org.bashpile.core.antlr.AstConvertingVisitor.visitVariableDeclarationStatement] */
class VariableDeclarationBastNode(
    id: String,
    type: TypeEnum,
    val subtype: TypeEnum = TypeEnum.UNKNOWN,
    val readonly: Boolean = false,
    val export: Boolean = false,
    child: BastNode
) : StatementBastNode(child, id, type)
{
    override fun render(options: RenderOptions): String {
        Main.Companion.callStack.addVariableInfo(id!!, majorType(), subtype, readonly)
        var exportFlags = ""
        if (export) { exportFlags += "x" }
        val flags = if (exportFlags.isNotEmpty()) "-$exportFlags " else ""
        val childRender = children[0].render(options.quoted())
        return """
            declare $flags$id
            $id=$childRender

        """.trimIndent()
    }

    override fun replaceChildren(nextChildren: List<BastNode>): VariableDeclarationBastNode {
        return VariableDeclarationBastNode(id!!, majorType(), subtype, readonly, export, nextChildren[0].deepCopy())
    }
}
