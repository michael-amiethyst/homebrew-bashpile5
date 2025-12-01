package org.bashpile.core

import com.google.common.annotations.VisibleForTesting
import org.apache.logging.log4j.LogManager
import org.bashpile.core.antlr.AstConvertingVisitor.Companion.ENABLE_STRICT
import org.bashpile.core.antlr.AstConvertingVisitor.Companion.OLD_OPTIONS
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.InternalBastNode
import org.bashpile.core.bast.expressions.arithmetic.ArithmeticBastNode
import org.bashpile.core.bast.expressions.shellstrings.LooseShellStringBastNode
import org.bashpile.core.bast.expressions.shellstrings.ShellStringBastNode
import org.bashpile.core.bast.statements.ShellLineBastNode
import org.bashpile.core.bast.statements.VariableDeclarationBastNode
import org.bashpile.core.engine.TypeEnum.UNKNOWN
import org.bashpile.core.bast.expressions.VariableReferenceBastNode
import org.bashpile.core.engine.Subshell


typealias PreambelsBastNodePair = Pair<List<BastNode>, BastNode>
/**
 * Takes a freshly created Bashpile Abstract Syntax Tree from [org.bashpile.core.antlr.AstConvertingVisitor] and
 * performs a series of transformations on it to prepare it for rendering with [BastNode.render].
 */
class FinishedBastFactory {

    private val logger = LogManager.getLogger(Main::javaClass)

    fun transform(root: BastNode): BastNode {
        logger.info("Mermaid graph ---------------- initial: {}", root.mermaidGraph())

        // unnest
        val unnestedBast = root.unnestSubshells().second
        logger.info("Mermaid graph ----- subshells unnested: {}", unnestedBast.mermaidGraph())

        // loosen
        val looseBast = unnestedBast.loosenShellStrings()
        logger.info("Mermaid graph - shell strings loosened: {}", looseBast.mermaidGraph())
        return looseBast
    }

    @VisibleForTesting
    internal fun BastNode.mermaidGraph(parentNodeName: String = "", mermaidNodeIds: HashMap<String, Int> = HashMap())
    : String {
        if (parentNodeName.isEmpty()) {
            // initial case
            mermaidNodeIds.clear()
            return "graph TD;" + mermaidGraph("root")
        } else {
            // terminating cose: no children
            var mermaid = ""
            children.forEach { child ->
                val nodeTypeName = child::class.simpleName!!.removeSuffix("BastNode")
                val nodeId = mermaidNodeIds.getOrDefault(nodeTypeName, Integer.valueOf(0))
                val nodeName = nodeTypeName + nodeId
                mermaidNodeIds[nodeTypeName] = nodeId + 1
                mermaid += "$parentNodeName --> $nodeName;${child.mermaidGraph(nodeName)}"
            }
            return mermaid
        }
    }

    /**
     * Returns a list of preambles to support unnesting.
     * Not implemented in BastNode.render() because of complexity and testability.
     *
     * @return An unnested version of the input tree.
     * @see /documentation/contributing/unnest.md
     */
    @VisibleForTesting
    internal fun BastNode.unnestSubshells(): PreambelsBastNodePair {
        // TODO recurse into children, bubble preambles up to first statement node
        var unnestedCount = 0
        val unnestedStatements: List<PreambelsBastNodePair> = children.map { statementNode ->
            // the recursion is hidden in .allNodes(), it's linear from there
            val nestedSubshells = statementNode.allDescendants().filter {
                it is Subshell && it !is ArithmeticBastNode // Arithmetic nodes render correctly if nested
            }.filter { subshells ->
                subshells.parents().any { it is Subshell }
            }
            val variableDeclarationBastNodes = nestedSubshells.map { nestedSubshell ->
                val id = "__bp_var${unnestedCount++}"
                nestedSubshell.mutatingReplaceWith(VariableReferenceBastNode(id, UNKNOWN))
                VariableDeclarationBastNode(
                    id,
                    UNKNOWN,
                    child = ShellStringBastNode(nestedSubshell.children)
                )
            }
            Pair(variableDeclarationBastNodes, statementNode)
        }
        return Pair(listOf(), replaceChildren(unnestedStatements.flatMap { it.first + it.second }))
    }

    /** @return A loosened version of the input tree */
    private fun BastNode.loosenShellStrings(): BastNode {
        // no recursion
        val loosenedStatements = children.map {
            val hasLooseShellStringBastNode = it.any { child -> child is LooseShellStringBastNode }
            if (hasLooseShellStringBastNode) {
                InternalBastNode(
                    ShellLineBastNode("eval \"$${OLD_OPTIONS}\""),
                    it,
                    ShellLineBastNode(ENABLE_STRICT))
            } else {
                it
            }
        }
        return replaceChildren(loosenedStatements)
    }
}
