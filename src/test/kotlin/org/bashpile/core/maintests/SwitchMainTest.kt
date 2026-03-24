package org.bashpile.core.maintests

import org.bashpile.core.runCommand
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.test.Test

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
        bashScript.runCommand(arguments = listOf("start")).assertRenderProduces("Starting service...\n")
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
        bashScript.runCommand(arguments = listOf("--alpha")).assertRenderProduces("Processing 'alpha' option\n")
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
        bashScript.runCommand(arguments = listOf("1.")).assertRenderProduces("Processing 'alpha' option\n")
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
        bashScript.runCommand(arguments = listOf("--delete")).assertRenderProduces("Delete flag set to: true\n")
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
                1.[0-1]\:|1.2)
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
                1.[!2-9]\:|1.2)
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
}