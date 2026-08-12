# Comments and BashpileDoc

## Line Comments
Like Java, just add `//` to start the comment.  It will be rendered

## Block Comments
Block comments (`/* */`) are not supported since Bash doesn't have a native Block Comment.  There are no good workarounds.

## Bashpile Doc

Similar to Java, `/**` and `*/`.  Keep them on their own lines only.
E.g. avoid
```shell
/**
 * Docs
 */ str: exported string = 'A_STRING'
 
// instead do:
/**
 * Docs
 */
str: exported string = 'A_STRING'
```