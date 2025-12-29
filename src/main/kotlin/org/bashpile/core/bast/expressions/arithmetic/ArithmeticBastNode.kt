package org.bashpile.core.bast.expressions.arithmetic

import org.bashpile.core.engine.TypeEnum
import org.bashpile.core.bast.BastNode

abstract class ArithmeticBastNode(children: List<BastNode> = listOf(), majorType: TypeEnum)
    : BastNode(children.toMutableList(), majorType = majorType)
