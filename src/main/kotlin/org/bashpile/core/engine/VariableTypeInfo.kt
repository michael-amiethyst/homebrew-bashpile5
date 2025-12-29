package org.bashpile.core.engine

/** All the relevant data for a variable for [CallStack] */
data class VariableTypeInfo(val id: String, val majorType: TypeEnum, val minorType: TypeEnum, val readonly: Boolean) {
    fun coercesTo(type: TypeEnum): Boolean = majorType.coercesTo(type)
}
