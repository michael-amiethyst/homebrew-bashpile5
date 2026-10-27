package org.bashpile.core.engine

enum class ShellStringMode {
    /** Run a command for its status or side effects, suppressing STDOUT and STDERR. */
    QUIET_COMMAND,

    /** Capture STDOUT as a value and leave STDERR visible. */
    CAPTURE,

    /** Capture STDOUT as a value while suppressing STDERR. */
    CAPTURE_QUIETLY
}

data class RenderOptions(
    val quoted: Boolean = false,
    val shellStringMode: ShellStringMode = ShellStringMode.CAPTURE,
    /** For floating point and integer arithmetic */
    val arithmeticContext: Boolean = false,
    val integerArithmeticContext: Boolean = false)
{
    companion object {
        val UNQUOTED = RenderOptions(quoted = false)
    }

    fun quoted(): RenderOptions = copy(quoted = true)

    fun unquoted(): RenderOptions = copy(quoted = false)

    fun ignoreOutput(): RenderOptions = copy(shellStringMode = ShellStringMode.QUIET_COMMAND)

    fun asValue(): RenderOptions = copy(
        shellStringMode = when (shellStringMode) {
            ShellStringMode.QUIET_COMMAND -> ShellStringMode.CAPTURE_QUIETLY
            ShellStringMode.CAPTURE, ShellStringMode.CAPTURE_QUIETLY -> shellStringMode
        }
    )

    fun arithmetic(): RenderOptions = copy(arithmeticContext = true, quoted = false)

    fun integerArithmetic(): RenderOptions =
        copy(arithmeticContext = true, integerArithmeticContext = true, quoted = false)
}
