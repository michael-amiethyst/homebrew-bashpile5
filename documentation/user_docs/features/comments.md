# Comments and BashpileDoc

## Line Comments
Like Java, just add `//` to start the comment.  It will be rendered

## Block Comments
Also like Java, anything inside of `/*` and `*/` is a comment.

Bash doesn't have a native Block Comment, so only use it at the end of a line.

E.g.
```bash
/* Here is an example of what
NOT to do */ print("Will not compile")

i: int = 0 /* You can start
sharing a line though */
print("The render will be on one line though")
```

## Bashpile Doc

Similar to Java, `/**` and `*/`.