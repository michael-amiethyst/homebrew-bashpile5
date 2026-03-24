# Bashpile Compiler Plans

1. While loops and argument parsing 
   1. See the test bashArguments_withWhile_worksAsExpected
2. lists
3. C-style for loops
4. foreach loops
5. Work-around for options with `||`, see https://unix.stackexchange.com/questions/65532/why-does-set-e-not-work-inside-subshells-with-parenthesis-followed-by-an-or
6. IDE integration (IntelliJ)

## Unscheduled ideas
switch/case statement extglob support
Take 2nd file argument, it would be the compiled file (with shebang and chmod +x)

# Bashpile STDLIB Plans

* Conversion of Types (e.g. `float` to `int` and actually round up or down)
  * Verify types / asserts
* Argument Handling / getopt parsing
