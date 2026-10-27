package org.bashpile.core.engine

data class RenderOptions(
    val quoted: Boolean = false,
    val ignoreOutput: Boolean = false,
    /** For floating point and integer arithmetic */
    val arithmeticContext: Boolean = false,
    val integerArithmeticContext: Boolean = false)
{
    companion object {
        val UNQUOTED = RenderOptions(quoted = false)
    }

    fun quoted(): RenderOptions = copy(quoted = true)

    fun unquoted(): RenderOptions = copy(quoted = false)

    fun ignoreOutput(): RenderOptions = copy(ignoreOutput = true)

    fun asValue(): RenderOptions = copy(ignoreOutput = false)

    fun arithmetic(): RenderOptions = copy(arithmeticContext = true, quoted = false)

    fun integerArithmetic(): RenderOptions =
        copy(arithmeticContext = true, integerArithmeticContext = true, quoted = false)
}
