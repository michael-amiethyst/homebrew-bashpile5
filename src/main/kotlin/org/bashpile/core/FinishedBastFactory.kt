package org.bashpile.core

import com.google.common.annotations.VisibleForTesting
import org.apache.logging.log4j.LogManager
import org.bashpile.core.bast.BastNode
import org.bashpile.core.bast.expressions.VariableReferenceBastNode
import org.bashpile.core.bast.expressions.arithmetic.ArithmeticBastNode
import org.bashpile.core.bast.expressions.shellstrings.ShellStringBastNode
import org.bashpile.core.bast.statements.StatementBastNode
import org.bashpile.core.bast.statements.VariableDeclarationBastNode
import org.bashpile.core.engine.Subshell
import org.bashpile.core.engine.TypeEnum.UNKNOWN


/**
 * "We'll fix it in post!"  This is the equivalent of post-production.
 *
 * Takes a freshly created Bashpile Abstract Syntax Tree from [org.bashpile.core.antlr.AstConvertingVisitor] and
 * performs transformations on it to prepare it for rendering with [BastNode.render].
 */
class FinishedBastFactory {

    private val logger = LogManager.getLogger(Main::javaClass)

    fun transform(root: BastNode): BastNode {
        logger.info("Mermaid graph ---------------- initial: {}", root.mermaidGraph())

        // unnest
        val unnestedBast = root.unnestSubshells()
        logger.info("Mermaid graph ----- subshells unnested: {}", unnestedBast.mermaidGraph())
        return unnestedBast
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
    internal fun BastNode.unnestSubshells(): BastNode {
        var unnestedCount = 0
        // forEach runs serially so mutating operations are OK
        this.nestedSubshells().forEach { nestedSubshell ->
            val id = "__bp_var${unnestedCount++}"
            nestedSubshell.mutatingReplaceWith(VariableReferenceBastNode(id, UNKNOWN))
            val decl = VariableDeclarationBastNode(id, UNKNOWN,
                    child = ShellStringBastNode(nestedSubshell.children))
            // insertVariableDeclaration(nestedSubshell.closestParentStatementIndex - 1)
            nestedSubshell.parents().first { it is StatementBastNode }.mutatingAddBefore(decl)
        }
        return this
    }

    /** Gets all descendants that are subshells in subshells */
    private fun BastNode.nestedSubshells(): List<BastNode> {
        return this.allDescendants().filter {
            it is Subshell && it !is ArithmeticBastNode // Arithmetic nodes render correctly if nested
        }.filter { subshells ->
            subshells.parents().any { it is Subshell }
        }
    }
}
