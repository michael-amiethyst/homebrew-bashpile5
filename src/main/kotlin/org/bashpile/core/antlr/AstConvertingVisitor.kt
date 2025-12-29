package org.bashpile.core.antlr

import org.antlr.v4.runtime.tree.TerminalNode
import org.bashpile.core.BashpileLexer
import org.bashpile.core.BashpileParser
import org.bashpile.core.BashpileParserBaseVisitor
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.InternalBastNode
import org.bashpile.core.bast.expressions.*
import org.bashpile.core.bast.expressions.arithmetic.FloatArithmeticBastNode
import org.bashpile.core.bast.expressions.arithmetic.IntegerArithmeticBastNode
import org.bashpile.core.bast.expressions.arithmetic.UnaryCrementArithmeticBastNode
import org.bashpile.core.bast.expressions.literals.*
import org.bashpile.core.bast.expressions.shellstrings.LooseShellStringBastNode
import org.bashpile.core.bast.expressions.shellstrings.ShellStringBastNode
import org.bashpile.core.bast.expressions.shellstrings.VerbatimShellStringBastNode
import org.bashpile.core.bast.statements.*
import org.bashpile.core.engine.TypeEnum.*

/**
 * Converts Antlr AST (AAST) to Bashpile AST (BAST).
 * Holds minimal logic; most logic is in the BAST nodes.
 * Created by [org.bashpile.core.Main].
 * Is in two major sections - Statements and Expressions.
 * Law of Demeter relaxed to two calls deep (extension methods are just used once or twice)
 * Code is arranged from complex at the top to simple at the bottom.
 */
class AstConvertingVisitor: BashpileParserBaseVisitor<BastNode>() {

    companion object {
        const val OLD_OPTIONS = "__bp_old_options"

        /** See [Unofficial Bash Strict Mode](http://redsymbol.net/articles/unofficial-bash-strict-mode/) */
        const val ENABLE_STRICT = "set -euo pipefail"

        @JvmStatic
        val STRICT_HEADER = """
            declare -i s
            trap 's=$?; echo "Error (exit code ${'$'}s) found on line ${'$'}LINENO of generated Bash.\
              Command was: ${'$'}BASH_COMMAND"; exit ${'$'}s' ERR
            declare $OLD_OPTIONS
            $OLD_OPTIONS=$(set +o)
            $ENABLE_STRICT

        """.trimIndent()
    }

    override fun visitProgram(ctx: BashpileParser.ProgramContext): BastNode {
        val statementNodes = ShellLineBastNode(STRICT_HEADER).asList() + ctx.children.map { visit(it) }
        return InternalBastNode(statementNodes)
    }

    override fun visitShellLineStatement(ctx: BashpileParser.ShellLineStatementContext): BastNode {
        return ShellLineBastNode(ctx.children.map { visit(it) })
    }

    override fun visitForeachFileLineLoopStatement(ctx: BashpileParser.ForeachFileLineLoopStatementContext): BastNode {
        val antlrStatements = ctx.indentedStatements().statement()
        val children = antlrStatements.map { visit(it) }
        val columns = ctx.typedId().map { visit(it) as VariableReferenceBastNode }
        return ForeachFileLineLoopBashNode(children, ctx.StringValues().text, columns)
    }

    override fun visitConditionalStatement(ctx: BashpileParser.ConditionalStatementContext): BastNode {
        val conditions = mutableListOf(visit(ctx.expression()))
        val blockBodies = ctx.indentedStatements().map { it.statement().map { visit(it) } }.toMutableList()
        ctx.elseIfClauses().forEach { elseIf ->
            conditions += visit(elseIf.expression())
            val insertIndex = if (blockBodies.size >= 2) blockBodies.size - 1 else blockBodies.size
            blockBodies.add(insertIndex, elseIf.indentedStatements().statement().map { visit(it) })
        }
        return ConditionalBastNode(conditions, blockBodies)
    }

    override fun visitVariableDeclarationStatement(ctx: BashpileParser.VariableDeclarationStatementContext): BastNode {
        val node = visit(ctx.expression())
        val readonly = ctx.modifiers().any { it.text == "readonly" }
        val export = ctx.modifiers().any { it.text == "exported" }
        val id = ctx.typedId().Id().text
        val typeText = ctx.typedId().majorType().text
        val type = valueOf(typeText.uppercase())
        return VariableDeclarationBastNode(id, type, readonly = readonly, export = export, child = node)
    }

    override fun visitReassignmentStatement(ctx: BashpileParser.ReassignmentStatementContext): BastNode {
        return ReassignmentBastNode(ctx.Id().text, visit(ctx.expression()))
    }

    override fun visitPrintStatement(ctx: BashpileParser.PrintStatementContext): BastNode {
        val antlrExpressions = ctx.argumentList().expression()
        val nodes = antlrExpressions.map { visit(it) }
        return PrintBastNode(nodes)
    }

    override fun visitExpressionStatement(ctx: BashpileParser.ExpressionStatementContext): BastNode {
        return InternalBastNode(ctx.children.map { visit(it) })
    }

    ///////////////////////////////////
    // expressions
    ///////////////////////////////////

    override fun visitUnaryPostCrementExpression(ctx: BashpileParser.UnaryPostCrementExpressionContext): BastNode {
        val expressionNode = visit(ctx.expression())
        val operator = ctx.op.text
        return UnaryCrementArithmeticBastNode(expressionNode, operator)
    }

    override fun visitUnaryPreCrementExpression(ctx: BashpileParser.UnaryPreCrementExpressionContext): BastNode {
        val expressionNode = visit(ctx.expression())
        val operator = ctx.op.text
        return UnaryCrementArithmeticBastNode(expressionNode, operator, precrement = true)
    }

    override fun visitIdExpression(ctx: BashpileParser.IdExpressionContext): BastNode {
        return VariableReferenceBastNode(ctx.Id().text, UNKNOWN)
    }

    override fun visitTypedId(ctx: BashpileParser.TypedIdContext): BastNode {
        val primaryTypeString = ctx.majorType().text
        val typeEnum = valueOf(primaryTypeString.uppercase())
        return VariableReferenceBastNode(ctx.Id().text, typeEnum)
    }

    override fun visitParenthesisExpression(ctx: BashpileParser.ParenthesisExpressionContext): BastNode {
        check(ctx.childCount == 3)
        val child = visit(ctx.children[1])
        return ParenthesisBastNode(child.asList(), child.majorType())
    }

    override fun visitLiteral(ctx: BashpileParser.LiteralContext): BastNode {
        val boolContext = ctx.BoolValues()
        val stringContext = ctx.StringValues()

        return if (boolContext != null) {
            BooleanLiteralBastNode(boolContext.text.toBoolean())
        } else if (stringContext != null) {
            val text = stringContext.text
            // remove enclosing double or single quotes
            val trimEnds = stringContext.text.substring(1, text.length - 1)
            StringLiteralBastNode(trimEnds)
        } else {
            val message = "Unknown literal type.  Numeric values should be handled in visitNumberExpression"
            throw IllegalArgumentException(message)
        }
    }

    override fun visitNumberExpression(ctx: BashpileParser.NumberExpressionContext): BastNode {
        val nodeText = ctx.text
        return if (nodeText.contains('.')) {
            FloatLiteralBastNode(nodeText.toBigDecimal())
        } else {
            IntegerLiteralBastNode(nodeText.toBigInteger())
        }
    }

    override fun visitNotExpression(ctx: BashpileParser.NotExpressionContext): BastNode {
        val bastNodeChildren = listOf(TerminalBastNode("! ", STRING), visit(ctx.expression()))
        return InternalBastNode(bastNodeChildren, BOOLEAN)
    }

    override fun visitMultipyDivideCalculationExpression(ctx: BashpileParser.MultipyDivideCalculationExpressionContext): BastNode {
        require(ctx.children.size == 3) { "Calculation expression must have 3 children" }
        val bastNodes = ctx.children.map { visit(it) }
        return calculationExpressionInner(bastNodes[0], bastNodes[1] as TerminalBastNode, bastNodes[2])
    }

    override fun visitAddSubtractCalculationExpression(ctx: BashpileParser.AddSubtractCalculationExpressionContext): BastNode {
        require(ctx.children.size == 3) { "Calculation expression must have 3 children" }
        val bastNodes = ctx.children.map { visit(it) }
        return calculationExpressionInner(bastNodes[0], bastNodes[1] as TerminalBastNode, bastNodes[2])
    }

    private fun calculationExpressionInner(left: BastNode, middle: TerminalBastNode, right: BastNode): BastNode {
        val areAllStrings = left.coercesTo(STRING) && right.coercesTo(STRING)
        return if (areAllStrings) {
            require(middle.isAddition()) { "Only addition is supported on strings" }
            StringConcatenationBastNode(listOf(left, right))
        } else if (left.coercesTo(INTEGER) && right.coercesTo(INTEGER)) {
            IntegerArithmeticBastNode(left, middle, right)
        } else if (left.coercesTo(FLOAT) && right.coercesTo(FLOAT)) {
            FloatArithmeticBastNode(left, middle, right)
        } else {
            throw UnsupportedOperationException(
                "Only calculations on all Strings or all numbers are supported, " +
                        "but found ${left.majorType()} and ${right.majorType()}")
        }
    }

    override fun visitUnaryPrimaryExpression(ctx: BashpileParser.UnaryPrimaryExpressionContext): BastNode {
        val operator = ctx.unaryPrimary().text
        val right = visit(ctx.expression())
        return UnaryPrimaryBastNode(operator, right)
    }

    override fun visitBinaryPrimaryExpression(ctx: BashpileParser.BinaryPrimaryExpressionContext): BastNode {
        val left = visit(ctx.expression(0))
        val right = visit(ctx.expression(1))
        return BinaryPrimaryBastNode(left, ctx.binaryPrimary().text, right)
    }

    override fun visitCombiningExpression(ctx: BashpileParser.CombiningExpressionContext): BastNode {
        check (ctx.expression().size == 2) { "Combining expression must have exactly 2 expressions" }
        val expressions = ctx.expression().map { visit(it) }
        return CombiningExpressionBastNode(
            expressions[0], ctx.combiningOperator().text, expressions[1])
    }

    override fun visitTypecastExpression(ctx: BashpileParser.TypecastExpressionContext): BastNode {
        val aastChildren = ctx.children
        require(aastChildren.size == 3)
        val typecastTo = aastChildren[2].text
        val nextType = valueOf(typecastTo.uppercase())
        val bastExpression = visit(aastChildren[0])
        return InternalBastNode(bastExpression.asList(), nextType)
    }

    override fun visitArgumentsBuiltinExpression(ctx: BashpileParser.ArgumentsBuiltinExpressionContext): BastNode {
        val argumentsCtx = ctx.argumentsBuiltin()
        return if (argumentsCtx.NumberValues() != null) {
            // if the script has 'arguments[5]' then textInBrackets would be '5'
            val textInBrackets = ctx.children[0].getChild(2).text
            ArgumentsBastNode(brackedText = textInBrackets)
        } else if (argumentsCtx.All() != null) {
            ArgumentsBastNode(all = true)
        } else if (argumentsCtx.Splat() != null) {
            ArgumentsBastNode(splat = true)
        } else {
            throw UnsupportedOperationException()
        }
    }

    // Leaf nodes (parts of expressions)

    override fun visitShellString(ctx: BashpileParser.ShellStringContext): BastNode {
        return ShellStringBastNode(ctx.shellStringContents().map { visit(it) })
    }

    override fun visitLooseShellString(ctx: BashpileParser.LooseShellStringContext): BastNode {
        return LooseShellStringBastNode(ctx.shellStringContents().map { visit(it) })
    }

    override fun visitVerbatimShellString(ctx: BashpileParser.VerbatimShellStringContext): BastNode {
        return VerbatimShellStringBastNode(ctx.shellStringContents().map { visit(it) })
    }

    override fun visitShellStringContents(ctx: BashpileParser.ShellStringContentsContext): BastNode {
        val bastChildren = ctx.children.map { visit(it) }
        val isNestedSubshell = bastChildren.size == 3
                && bastChildren[0] is SubshellStartTerminalBastNode
                && bastChildren[2] is ClosingParenthesisTerminalBastNode
        return if (bastChildren.size == 1) {
            bastChildren[0]
        } else if (isNestedSubshell) {
            // middle child only
            ShellStringBastNode(bastChildren.subList(1, 2))
        } else {
            InternalBastNode(bastChildren)
        }
    }

    /**
     * Returns a TerminalBastNode.  This is the catch-all when a more specific literal does not match.
     * @see visitLiteral
     * @see visitNumberExpression
     */
    override fun visitTerminal(node: TerminalNode): BastNode {
        return when (node.typeIndex()) {
            BashpileLexer.DollarOParen -> SubshellStartTerminalBastNode()
            BashpileLexer.CParen -> ClosingParenthesisTerminalBastNode()
            else -> TerminalBastNode(
                node.text.replace("^newline$".toRegex(), "\n"),
                STRING
            )
        }
    }
}

private fun BashpileParser.VariableDeclarationStatementContext.modifiers(): List<BashpileParser.ModifierContext> {
    return typedId().modifier()
}

private fun BashpileParser.TypedIdContext.majorType(): BashpileParser.TypesContext {
    return complexType().types(0)
}

/**
 * Gets the type index of this [TerminalNode].  Indexes are defined in [BashpileLexer].
 * @see BashpileLexer.DollarOParen as an example value to match against
 */
private fun TerminalNode.typeIndex(): Int {
    return symbol.type
}

