# Bashpile Compiler Plans

1. While loops and argument parsing 
   1. See the test bashArguments_withWhile_worksAsExpected
2. lists
   1. See ArraysMainTest.kt
3. C-style for loops
4. foreach loops
5. Work-around for options with `||`, see https://unix.stackexchange.com/questions/65532/why-does-set-e-not-work-inside-subshells-with-parenthesis-followed-by-an-or
6. Functions, Function forward declarations
7. IDE integration (IntelliJ)
8. '--POSIX' mode, strict posix, test in ash/dash

## Full Release (v1.0.0)
Copy repo to michael-amiethyst/homebrew-bashpile/
1. Rename old homebrew-bashpile to homebrew-bashpile-java.
2. Rename current to homebrew-bashpile.
3. Test bashpile.com/bashpile.org links after a day or two to verify.
    1. Make java.bashpile.org, kotlin.bashpile.org

## Unscheduled ideas
switch/case statement extglob support
Take 2nd file argument, it would be the compiled file (with shebang and chmod +x)
Reverse compile (Bash -> Bashpile) for easy diffs
- And/Or add Bashpile as comment above the Bash

# Bashpile STDLIB Plans

* Conversion of Types (e.g. `float` to `int` and actually round up or down)
  * Verify types / asserts
* Argument Handling / getopt parsing
