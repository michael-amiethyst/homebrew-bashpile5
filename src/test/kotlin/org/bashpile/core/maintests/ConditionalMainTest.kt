package org.bashpile.core.maintests

import org.bashpile.core.SCRIPT_ERROR__GENERIC
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ConditionalMainTest : MainTest() {

    override val testName = "ConditionalTest"

    @Test
    fun conditionals_works() {
        val renderedBash = """
            #(ls some_random_file_that_does_not_exist.txt) or true
            """.trimIndent().createRender()
        assertRenderEquals("""
            (ls some_random_file_that_does_not_exist.txt) >/dev/null 2>&1 || true
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("\n")
    }

    @Test
    fun ifStatement_works() {
        val renderedBash = """
            if (1 > 0):
                print("Math is mathing! ")
                print("Math is mathing!\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if [ 1 -gt 0 ]; then
                printf "Math is mathing! "
                printf "Math is mathing!\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing! Math is mathing!\n")
    }

    @Test
    fun ifStatement_isEmpty_works() {
        val renderedBash = """
            name: string = ""
            if (isEmpty name):
                print("Empty\n")
            else:
                print("Not empty\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            if [ -z "${'$'}{name}" ]; then
                printf "Empty\n"
            else
                printf "Not empty\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Empty\n")
    }

    @Test
    fun ifStatement_isEmptyPrecedence_works() {
        val renderedBash = """
            name: string = ""
            if (isEmpty name + "notEmpty"):
                print("Empty\n")
            else:
                print("Not empty\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            if [ -z "${'$'}{name}notEmpty" ]; then
                printf "Empty\n"
            else
                printf "Not empty\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Not empty\n")
    }

    @Test
    fun ifStatement_isEmptyPrecedence_not_works() {
        val renderedBash = """
            name: string = ""
            if (not isEmpty name + "value"):
                print("!Empty\n")
            else:
                print("Empty\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            if ! [ -z "${'$'}{name}value" ]; then
                printf "!Empty\n"
            else
                printf "Empty\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("!Empty\n")
    }

    @Test
    fun ifStatement_notEquals_withStringConcat_works() {
        val renderedBash = """
            name: string = ""
            if ("value" == name + "value"):
                print("Equal\n")
            else:
                print("Not Equal\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            if [ "value" == "${'$'}{name}value" ]; then
                printf "Equal\n"
            else
                printf "Not Equal\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Equal\n")
    }

    @Test
    fun ifStatement_notEquals_withStringConcat_andParenthesis_works() {
        val renderedBash = """
            name: string = ""
            if ("value" == (name + "value")):
                print("Equal\n")
            else:
                print("Not Equal\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            if [ "value" == "${'$'}{name}value" ]; then
                printf "Equal\n"
            else
                printf "Not Equal\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Equal\n")
    }

    @Test
    fun ifStatement_notEquals_withStringConcat_andParenthesisAndCombining_works() {
        val renderedBash = """
            name: string = ""
            input1: string = "value"
            if ("value" == (name + "value") and input1 == "value"):
                print("Equal\n")
            else:
                print("Not Equal\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            declare input1
            input1="value"
            if [ "value" == "${'$'}{name}value" ] && [ "${'$'}{input1}" == "value" ]; then
                printf "Equal\n"
            else
                printf "Not Equal\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Equal\n")
    }

    @Test
    fun ifStatement_notEquals_withStringConcat_andParenthesisAndCombiningAndShellString_works() {
        val renderedBash = """
            name: string = ""
            input1: string = "value"
            if ("value" == (name + "value") and input1 == #(printf "value")):
                print("Equal\n")
            else:
                print("Not Equal\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            declare input1
            input1="value"
            if [ "value" == "${'$'}{name}value" ] && [ "${'$'}{input1}" == "$(printf "value")" ]; then
                printf "Equal\n"
            else
                printf "Not Equal\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Equal\n")
    }

    @Test
    fun ifStatement_notEquals_withDeepStringConcat_works() {
        val renderedBash = """
            name: string = ""
            if ("value" == ( name + ("val" + "ue") )):
                print("Equal\n")
            else:
                print("Not Equal\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name=""
            if [ "value" == "${'$'}{name}value" ]; then
                printf "Equal\n"
            else
                printf "Not Equal\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Equal\n")
    }

    @Test
    fun ifStatement_notEquals_withShellString_works() {
        val renderedBash = """
            if ("value" == #(ls)):
                print("Equal\n")
            else:
                print("Not Equal\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if [ "value" == "$(ls)" ]; then
                printf "Equal\n"
            else
                printf "Not Equal\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Not Equal\n")
    }

    @Test
    fun ifStatement_isNotEmpty_works() {
        val renderedBash = """
            name: string = "hello"
            if (isNotEmpty name):
                print("Not empty\n")
            else:
                print("Empty\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name="hello"
            if [ -n "${'$'}{name}" ]; then
                printf "Not empty\n"
            else
                printf "Empty\n"
            fi
    
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Not empty\n")
    }

    @Test
    fun ifStatement_exists_works() {
        val filename = "src/test/resources/data/example.csv"
        val renderedBash = """
            filename: string = "$filename"
            if (exists filename):
                print("File exists\n")
            else if (doesNotExist filename):
                print("File does not exist\n")
            else:
                print("Unknown\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare filename
            filename="$filename"
            if [ -e "${'$'}{filename}" ]; then
                printf "File exists\n"
            elif [ ! -e "${'$'}{filename}" ]; then
                printf "File does not exist\n"
            else
                printf "Unknown\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("File exists\n")
    }

    @Test
    fun ifStatement_doesNotExist_works() {
        val filename = "src/test/resources/data/exampleNonExistentFile.csv"
        val renderedBash = """
            filename: string = "$filename"
            if (exists filename):
                print("File exists\n")
            else if (doesNotExist filename):
                print("File does not exist\n")
            else:
                print("Unknown\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare filename
            filename="$filename"
            if [ -e "${'$'}{filename}" ]; then
                printf "File exists\n"
            elif [ ! -e "${'$'}{filename}" ]; then
                printf "File does not exist\n"
            else
                printf "Unknown\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("File does not exist\n")
    }

    @Test
    fun ifStatement_stringCompare_works() {
        val filename = "src/test/resources/data/exampleNonExistentFile.csv"
        val renderedBash = """
            filename: string = "$filename"
            if (filename == "$filename"):
                print("String compare works\n")
            else if (doesNotExist filename):
                print("Fail\n")
            else:
                print("Unknown\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare filename
            filename="$filename"
            if [ "${'$'}{filename}" == "$filename" ]; then
                printf "String compare works\n"
            elif [ ! -e "${'$'}{filename}" ]; then
                printf "Fail\n"
            else
                printf "Unknown\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("String compare works\n")
    }

    /** Tests for a native '-e' */
    @Test
    fun ifStatement_bashExists_works() {
        val filename = "src/test/resources/data/exampleNonExistentFilename.csv"
        val renderedBash = """
            filename: string = "$filename"
            if (-e filename):
                print("File exists\n")
            else if (not -e filename):
                print("File does not exist\n")
            else:
                print("Unknown\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare filename
            filename="$filename"
            if [ -e "${'$'}{filename}" ]; then
                printf "File exists\n"
            elif ! [ -e "${'$'}{filename}" ]; then
                printf "File does not exist\n"
            else
                printf "Unknown\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("File does not exist\n")
    }

    /** Tests for a native '-e' */
    @Test
    fun ifStatement_bashNotExists_works() {
        val filename = "src/test/resources/data/example.csv"
        val renderedBash = """
            filename: string = "$filename"
            if (-e filename):
                print("File exists\n")
            else if (not -e filename):
                print("File does not exist\n")
            else:
                print("Unknown\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare filename
            filename="$filename"
            if [ -e "${'$'}{filename}" ]; then
                printf "File exists\n"
            elif ! [ -e "${'$'}{filename}" ]; then
                printf "File does not exist\n"
            else
                printf "Unknown\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("File exists\n")
    }

    /** Tests for a native '-e' */
    @Test
    fun ifStatement_bashNotNotExists_works() {
        val filename = "src/test/resources/data/example.csv"
        val renderedBash = """
            filename: string = "$filename"
            if (not doesNotExist filename):
                print("File exists\n")
            else if (not -e filename):
                print("File does not exist\n")
            else:
                print("Unknown\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare filename
            filename="$filename"
            if ! [ ! -e "${'$'}{filename}" ]; then
                printf "File exists\n"
            elif ! [ -e "${'$'}{filename}" ]; then
                printf "File does not exist\n"
            else
                printf "Unknown\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("File exists\n")
    }

    @Test
    fun ifElseStatement_works() {
        val renderedBash = """
            zero: integer = 0
            if (1 < zero):
                print("Math is not mathing\n")
            else:
                print("Math is mathing! ")
                print("Math is mathing!\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare zero
            zero=0
            if [ 1 -lt "${'$'}{zero}" ]; then
                printf "Math is not mathing\n"
            else
                printf "Math is mathing! "
                printf "Math is mathing!\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing! Math is mathing!\n")
    }

    @Test
    fun ifElseStatement_withFailedShellString_works() {
        val renderedBash = """
            if (#(expr 1 \> 0; exit ${SCRIPT_ERROR__GENERIC})):
                print("Math is mathing! ")
                print("Math is mathing!\n")
            else:
                print("Command failed\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if (expr 1 \> 0; exit ${SCRIPT_ERROR__GENERIC}) >/dev/null 2>&1; then
                printf "Math is mathing! "
                printf "Math is mathing!\n"
            else
                printf "Command failed\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Command failed\n")
    }

    @Test
    fun ifElseStatement_withFailedShellString_andParens_works() {
        val renderedBash = """
            if ((#(expr 1 \> 0; exit ${SCRIPT_ERROR__GENERIC}))):
                print("Math is mathing! ")
                print("Math is mathing!\n")
            else:
                print("Command failed\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if (expr 1 \> 0; exit ${SCRIPT_ERROR__GENERIC}) >/dev/null 2>&1; then
                printf "Math is mathing! "
                printf "Math is mathing!\n"
            else
                printf "Command failed\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Command failed\n")
    }

    @Test
    fun ifElseIfElseStatement_works() {
        val renderedBash = """
            zero: integer = 0
            if (1 < zero):
                print("Math is not mathing\n")
            else if (zero < 1):
                print("Math is mathing! ")
                print("Math is mathing!\n")
            else:
                print("Zero is one???\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare zero
            zero=0
            if [ 1 -lt "${'$'}{zero}" ]; then
                printf "Math is not mathing\n"
            elif [ "${'$'}{zero}" -lt 1 ]; then
                printf "Math is mathing! "
                printf "Math is mathing!\n"
            else
                printf "Zero is one???\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing! Math is mathing!\n")
    }

    @Test
    fun ifElseIfElseStatement_scopingWorks() {
        assertFailsWith<IllegalStateException> { """
            zero: integer = 0
            if (1 < zero):
                print("Math is not mathing\n")
            else if (zero < 1):
                math: string = "Math"
                print(math + " is mathing! ")
                print(math + " is mathing!\n")
            else:
                print("Zero is one???\n")
            print(math)
            """.trimIndent().createRender()
        }
    }

    @Test
    fun ifElseIfElseStatement_withShellStringInElseIf_works() {
        val renderedBash = """
            zero: integer = 0
            if (1 < zero):
                print("Math is not mathing\n")
            else if (#(expr "${'$'}{zero}" \< 1)):
                print("Math is mathing! ")
                print("Math is mathing!\n")
            else:
                print("Zero is one???\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare zero
            zero=0
            if [ 1 -lt "${'$'}{zero}" ]; then
                printf "Math is not mathing\n"
            elif (expr "${'$'}{zero}" \< 1) >/dev/null 2>&1; then
                printf "Math is mathing! "
                printf "Math is mathing!\n"
            else
                printf "Zero is one???\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing! Math is mathing!\n")
    }

    @Test
    fun ifElseStatement_withAnd_works() {
        val renderedBash = """
            if (1 < 2 and 2 <= 3):
                print("Math is mathing!\n")
            else:
                print("Command failed\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if [ 1 -lt 2 ] && [ 2 -le 3 ]; then
                printf "Math is mathing!\n"
            else
                printf "Command failed\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing!\n")
    }

    @Test
    fun ifElseStatement_withAndOr_works() {
        val renderedBash = """
            if (1 < 2 and 2 <= 1 or #(expr 1 \> 0)):
                print("Math is mathing!\n")
            else:
                print("Command failed\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if [ 1 -lt 2 ] && [ 2 -le 1 ] || (expr 1 \> 0) >/dev/null 2>&1; then
                printf "Math is mathing!\n"
            else
                printf "Command failed\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing!\n")
    }

    @Test
    fun ifElseStatement_withAndOr_literalFloats_works() {
        val renderedBash = """
            if (1.0 < 2.0 and 2.0 <= 1.0 or #(bc <<< "2.0 < 3.0")):
                print("Math is mathing!\n")
            else:
                print("Command failed\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            if bc -l <<< "1.0 < 2.0" > /dev/null && bc -l <<< "2.0 <= 1.0" > /dev/null || (bc <<< "2.0 < 3.0") >/dev/null 2>&1; then
                printf "Math is mathing!\n"
            else
                printf "Command failed\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing!\n")
    }

    @Test
    fun ifElseStatement_withAndOr_floatVariable_works() {
        val renderedBash = """
            one: float = 1.0
            if (one < 2.0 and 2.0 <= one or #(bc <<< "2.0 < 3.0")):
                print("Math is mathing!\n")
            else:
                print("Command failed\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare one
            one=1.0
            if bc -l <<< "${'$'}{one} < 2.0" > /dev/null && bc -l <<< "2.0 <= ${'$'}{one}" > /dev/null || (bc <<< "2.0 < 3.0") >/dev/null 2>&1; then
                printf "Math is mathing!\n"
            else
                printf "Command failed\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("Math is mathing!\n")
    }

    @Test
    fun ifElseStatement_withBooleans_works() {
        val renderedBash = """
            a: boolean = true
            if (a and false):
                print("Both true\n")
            else if (a or false):
                print("At least one true\n")
            else:
                print("Neither true\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare a
            a=true
            if ${'$'}{a} && false; then
                printf "Both true\n"
            elif ${'$'}{a} || false; then
                printf "At least one true\n"
            else
                printf "Neither true\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("At least one true\n")
    }

    @Test
    fun ifElseStatement_withTypeCastSubtractionAndEquals_works() {
        val renderedBash = """
            a: integer = 1
            if (#(expr 5 + 6) as integer - a == 10):
                print("It tracks\n")
            else:
                print("Lame\n")
            """.trimIndent().createRender()
        assertRenderEquals("""
            declare a
            a=1
            if [ $(($(expr 5 + 6) - a)) -eq 10 ]; then
                printf "It tracks\n"
            else
                printf "Lame\n"
            fi
            
            """.trimIndent(), renderedBash
        ).assertRenderProduces("It tracks\n")
    }
}
