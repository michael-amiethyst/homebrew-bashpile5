# Bashpile

YOUR Bash wizard!

See [user_docs](user_docs/overview.md) for the quickstart and a language reference.

See [Rendering generated Bash](contributing/rendering.md) for the `BastNode.render` contract and the `shfmt` boundary.

See [Contributing](contributing/contributing.md) for helping with the project and a technical quickstart 

## Layer Diagram

* Users Shell
* CLI Layer - Handles options and arguments
* Antlr Layer - Creates an Antlr AST
* Engine Layer - Core logic to make the final Bash code
* Internal Shell Layer - Makes any shell calls needed
  * May be able to change to make Linux calls directly?

## Differences from previous version

A rewrite from scratch was justified for a few reasons:

1. To simplify deployment (no dog-fooding for it's own sake) we use Kotlin with a Native Target
   1. This also means no JVM required for end-users
2. A Bashpile AST to avoid Regex string munging
   1. Before context only bubbled up from below in the form of Translation metadata and created a string
   2. When additional context was discovered above the Translation string was edited directly, sometimes using complex regex's
   3. Now an AST is generated that can be modified from above easily and only rendered as Bash after all context is discovered
   4. The BAST renderer emits syntactically valid Bash without owning presentation whitespace; `shfmt` canonicalizes the complete script
3. Stick to pure Kotlin for simplicity
