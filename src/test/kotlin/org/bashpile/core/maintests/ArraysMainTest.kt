package org.bashpile.core.maintests

/* STUB for POSIX arrays */
class ArraysMainTest {
    val exampleBash = """
        #!/bin/sh

        # Imagine this comes from an untrusted web form or API
        untrusted_input="MaliciousValue; rm -rf /; echo 'hacked'"

        # The safe index we want to assign to
        i=0

        # --- THE GUARD STEP ---
        # 1. Assign the dangerous input to a plain, static temporary variable
        tmp="${'$'}untrusted_input"

        # 2. Use eval to dynamically create the variable name, 
        #    but reference ${'$'}tmp with an escaped dollar sign (\$)
        eval "user_${'$'}i=\${'$'}tmp"

        # --- VERIFICATION ---
        # Let's read it back safely using the same pattern
        eval "retrieved_value=\${'$'}user_${'$'}i"

        echo "Safely stored value:"
        echo "${'$'}retrieved_value"

    """.trimIndent()
}