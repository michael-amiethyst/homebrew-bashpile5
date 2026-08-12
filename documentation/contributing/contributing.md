# Contributing

## Quick Start
### Setup env

Ensure your IDE can display Mermaid syntax.

If you need to test the Homebrew formula locally make a soft link (`ln -s`) from HOMEBREW_CELLAR/bashpile-local
to your project root.

### Build
`./gradlew clean check`
Native executable is generated to bin/bashpile

## Design

1. Parse arguments with [CLIKT](https://ajalt.github.io/clikt/).  
2. Parse Bashpile script with Antlr to Antlr AST (ANTLR framework does this).
3. Transform into Bashpile AST with AstConvertingVisitor.
4. Run mutations on Bashpile ASTs.
5. Render the Bashpile AST as syntactically valid, unformatted Bash.
6. Run the complete Bash script through `shfmt` for canonical presentation formatting.

See [Rendering generated Bash](rendering.md) for the `BastNode.render` contract and testing guidance.
