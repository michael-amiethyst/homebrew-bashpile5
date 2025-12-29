# Bashpile Compiler Plans

1. Debug comments (mapping from Bashpile lines to generated Bash lines)
2. switch / getopt
3. C-style for loops
4. lists
5. foreach loops
6. Work-around for options with `||`, see https://unix.stackexchange.com/questions/65532/why-does-set-e-not-work-inside-subshells-with-parenthesis-followed-by-an-or
7. IDE integration (IntelliJ)

## Unscheduled ideas
Take 2nd file argument, it would be the compiled file (with shebang and chmod +x)

# Bashpile STDLIB Plans

* Conversion of Types (e.g. `float` to `int` and actually round up or down)
  * Verify types / asserts
* Argument Handling / getopt parsing
