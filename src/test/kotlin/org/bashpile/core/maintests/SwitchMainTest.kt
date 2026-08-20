package org.bashpile.core.maintests

import org.antlr.v4.runtime.misc.ParseCancellationException
import org.bashpile.core.LinuxProcess
import org.bashpile.core.bast.expressions.CaseBastNode
import org.bashpile.core.engine.RenderOptions.Companion.UNQUOTED
import org.bashpile.core.runCommand
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SwitchMainTest : MainTest() {
    override val testName = "SwitchTest"
    val bpsScriptsDir = "src/test/resources/bpsScripts"

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
        LinuxProcess(bashScript).run(arguments = listOf("start"))
            .assertRenderProduces("Starting service...\n")
    }

    @Test
    fun bashArguments_withGetopt_worksAsExpected() {
        val bashScript = """
            VALID_ARGS=$(getopt -o abg:d: --long alpha,beta,gamma:,delta: -- "$@")
            if [[ $? -ne 0 ]]; then
                exit 1;
            fi

            eval set -- "${'$'}VALID_ARGS"
            while [ : ]; do
              case "$1" in
                -a | --alpha)
                    printf "Processing 'alpha' option"
                    shift
                    ;;
                -b | --beta)
                    printf "Processing 'beta' option"
                    shift
                    ;;
                -g | --gamma)
                    printf "Processing 'gamma' option. Input argument is '$2'"
                    shift 2
                    ;;
                -d | --delta)
                    printf "Processing 'delta' option. Input argument is '$2'"
                    shift 2
                    ;;
                --) shift; 
                    break 
                    ;;
              esac
            done

        """.trimIndent()
        LinuxProcess(bashScript).run(arguments = listOf("--alpha")).assertRenderProduces("Processing 'alpha' option\n")
    }

    @Test
    fun bashArguments_withSingleCharacterClass_worksAsExpected() {
        val bashScript = """
            case "$1" in
                [1][.])
                    printf "Processing 'alpha' option"
                    shift
                    ;;
                -b | --beta)
                    printf "Processing 'beta' option"
                    shift
                    ;;
                -g | --gamma)
                    printf "Processing 'gamma' option. Input argument is '$2'"
                    shift 2
                    ;;
                -d | --delta)
                    printf "Processing 'delta' option. Input argument is '$2'"
                    shift 2
                    ;;
                --) shift; 
                    break 
                    ;;
                esac

        """.trimIndent()
        LinuxProcess(bashScript).run(arguments = listOf("1.")).assertRenderProduces("Processing 'alpha' option\n")
    }

    /** When we implement while statements we'll handle argument parsing this way */
    @Test
    fun bashArguments_withWhile_worksAsExpected() {
        val bashScript = """
            while [[ $# -gt 0 ]]; do
              key="$1"
            
              case ${'$'}key in
                -e|--env)
                  ENV_VAR="$2"
                  printf "Environment variable set to: %s" "${'$'}ENV_VAR"
                  shift # shift once for the key
                  shift # shift a second time for the value
                  ;;
                -d|--delete)
                  DELETE=true
                  printf "Delete flag set to: ${'$'}DELETE"
                  shift # shift once for the flag
                  ;;
                *)    # Unknown option or positional argument
                  # Handle positional arguments or error out
                  printf "Unknown option %s" "$1"
                  exit 1
                  ;;
              esac
            done

        """.trimIndent()
        LinuxProcess(bashScript).run(arguments = listOf("--delete")).assertRenderProduces("Delete flag set to: true\n")
    }

    @Test
    fun switch_works() {
        val render: String =
            Path("src/test/resources/bpsScripts/switch.bps").readText(Charsets.UTF_8).createRender()
        assertRenderEquals("""
            declare name
            name="Riker"
            case "${'$'}{name}" in
                Riker)
                    printf "Number 1"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Number 1\n")
    }

    @Test
    fun switch_withMultipleCases_works() {
        val render: String =
            Path("src/test/resources/bpsScripts/switchTwoCases.bps").readText(Charsets.UTF_8).createRender()
        assertRenderEquals("""
            declare name
            name="Riker"
            case "${'$'}{name}" in
                Riker)
                    printf "Number 1"
                    ;;
                Picard)
                    printf "The Captain"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Number 1\n")
    }

    @Test
    fun switch_withMultipleCases_inCondition_works() {
        val render: String = """
            five: integer = 5
            if (4 < five):
                name: string = "Riker"
                switch (name):
                    case Riker:
                        printf "Number 1"
                    case Picard:
                        printf "The Captain"
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare five
            five=5
            if [ 4 -lt "${'$'}{five}" ]; then
                declare name
                name="Riker"
                case "${'$'}{name}" in
                    Riker)
                        printf "Number 1"
                        ;;
                    Picard)
                        printf "The Captain"
                        ;;
                esac
            fi
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Number 1\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_works() {
        val render: String = """
            name: string = "Riker"
            switch (name):
                case Riker:
                    printf "Number 1\n"
                    print("Trombone player\n")
                case Picard:
                    printf "The Captain"
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name="Riker"
            case "${'$'}{name}" in
                Riker)
                    printf "Number 1\n"
                    printf "Trombone player\n"
                    ;;
                Picard)
                    printf "The Captain"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Number 1\nTrombone player\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_integers_works() {
        val render: String = Path("$bpsScriptsDir/switchIntegers.bps").readText().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber=1
            case "${'$'}{starbaseNumber}" in
                1)
                    printf "Earth\n"
                    ;;
                80)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_floats_works() {
        val render: String = """
            starbaseNumber: float = 1.0
            switch (starbaseNumber):
                case 1.0:
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber=1.0
            case "${'$'}{starbaseNumber}" in
                1.0)
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_characterClasses_works() {
        val render = Path("src/test/resources/bpsScripts/switchCharacterClasses.bps").readText().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber=1.0
            case "${'$'}{starbaseNumber}" in
                [1][.][0-1])
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_characterClassesAndStrings_works() {
        val render = """
            starbaseNumber: float = 1.0
            switch (starbaseNumber):
                case 1.[0-1]:
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")""".trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber=1.0
            case "${'$'}{starbaseNumber}" in
                1.[0-1])
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_asterisk_works() {
        val render = """
            starbaseNumber: float = 1.0
            switch (starbaseNumber):
                case 1*:
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")""".trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber=1.0
            case "${'$'}{starbaseNumber}" in
                1*)
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_questionMark_works() {
        val render = """
            starbaseNumber: float = 1.0
            switch (starbaseNumber):
                case 1?0:
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")""".trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber=1.0
            case "${'$'}{starbaseNumber}" in
                1?0)
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_characterClassesAndStrings_literalColon_works() {
        val render = """
            starbaseNumber: string = "1.0:"
            switch (starbaseNumber):
                case 1.[0-1]\::
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")""".trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber="1.0:"
            case "${'$'}{starbaseNumber}" in
                1.[0-1]\:)
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_characterClassesAndStrings_withOr_works() {
        val render = """
            starbaseNumber: string = "1.2"
            switch (starbaseNumber):
                case 1.[0-1]\:|1.2:
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")""".trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber="1.2"
            case "${'$'}{starbaseNumber}" in
                1.[0-1]\: | 1.2)
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_negatedCharacterClassesAndStrings_withOr_works() {
        val render = """
            starbaseNumber: string = "1.0:"
            switch (starbaseNumber):
                case 1.[!2-9]\:|1.2:
                    print("Earth")
                    print(", the first one")
                case 80.0:
                    print("The Lower Decks one\n")""".trimIndent().createRender()
        assertRenderEquals("""
            declare starbaseNumber
            starbaseNumber="1.0:"
            case "${'$'}{starbaseNumber}" in
                1.[!2-9]\: | 1.2)
                    printf "Earth"
                    printf ", the first one"
                    ;;
                80.0)
                    printf "The Lower Decks one\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Earth, the first one\n")
    }

    @Test
    fun switch_withMultipleCases_multipleStatements_withEscapes_works() {
        val render = """
            name: string = "La Forge:|" // We wanted to see if colon and pipe worked
            switch (name):
                case Riker:
                    print("Number 1\n") // new lines needed
                    print("Trombone player\n")
                case Picard:
                    print("The Captain")
                case La\ For[!a]?\:\|:
                    print("Chief Engineer") // this prints
                default:
                    print("Other")
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare name # We wanted to see if colon and pipe worked
            name="La Forge:|"
            case "${'$'}{name}" in
                Riker)
                    printf "Number 1\n" # new lines needed
                    printf "Trombone player\n"
                    ;;
                Picard)
                    printf "The Captain"
                    ;;
                La\ For[!a]?\:\|)
                    printf "Chief Engineer" # this prints
                    ;;
                *)
                    printf "Other"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Chief Engineer\n")
    }

    @Test
    fun switch_withDefault_works() {
        val render: String = """
            name: string = "La Forge"
            switch (name):
                case Riker:
                    printf "Number 1\n"
                    print("Trombone player\n")
                case Picard:
                    printf "The Captain"
                default:
                    print("Other crew\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare name
            name="La Forge"
            case "${'$'}{name}" in
                Riker)
                    printf "Number 1\n"
                    printf "Trombone player\n"
                    ;;
                Picard)
                    printf "The Captain"
                    ;;
                *)
                    printf "Other crew\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Other crew\n")
    }

    @Test
    fun switch_withDefault_withMultipleComments_works() {
        val render: String = """
            name: string = "La Forge" // A cool actor // did Reading Rainbow too
            switch (name):
                case Riker:
                    printf "Number 1\n"
                    print("Trombone player\n")
                case Picard:
                    printf "The Captain"
                default:
                    print("Other crew\n")
        """.trimIndent().createRender()
        assertRenderEquals("""
            declare name # A cool actor // did Reading Rainbow too
            name="La Forge"
            case "${'$'}{name}" in
                Riker)
                    printf "Number 1\n"
                    printf "Trombone player\n"
                    ;;
                Picard)
                    printf "The Captain"
                    ;;
                *)
                    printf "Other crew\n"
                    ;;
            esac
            
        """.trimIndent(), render).runCommand().assertRenderProduces("Other crew\n")
    }

    @Test
    fun switch_caseVariable_isOutOfScopeAfterSwitch() {
        assertFailsWith<IllegalStateException> { """
                name: string = "Riker"
                switch (name):
                    case Riker:
                        rank: string = "Commander"
                        print(rank + "\n")
                print(rank + "\n")
            """.trimIndent().createRender()
        }
    }

    @Test
    fun switch_defaultVariable_isOutOfScopeAfterSwitch() {
        assertFailsWith<IllegalStateException> { """
                name: string = "La Forge"
                switch (name):
                    case Riker:
                        print("Commander\n")
                    default:
                        rank: string = "Lieutenant Commander"
                        print(rank + "\n")
                print(rank + "\n")
            """.trimIndent().createRender()
        }
    }

    @Test
    fun switch_render_isIdempotent() {
        val bast = fixture._getBast("""
            name: string = "Riker"
            switch (name):
                case Riker:
                    print("Number 1\n")
                default:
                    print("Other\n")
        """.trimIndent().byteInputStream())
        val cases = bast.toList().filterIsInstance<CaseBastNode>()
        val statementCounts = cases.map { it.statements.size }

        val firstRender = LinuxProcess.shfmt(bast.render(UNQUOTED))
        val secondRender = LinuxProcess.shfmt(bast.render(UNQUOTED))

        assertEquals(firstRender, secondRender)
        assertEquals(statementCounts, cases.map { it.statements.size })
    }

    /** Test for comments */
    @Test
    fun switch_withDefault_withMultilineComments_atStartOfLine_works() {
        assertThrows<ParseCancellationException> {
            // mixing the end of a multiline comment and the start of statements is not supported
            """
                name: string = "La Forge" /* A cool
                 actor */ switch (name):
                    case Riker:
                        printf "Number 1\n"
                        print("Trombone player\n")
                    case Picard:
                        printf "The Captain"
                    default:
                        print("Other crew\n")
            """.trimIndent().createRender()
        }
    }
}