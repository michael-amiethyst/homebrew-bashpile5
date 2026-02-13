package org.bashpile.core.maintests

import org.bashpile.core.runCommand
import kotlin.test.Test

class SwitchMainTest : MainTest() {
    override val testName = "SwitchTest"

    @Test
    fun bashSwitch_worksAsExpected() {
        val bashScript = """
            case "$1" in
                start|up)
                    printf "Starting service..."
                    # Add start commands here
                    ;;
                stop|down)
                    printf "Stopping service..."
                    # Add stop commands here
                    ;;
                status)
                    printf "Checking status..."
                    # Add status check commands here
                    ;;
                *)
                    printf "Usage: $0 {start|stop|status}"
                    exit 1
                    ;;
            esac

        """.trimIndent()
        bashScript.runCommand(arguments = listOf("start")).assertRenderProduces("Starting service...\n")
    }

    @Test
    fun bashArguments_worksAsExpected() {
        val bashScript = """
            ...

        """.trimIndent()
        bashScript.runCommand(arguments = listOf("start")).assertRenderProduces("Starting service...\n")
    }
}