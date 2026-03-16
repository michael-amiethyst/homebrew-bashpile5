# Switch Statements

May use integers, floats, strings or shell globbing.  Note that Bash uses string based matching so a float of 1.0, 1. and 1.00 will all be different cases.

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

Default cases can be specified as well.  Such as:

```Bash
name: string = "La Forge"
switch name:
    case "Riker":
        print("Number 1")
    case "Picard":
        print("The Captain")
    default:
        print("Other") // prints "Other"
```
