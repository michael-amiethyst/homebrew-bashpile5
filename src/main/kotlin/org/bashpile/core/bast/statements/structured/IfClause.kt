package org.bashpile.core.bast.statements.structured

import org.bashpile.core.bast.BastNode

data class IfClause(val condition: BastNode, val comments: List<BastNode>, val body: List<BastNode>)
