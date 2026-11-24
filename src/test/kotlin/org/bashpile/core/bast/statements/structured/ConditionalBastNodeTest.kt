package org.bashpile.core.bast.statements.structured

import org.bashpile.core.bast.expressions.literals.StringLiteralBastNode
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionalBastNodeTest {

    @Test
    fun elseBodyAndCommentsAreKeptTogetherWhenNodeIsCopied() {
        val elseBody = listOf(StringLiteralBastNode("else statement"))
        val elseComments = listOf(StringLiteralBastNode("# else comment"))
        val node = ConditionalBastNode(
            IfClause(StringLiteralBastNode("condition"), emptyList(), emptyList()),
            emptyList(),
            elseBody to elseComments
        )

        val copy = node.deepCopy() as ConditionalBastNode

        assertEquals(elseBody, copy.elseClause.first)
        assertEquals(elseComments, copy.elseClause.second)
    }
}
