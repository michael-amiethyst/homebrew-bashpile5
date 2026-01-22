package org.bashpile.core.maintests

import org.bashpile.core.bast.statements.ForeachFileLineLoopBashNode.Companion.sed
import kotlin.test.Test
import kotlin.test.assertFailsWith

// TODO 0.21.0 - Fix shellcheck errors here and for ShellStringTests
//  command is `shellcheck --shell=bash --exclude=SC2059,SC2157,SC2327,SC2328 ./build/shellcheck/Loops* `
class LoopsMainTest : MainTest() {

    override val testName = "LoopsTest"

    @Test
    fun foreach_fileLine_works() {
        val renderedBash = """
            for(first: string, last: string, email: string, phone: string in "src/test/resources/data/example.csv"):
                print(first + " " + last + " " + email + " " + phone + "\n")
        """.trimIndent().createRender()
        assertRenderEquals(
            """
            cat "src/test/resources/data/example.csv" | $sed -e '1d' -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r __bp_line; do
                first=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $1}'); last=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $2}'); email=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $3}'); phone=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $4}');
                printf "${'$'}{first} ${'$'}{last} ${'$'}{email} ${'$'}{phone}\n"
            done
            
        """.trimIndent(), renderedBash).assertRenderProduces("""
            Alice Smith alice.smith@email.com 555-1234
            Bob Johnson bob.j@email.com 555-5678
            Charlie Williams c.williams@email.com 555-9012
            
        """.trimIndent()
        )
    }

    @Test
    fun foreach_fileLine_multistatement_works() {
        val renderedBash = """
            // Real world example
            HOST: readonly exported string = "HOST_NAME"
            TOKEN: readonly exported string = "OAUTH_TOKEN"
            for(firstName: string, middleName: string, lastName: string, email: string, landline: string, cell: string \
                    in "src/test/resources/data/example_extended.csv"):
                // set progress status too
                cellShort: exported string = #(printf "${'$'}cell" | cut -d " " -f 2)
                regionId: exported integer = 13
                print("Updating phone # " + cellShort + " with values: lastName " + lastName + " cell " + cell + ".\n")
                print("{ \"cellShort\": ${'$'}cellShort, \"lastName\": \"${'$'}lastName\" \"cell\": \"${'$'}cell\", " + \
                    "\"regionId\": \"${'$'}regionId\" }\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            # Real world example
            declare -x HOST
            HOST="HOST_NAME"
            declare -x TOKEN
            TOKEN="OAUTH_TOKEN"
            cat "src/test/resources/data/example_extended.csv" | $sed -e '1d' -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r __bp_line; do
                firstName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $1}'); middleName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $2}'); lastName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $3}'); email=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $4}'); landline=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $5}'); cell=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $6}');
                # set progress status too
                declare -x cellShort
                cellShort="$(printf "${'$'}cell" | cut -d " " -f 2)"
                declare -x regionId
                regionId=13
                printf "Updating phone # ${'$'}{cellShort} with values: lastName ${'$'}{lastName} cell ${'$'}{cell}.\n"
                printf "{ \"cellShort\": ${'$'}cellShort, \"lastName\": \"${'$'}lastName\" \"cell\": \"${'$'}cell\", \"regionId\": \"${'$'}regionId\" }\n"
            done
            
        """.trimIndent(), renderedBash).assertRenderProduces("""
            Updating phone # 555-1235 with values: lastName Smith cell (555) 555-1235.
            { "cellShort": 555-1235, "lastName": "Smith" "cell": "(555) 555-1235", "regionId": "13" }
            Updating phone # 555-5679 with values: lastName Johnson cell (555) 555-5679.
            { "cellShort": 555-5679, "lastName": "Johnson" "cell": "(555) 555-5679", "regionId": "13" }
            Updating phone # 555-1701 with values: lastName Williams cell (555) 555-1701.
            { "cellShort": 555-1701, "lastName": "Williams" "cell": "(555) 555-1701", "regionId": "13" }

        """.trimIndent()
        )
    }

    @Test
    fun foreach_fileLine_multistatement_with_float_works() {
        val renderedBash = """
            // Real world example
            HOST: readonly exported string = "HOST_NAME"
            TOKEN: readonly exported string = "OAUTH_TOKEN"
            for(firstName: string, middleName: string, lastName: string, email: string, landline: float, \
                    cell: string in "src/test/resources/data/example_extended.csv"):
                /** set progress status too */
                cellShort: exported string = #(printf "${'$'}cell" | cut -d " " -f 2)
                regionId: exported integer = 13
                print("Updating phone # " + cellShort + " with values: lastName " + lastName + " cell " + cell + ".\n")
                print("{ \"cellShort\": ${'$'}cellShort, \"lastName\": \"${'$'}lastName\" \"cell\": \"${'$'}cell\", \"regionId\": \"${'$'}regionId\" }\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            # Real world example
            declare -x HOST
            HOST="HOST_NAME"
            declare -x TOKEN
            TOKEN="OAUTH_TOKEN"
            cat "src/test/resources/data/example_extended.csv" | $sed -e '1d' -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r __bp_line; do
                firstName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $1}'); middleName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $2}'); lastName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $3}'); email=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $4}'); landline=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $5}'); cell=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $6}');
                #########################
                # set progress status too
                #########################
                declare -x cellShort
                cellShort="$(printf "${'$'}cell" | cut -d " " -f 2)"
                declare -x regionId
                regionId=13
                printf "Updating phone # ${'$'}{cellShort} with values: lastName ${'$'}{lastName} cell ${'$'}{cell}.\n"
                printf "{ \"cellShort\": ${'$'}cellShort, \"lastName\": \"${'$'}lastName\" \"cell\": \"${'$'}cell\", \"regionId\": \"${'$'}regionId\" }\n"
            done
            
        """.trimIndent(), renderedBash).assertRenderProduces("""
            Updating phone # 555-1235 with values: lastName Smith cell (555) 555-1235.
            { "cellShort": 555-1235, "lastName": "Smith" "cell": "(555) 555-1235", "regionId": "13" }
            Updating phone # 555-5679 with values: lastName Johnson cell (555) 555-5679.
            { "cellShort": 555-5679, "lastName": "Johnson" "cell": "(555) 555-5679", "regionId": "13" }
            Updating phone # 555-1701 with values: lastName Williams cell (555) 555-1701.
            { "cellShort": 555-1701, "lastName": "Williams" "cell": "(555) 555-1701", "regionId": "13" }

        """.trimIndent()
        )
    }

    @Test
    fun foreach_fileLine_multistatement_with_windows_line_endings_float_works() {
        val renderedBash = """
            // Real world example
            HOST: readonly exported string = "HOST_NAME"
            TOKEN: readonly exported string = "OAUTH_TOKEN"
            for(firstName: string, middleName: string, lastName: string, email: string, landline: float, cell: string\
                    in "src/test/resources/data/example_extended_windows_line_endings.csv"):
                // set progress status too
                cellShort: exported string = #(printf "${'$'}cell" | cut -d " " -f 2)
                regionId: exported integer = 13
                print("Updating phone # " + cellShort + " with values: lastName " + lastName + " cell " + cell + ".\n")
                print("{ \"cellShort\": ${'$'}cellShort, \"lastName\": \"${'$'}lastName\" \"cell\": \"${'$'}cell\", \"regionId\": \"${'$'}regionId\" }\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            # Real world example
            declare -x HOST
            HOST="HOST_NAME"
            declare -x TOKEN
            TOKEN="OAUTH_TOKEN"
            cat "src/test/resources/data/example_extended_windows_line_endings.csv" | $sed -e '1d' -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r __bp_line; do
                firstName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $1}'); middleName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $2}'); lastName=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $3}'); email=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $4}'); landline=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $5}'); cell=$(printf "%s" "${'$'}{__bp_line}" | gawk --csv '{print $6}');
                # set progress status too
                declare -x cellShort
                cellShort="$(printf "${'$'}cell" | cut -d " " -f 2)"
                declare -x regionId
                regionId=13
                printf "Updating phone # ${'$'}{cellShort} with values: lastName ${'$'}{lastName} cell ${'$'}{cell}.\n"
                printf "{ \"cellShort\": ${'$'}cellShort, \"lastName\": \"${'$'}lastName\" \"cell\": \"${'$'}cell\", \"regionId\": \"${'$'}regionId\" }\n"
            done
            
        """.trimIndent(), renderedBash).assertRenderProduces("""
            Updating phone # 555-1235 with values: lastName Smith cell (555) 555-1235.
            { "cellShort": 555-1235, "lastName": "Smith" "cell": "(555) 555-1235", "regionId": "13" }
            Updating phone # 555-5679 with values: lastName Johnson cell (555) 555-5679.
            { "cellShort": 555-5679, "lastName": "Johnson" "cell": "(555) 555-5679", "regionId": "13" }
            Updating phone # 555-1701 with values: lastName Williams cell (555) 555-1701.
            { "cellShort": 555-1701, "lastName": "Williams" "cell": "(555) 555-1701", "regionId": "13" }

        """.trimIndent()
        )
    }

    @Test
    fun foreach_fileLine_non_csv_works() {
        val filename = "src/test/resources/data/plain.txt"
        val renderedBash = """
            for(line: string in "$filename"):
                print(line + "\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            cat "$filename" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                printf "${'$'}{line}\n"
            done
            
        """.trimIndent(), renderedBash).assertRenderProduces("""
            lorum
            ipsum

            """.trimIndent()
        )
    }

    @Test
    fun foreach_fileLine_no_trailing_newline_works() {
        val filename = "src/test/resources/data/plain_no_trailing_newline.txt"
        val renderedBash = """
            for(line: string in "$filename"):
                print(line + "\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            cat "$filename" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                printf "${'$'}{line}\n"
            done
            
        """.trimIndent(), renderedBash).assertRenderProduces("""
            lorum
            ipsum

            """.trimIndent())
    }

    @Test
    fun foreach_fileLine_scoping_works() {
        val filename = "src/test/resources/data/plain.txt"
        assertFailsWith<IllegalStateException>  {
            """
                for(line: string in "$filename"):
                    scoped: string = "Hello World"
                    print(line + "\n")
                print(scoped + "\n")
            """.trimIndent().createRender()
        }
    }

    @Test
    fun foreach_fileLine_scoping_referenceOuterScope_works() {
        val filename = "src/test/resources/data/plain.txt"
        """
            outerScope: string = "Hello Mars"
            for(line: string in "$filename"):
                print(outerScope + "\n")
        """.trimIndent().createRender().assertRenderProduces("""
            Hello Mars
            Hello Mars

        """.trimIndent()
            )
    }

    @Test
    fun foreach_fileLine_scoping_variableShadowing_works() {
        val filename = "src/test/resources/data/plain.txt"
        """
            line: string = "Who's line is it Anyway?"
            for(line: string in "$filename"):
                print(line + "\n")
        """.trimIndent().createRender().assertRenderProduces("""
            lorum
            ipsum

        """.trimIndent()
            )
    }

    @Test
    fun foreach_fileLine_nested_works() {
        val outerFilename = "src/test/resources/data/labeled_lines.txt"
        val innerFilename = "src/test/resources/data/plain.txt"
        """
            line: string = "Who's line is it Anyway?"
            for(line: string in "$outerFilename"):
                print(line + "\n")
                for(line2: string in "$innerFilename"):
                    print(line2 + "\n")
        """.trimIndent().createRender().assertRenderProduces("""
            row1
            lorum
            ipsum
            row2
            lorum
            ipsum

        """.trimIndent()
            )
    }

    @Test
    fun foreach_fileLine_nested_withShadowing_works() {
        val outerFilename = "src/test/resources/data/labeled_lines.txt"
        val innerFilename = "src/test/resources/data/plain.txt"
        """
            line: string = "Who's line is it Anyway?"
            for(line: string in "$outerFilename"):
                print(line + "\n")
                for(line: string in "$innerFilename"):
                    print(line + "\n")
        """.trimIndent().createRender().assertRenderProduces("""
            row1
            lorum
            ipsum
            row2
            lorum
            ipsum

        """.trimIndent()
            )
    }

    @Test
    fun foreach_fileLine_withNestedSubshells_works() {
        val outerFilename = "labeled_lines.txt"
        val render = """
            cd src/test/resources/data
            for(line: string in "$outerFilename"):
                print(#(ls -m $(printf '.')) + "\n")
                
            """.trimIndent().createRender()
        assertRenderEquals(
            """
            cd src/test/resources/data
            cat "labeled_lines.txt" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                declare __bp_var0
                __bp_var0="$(printf '.')"
                printf "$(ls -m ${'$'}{__bp_var0})\n"
            done
            
            """.trimIndent(), render
        ).assertRenderProduces(
            """
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            
        """.trimIndent()
        )
    }

    @Test
    fun foreach_nested_withNestedSubshells_works() {
        val outerFilename = "labeled_lines.txt"
        val render = """
            cd src/test/resources/data
            for(line: string in "$outerFilename"):
                for(line: string in "$outerFilename"):
                    print(#(ls -m $(printf '.')) + "\n")
                
            """.trimIndent().createRender()
        assertRenderEquals("""
            cd src/test/resources/data
            cat "labeled_lines.txt" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                cat "labeled_lines.txt" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                    declare __bp_var0
                    __bp_var0="$(printf '.')"
                    printf "$(ls -m ${'$'}{__bp_var0})\n"
                done
            done
            
        """.trimIndent(), render).assertRenderProduces("""
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            
        """.trimIndent())
    }

    @Test
    fun foreach_nested_withMultipleNestedSubshells_works() {
        val outerFilename = "labeled_lines.txt"
        val render = """
            cd src/test/resources/data
            for(line: string in "$outerFilename"):
                printf "%s" "${'$'}{line}" > /dev/null
                print(#(ls -m "$(printf '.')" > /dev/null))
                for(line: string in "$outerFilename"):
                    printf "%s" "${'$'}{line}" > /dev/null
                    print(#(ls -m "$(printf '.')") + "\n")
                    // this Bash makes sense to me, may God have mercy on my soul
                    print(#(cat $(ls -all | head -4 | tail -1 | tr -s " " | cut -d " " -f 9 ) | head -1) + "\n")
                
            """.trimIndent().createRender()
        assertRenderEquals(
            """
            cd src/test/resources/data
            cat "labeled_lines.txt" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                printf "%s" "${'$'}{line}" > /dev/null
                declare __bp_var0
                __bp_var0="$(printf '.')"
                printf "$(ls -m "${'$'}{__bp_var0}" > /dev/null)"
                cat "labeled_lines.txt" | $sed -e 's/\r\n/\n/g' | $sed -ze '/\n$/!s/$/\n$/g' | while IFS='' read -r line; do
                    printf "%s" "${'$'}{line}" > /dev/null
                    declare __bp_var1
                    __bp_var1="$(printf '.')"
                    printf "$(ls -m "${'$'}{__bp_var1}")\n"
                    # this Bash makes sense to me, may God have mercy on my soul
                    declare __bp_var2
                    __bp_var2="$(ls -all | head -4 | tail -1 | tr -s " " | cut -d " " -f 9 )"
                    printf "$(cat ${'$'}{__bp_var2} | head -1)\n"
                done
            done
            
        """.trimIndent(), render).assertRenderProduces("""
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            FirstName,LastName,Email,Phone
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            FirstName,LastName,Email,Phone
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            FirstName,LastName,Email,Phone
            example.csv, example_extended.csv, example_extended_windows_line_endings.csv, 
            labeled_lines.txt, plain.txt, plain_no_trailing_newline.txt
            FirstName,LastName,Email,Phone
            
        """.trimIndent())
    }
}
