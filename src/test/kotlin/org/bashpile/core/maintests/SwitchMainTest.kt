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
}