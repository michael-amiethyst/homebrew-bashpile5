# Quickstart

Ensure you have installed Bashpile with brew, this will pull in prerequisites for development as well.
Alternately run `brew install gawk gsed shfmt`.

1. `./gradlew clean check` for all integration tests and native compile
   1. Ensure Java 21 by GraalVM is your JVM
   2. You may want to use [sdkman](https://sdkman.io/) (with sdkman_auto_env enabled)
      1. A .sdkman file is included here
2. or `./gradlew clean build` for a quicker build

# More Docs

See [documentation directory](documentation/README.md)
