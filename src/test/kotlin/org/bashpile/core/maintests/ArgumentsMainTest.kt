package org.bashpile.core.maintests

import kotlin.test.Test

class ArgumentsMainTest : MainTest() {
    override val testName: String = "ArgumentsMainTest"

    @Test
    fun argumentReferenceWorks() {
        val script = "print(arguments[1])".createRender()
        assertRenderEquals("printf \"$1\"\n", script)
            .assertRenderProduces("first\n", arguments = listOf("first"))
    }

    @Test
    fun argument_all_works() {
        // ensure Bash is behaving as expected
        """
            printf "$@"

        """.trimIndent().assertRenderProduces(
            "first\n", arguments = listOf("first", "second", "third"))

        // ensure our Render is as expected
        val script = """
            print(arguments[all])

            """.trimIndent().createRender()
        assertRenderEquals("""
            printf "$@"

            """.trimIndent(), script
        ).assertRenderProduces("first\n", arguments = listOf("first", "second", "third"))
    }

    @Test
    fun argument_splat_works() {
        // ensure Bash is behaving as expected
        """
            printf "$*\n"

        """.trimIndent().assertRenderProduces(
            "first second third\n", arguments = listOf("first", "second", "third"))

        // ensure our Render is as expected
        val script = """
            print(arguments[splat] + "\n")

            """.trimIndent().createRender()
        assertRenderEquals("""
            printf "$*\n"

            """.trimIndent(), script
        ).assertRenderProduces("first second third\n", arguments = listOf("first", "second", "third"))
    }
}
