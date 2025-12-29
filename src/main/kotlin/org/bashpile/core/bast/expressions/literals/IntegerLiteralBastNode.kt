package org.bashpile.core.bast.expressions.literals

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode
import org.bashpile.core.engine.RenderOptions
import java.math.BigInteger

/** Represents an integer of any size */
class IntegerLiteralBastNode(private val bigInt: BigInteger)
    : BastNode(mutableListOf(), majorType = TypeEnum.INTEGER), Literal
{
    override fun render(options: RenderOptions): String {
        val toString = bigInt.toString()
        val noWhitespace = "\\s".toRegex().find(toString) == null
        check (noWhitespace)
        return toString
    }

    override fun replaceChildren(nextChildren: List<BastNode>): IntegerLiteralBastNode {
        return IntegerLiteralBastNode(bigInt)
    }
}
