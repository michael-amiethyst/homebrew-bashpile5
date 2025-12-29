package org.bashpile.core.engine

enum class TypeEnum {
    /** Does not coerce to anything */
    EMPTY,
    /** Coerces to anything */
    UNKNOWN,
    BOOLEAN,
    INTEGER,
    /** Also used for unknown calculation results */
    FLOAT,
    STRING,
    LIST,
    MAP,
    REFERENCE;

    fun coercesTo(type: TypeEnum): Boolean {
        return if (type != FLOAT) {
            this == type || this == UNKNOWN || type == UNKNOWN
        } else {
            this == type || this == UNKNOWN || this == INTEGER
        }
    }

    /** This is for reducing a list of types to what they coerce to, or EMPTY */
    fun fold(type: TypeEnum): TypeEnum {
        return if (this.coercesTo(type)) { type } else { EMPTY }
    }
}
