# Switch Statements

May use integers, floats, strings or shell globbing.  Note that Bash uses string based matching so a float of 1.0, 1. and 1.00 will all be different cases.

Shell globbing includes wildcards and charactor classes like `[1-9]|[1-7][0-9]|8[0-4])`.  This example matches 1-9, 10-79, or 80-84.

Here is an example with a string.  The syntax is similar to Java without break statements needed. 

```Bash
name: string = "Riker"
switch (name):
    case Riker:
        printf "Number 1"
    case Picard:
        printf "The Captain"
// prints "Number 1"
```

Default cases can be specified as well.  Such as:

```Bash
name: string = "La Forge"
switch (name):
    case Riker:
        print("Number 1")
    case Picard:
        print("The Captain")
    default:
        print("Other") // prints "Other"
```

## Lexical scope

Each `case` body and the `default` body has its own lexical scope. Variables declared before the switch remain
available to the matching expression and to every branch. A variable declared inside a branch can be used by later
statements in that branch, but it is not available in another branch or after the switch. The same variable name may
therefore be declared independently in different branches.

```Bash
name: string = "Riker"
switch (name):
    case Riker:
        rank: string = "Commander"
        print(rank) // rank is available here
    default:
        rank: string = "Unknown"
        print(rank) // a separate rank declaration

// print(rank) would fail to compile because rank is out of scope
```

Glob patterns and pipes are allowed as well, and character escapes.  Such as:

```Bash
name: string = "La Forge:|"
switch (name):
    case Riker:
        print("Number 1")
    case Picard:
        print("The Captain")
    case La\ For[!a]?\:\|:
        print("Chief Engineer") // this prints
    default:
        print("Other")
```
