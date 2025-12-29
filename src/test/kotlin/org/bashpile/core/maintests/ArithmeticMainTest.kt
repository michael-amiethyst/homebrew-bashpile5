package org.bashpile.core.maintests

import org.bashpile.core.SCRIPT_ERROR__GENERIC
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Tests Clikt and [org.bashpile.core.Main._getBast], does not test logging.
 * See SystemTest for logger framework tests.
 */
class ArithmeticMainTest : MainTest() {

    override val testName = "ArithmeticTest"

    @Test
    fun getBast_basicArithmatic_works() {
        val render = """
            print(1 + 1)""".trimIndent().createRender()
        assertRenderEquals("""
            printf "%s" "$((1 + 1))"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("2\n")
    }

    @Test
    fun getBast_basicArithmatic_subtraction_works() {
        val render = """
            print(1 - 1)""".trimIndent().createRender()
        assertRenderEquals("""
            printf "%s" "$((1 - 1))"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("0\n")
    }

    @Test
    fun getBast_basicArithmatic_division_works() {
        val render = """
            print(6 / 4)""".trimIndent().createRender()
        assertRenderEquals("""
            printf "%s" "$((6 / 4))"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("1\n")
    }

    @Test
    fun getBast_basicArithmatic_withTypecast_works() {
        val render = """
                one: string = "1"
                print(1 - one as integer)""".trimIndent().createRender()
        assertRenderEquals("""
                declare one
                one="1"
                printf "%s" "$((1 - one))"

            """.trimIndent(), render
        )
        render.assertRenderProduces("0\n")
    }

    @Test
    fun getBast_basicArithmatic_withTypecast_andFloatAssignToInt_works() {
        val render = """
                four: string = "4"
                i: integer = 6 / four as integer
                print(i)""".trimIndent().createRender()
        assertRenderEquals("""
                declare four
                four="4"
                declare i
                i=$((6 / four))
                printf "%s" "${'$'}{i}"

            """.trimIndent(), render
        )
        render.assertRenderProduces("1\n")
    }

    @Test
    fun getBast_basicArithmatic_withTypecast_andFloats_work() {
        val render = """
                four: string = "4"
                i: float = 6 / four as float
                print(i)""".trimIndent().createRender()
        assertRenderEquals("""
                declare four
                four="4"
                declare i
                i="$(bc -l <<< "6 / ${'$'}{four}")"
                printf "%s" "${'$'}{i}"

            """.trimIndent(), render
        )
        render.assertRenderProduces("1.50000000000000000000\n")
    }

    /** We don't double-check the user */
    @Test
    fun getBast_basicArithmatic_withBadTypecast_works() {
        val render = """
                one: string = "one"
                print(1 - one as integer)""".trimIndent().createRender()
        render.assertRenderProduces(null, SCRIPT_ERROR__GENERIC)
    }

    @Test
    fun getBast_basicArithmatic_withTypecastAndParenthesis_works() {
        val render = """
                one: string = "1"
                print(1 - (one as integer))""".trimIndent().createRender()
        assertRenderEquals("""
                declare one
                one="1"
                printf "%s" "$((1 - (one)))"

            """.trimIndent(), render
        )
        render.assertRenderProduces("0\n")
    }

    @Test
    fun getBast_basicArithmatic_withPlusPlus_works() {
        val render = """
                i: integer = 0
                print(i++)
                print(i)""".trimIndent().createRender()
        assertRenderEquals("""
                declare i
                i=0
                printf "%s" "$((i++))"
                printf "%s" "${'$'}{i}"

            """.trimIndent(), render
        )
        render.assertRenderProduces("01\n")
    }

    @Test
    fun getBast_basicArithmatic_withPreIncrement_works() {
        val render = """
                i: integer = 0
                print(++i)
                print(i)""".trimIndent().createRender()
        assertRenderEquals("""
                declare i
                i=0
                printf "%s" "$((++i))"
                printf "%s" "${'$'}{i}"

            """.trimIndent(), render
        )
        render.assertRenderProduces("11\n")
    }

    @Test
    fun getBast_basicArithmatic_withFloatPlusPlus_fails() {
        assertFailsWith<IllegalStateException> { """
                i: float = 0
                print(i++)
                print(i)""".trimIndent().createRender()
        }
    }

    @Test
    fun getBast_basicArithmatic_withLiteralIntegerPlusPlus_fails() {
        assertFailsWith<IllegalStateException> {"""
                print(0++)
                print(0)""".trimIndent().createRender()
        }
    }

    @Test
    fun getBast_basicArithmatic_withShellString_works() {
        val render = """
                i: integer = 0
                print(#(expr ${'$'}i) as integer + 1)
                print(i)""".trimIndent().createRender()
        assertRenderEquals("""
                declare i
                i=0
                printf "%s" "$(($(expr ${'$'}i) + 1))"
                printf "%s" "${'$'}{i}"

            """.trimIndent(), render
        )
        render.assertRenderProduces("10\n")
    }

    @Test
    fun getBast_basicArithmatic_withParenthesis_works() {
        val render = """
                i: integer = 0
                print((i++))
                print(i)""".trimIndent().createRender()
        assertRenderEquals("""
                declare i
                i=0
                printf "%s" "$((i++))"
                printf "%s" "${'$'}{i}"

            """.trimIndent(), render
        )
        render.assertRenderProduces("01\n")
    }

    @Test
    fun getBast_complexArithmatic_works() {
        val render = """
            print(1 - (5 * 6))""".trimIndent().createRender()
        assertRenderEquals("""
            printf "%s" "$((1 - (5 * 6)))"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("-29\n")
    }

    @Test
    fun getBast_basicFloatingPointArithmatic_works() {
        val render = """
            print(1.0 + .5)""".trimIndent().createRender()
        assertRenderEquals("""
            printf "%s" "$(bc -l <<< "1.0 + 0.5")"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("1.5\n")
    }

    @Test
    fun getBast_floatingPointArithmatic_parenthesis_works() {
        val render = """
            print(1.0 - (30 * .5))""".trimIndent().createRender()
        assertRenderEquals("""
                printf "%s" "$(bc -l <<< "1.0 - (30 * 0.5)")"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("-14.0\n")
    }

    @Test
    fun getBast_floatingPointArithmatic_noParenthesis_works() {
        val render = """
            print(1.0 - 30 * .5)""".trimIndent().createRender()
        assertRenderEquals("""
                printf "%s" "$(bc -l <<< "1.0 - 30 * 0.5")"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("-14.0\n")
    }

    @Test
    fun getBast_floatingPointArithmatic_shellStringAndParenthesis_throws() {
        // ShellString is a... STRING
        assertFailsWith<UnsupportedOperationException> { """
            print(#(expr 2 - 1) - (30 * .5))""".trimIndent().createRender()
        }
    }

    @Test
    fun getBast_floatingPointArithmatic_shellStringAndParenthesis_withTypecast_works() {
        val render = """
            print(#(expr 2 - 1) as integer - (30 * .5))""".trimIndent().createRender()
        assertRenderEquals("""
            declare __bp_var0
            __bp_var0="$(expr 2 - 1)"
            printf "%s" "$(bc -l <<< "${'$'}{__bp_var0} - (30 * 0.5)")"
            
            """.trimIndent(), render
        )
        render.assertRenderProduces("-14.0\n")
    }
}