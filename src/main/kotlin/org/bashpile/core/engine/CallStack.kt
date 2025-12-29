package org.bashpile.core.engine


typealias Stackframe = MutableList<VariableTypeInfo>

/**
 * Sometimes the type of a node is known at creation, so the type may be in [org.bashpile.core.bast.BastNode] as well.
 */
class CallStack: AutoCloseable {

    private val stack: MutableList<Stackframe> = mutableListOf(mutableListOf())

    fun addVariableInfo(id: String, type: TypeEnum, subtype: TypeEnum, readonly: Boolean) {
        val top: Stackframe = stack.last()
        top.add(VariableTypeInfo(id, type, subtype, readonly))
    }

    fun requireOnStack(id: String): VariableTypeInfo {
        val info = variableInfo(id)
        check (info != null) { "Could not find $id on the stack"}
        return info
    }

    /** Returns null on null input or if the variable ID was not found on the call stack. */
    fun variableInfo(id: String?): VariableTypeInfo? {
        val topmostStackframeWithId = stack.findLast { frame -> frame.any { it.id == id } }
        return topmostStackframeWithId?.find { it.id == id }
    }

    fun pushStackframe() {
        stack.add(mutableListOf())
    }

    override fun close() {
        // pop stack
        stack.removeLast()
        check(stack.isNotEmpty()) { "You cannot pop every frame off of stack" }
    }
}
