# Rendering generated Bash

`BastNode.render(RenderOptions)` converts a BAST subtree into Bash source. Its output must be syntactically valid and
semantically correct, but it is not responsible for presentation formatting.

Render implementations still need to emit whitespace that Bash treats as syntax. This includes separators between
tokens, command boundaries, the newline after a line comment, and the exact structure required by heredocs. They do
not need to align indentation, preserve optional spaces, wrap long constructs, or otherwise match the final style.

The application renders the complete BAST and then calls `String.shfmt()` once. `shfmt` reparses the complete program
and owns canonical indentation and presentation whitespace. Do not call `shfmt` from an individual node: a subtree
may be a fragment such as a condition or expression that is not independently parseable as a Bash program.

Tests that compare generated Bash must use the same boundary:

```kotlin
val bash = root.render(RenderOptions.UNQUOTED).shfmt()
```

Most compiler integration tests should use `MainTest.createRender()`, which already renders and formats. Tests that
construct or transform BAST nodes directly may call `render(...).shfmt()` instead. Expected Bash should describe the
canonical `shfmt` output rather than a node's incidental raw spacing.

The project currently invokes `shfmt` with the Bash dialect, four-space indentation, and indented `case` alternatives.
Formatting is a runtime boundary, so `shfmt` must be installed and available on `PATH` when Bashpile or its render
tests run.
