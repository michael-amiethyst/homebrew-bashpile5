# Foreach File Line Statements

Windows line endings OK.  May end with no trailing newline.
Skips first line of CSV.

Example:

```Bash
for(line: string):
  someMethod(line)
  
// Referencing more than two variable names will put it into CSV parsing mode
for(first: string, last: string, email: string, phone: string in "src/test/resources/data/example.csv"):
  print(first + " " + last + " " + email + " " + phone + "\n")
```
