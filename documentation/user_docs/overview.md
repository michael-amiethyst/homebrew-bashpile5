# Bashpile: A modern language that transpiles to Bash

## Why?

I hate Bash.  Surviving the Unix Wars left it scarred.  
Did you know that there are 4 different built in ways to do addition and none of them support floats?
Here's some things from the "Bash Iceberg" that Bashpile will handle for you:
* Ensuring that generated scripts run in "strict mode" to ensure that errors are caught early
* shellcheck compatible
* Portability (e.g. POSIX)
* Whitespace (Bash says that spaces separate tokens but "varName = 1" will not work)
* Various Bash bugs (e.g. a nested subshell will have a bad exit code ignored)

As a bonus example if in that arithmetic you want to use `<=`,
it isn't supported so you have to use not `>`.  However, that's interpreted as a redirect so you have to escape it as `\>`.
If you didn't notice that's a workaround for a workaround!  Bashpile handles it for you.

Bashpile has over 20 StackOverflow answers built in and you get to work with a language similar to Python and Java.

## Why not just use Fish or Python directly?

Bash runs just about anywhere on the most minimal of servers, including production machines and docker containers.
Also, for all of its faults mentioned above it's fast, especially if you are making a lot of command line calls.


## Quickstart
This will install a JVM Jar, and may be a bit slow (a few seconds to compile a small program).
1. `brew tap michael-amiethyst/core`
2. `brew install michael-amiethyst/core/bashpile`
3. `bashpile -c "print('Hello World')"`
   1. This will print out the Bash translation
   2. You can redirect this to a file to use the Bash directly or immediatly execute it
   3. E.g. `bashpile /tmp/hello > /tmp/hello.bash && bash /tmp/hello.bash` for "Hello World"

## Regular start
If you've used Bashpile a bit and want faster execution times, read this section.  

To make a faster execution time you can pull the code and build on your machine.  The magic of Graal will let this
JVM project to run as a fast native program!  I've seen a start of 5 seconds go to milliseconds.

Prerequisite: don't have the Homebrew version installed.  Run `brew uninstall bashpile` if needed. 

1. Pull the code from our repo at https://github.com/michael-amiethyst/homebrew-core
2. At the project root run 'make install' with a Graal 21 VM, it will install a bashpile binary to `/usr/local/bin/bashpile`
   1. You can install with sdkman or jenv
3. `bashpile -c "print('Hello World')"` will run much quicker
   1. Note to OSX users, you may need to jump through some security hoops to allow the program to run

## Script Start

1. Create a script starting with `#!/usr/bin/env bashpile`
2. $(bashpile SCRIPT)
3. If errors run `cat ~/.bashpile/log.txt`

## Language Reference

* Quick compiles!  Within a second
* White space agnostic (except for Python style indentation)
  * Bash is white space sensitive and inconsistent about it too!
* `print("Any String here")`, [print statements](statements/print-statements.md)
* `"string" + "concatination"` [calculation expressions](expressions/calculation-expressions.md)
  * Integer and Floating Point arithmatic supported
* [Types!](features/types.md)
* `varName: str = "variable"` [variable declaration statements](statements/variable-declaration-statements.md)
  * `SOME_CONSTANT: readonly string = "const"`
  * `SOME_CONSTANT: readonly exported str = "const"` exported is the same as Bash's `export`
* Typecasts
  * `one: string = "1"`
  * `(one as integer) + 1`
* `varName = "reassign"` [reassignment statements](statements/reassignment-statements.md)
* Subshells (`$()` syntax) are called [shell strings](expressions/shell-strings.md) and use a `#()` syntax
  * To just pass inline Bash (similar to inline assembly in C/C++) use a verbatim shell string with `v#()` syntax
* Automatic [strict mode](features/strict-mode.md) handling
  * This adds `set -euo pipefail` at the top of the file -- this makes the script fail fast in many ways
  * Opt out (e.g. for a 3rd party script) with `l#(command line)` syntax (the l stands for loose)
* [If-else if-else statements](statements/if-statements.md)
* Argument handling
  * `arguments[1]` -> `$1`
  * `arguments[all]` -> `$@`
    * Note that Bash treats $@ as a list, so print(arguments[all]) only prints the first argument
  * `arguments[splat]` -> `$*`
    * `print(arguments[splat])` prints all arguments as one would expect
    * By default it will separate with a space, to change set IFS (e.g. set to a linebreak with `l#(IFS="\n")`)
