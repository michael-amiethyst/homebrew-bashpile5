# Switch Statements

May use integers, floats, strings or shell globbing.

Shell globbing includes wildcards and charactor classes like `[1-9]|[1-7][0-9]|8[0-4])`.  This example matches 1-9, 10-79, or 80-84.

Here is an example with a string.  The syntax is similar to Java without break statements needed. 

```Bash
name: string = "Riker"
switch name:
    case "Riker":
        printf "Number 1"
    case "Picard":
        printf "The Captain"
// prints "Number 1"
```
