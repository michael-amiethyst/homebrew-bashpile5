package org.bashpile.core.bast.expressions

import org.bashpile.core.Main.Companion.callStack
import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions

class VariableReferenceBastNode(id: String, majorType: TypeEnum) : BastNode(mutableListOf(), id, majorType) {

    override fun render(options: RenderOptions): String {
        callStack.requireOnStack(id!!)
        val dereferenceNeeded = !options.integerArithmeticContext
        val dollarId = if (dereferenceNeeded) { "${'$'}{$id}" } else { id }
        return if (options.quoted) {"\"${dollarId}\""} else { dollarId }
    }

    override fun replaceChildren(nextChildren: List<BastNode>): VariableReferenceBastNode {
        return VariableReferenceBastNode(id!!, majorType())
    }
}
