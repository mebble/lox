# Lox

Implementing the Lox programming language through this [book](https://craftinginterpreters.com/).

## Development

### jlox

- `cd jlox`
- `./gradlew test` to run tests
- `./gradlew build` to build uber jar
- `java -jar build/libs/jlox-fat.jar` to run the uber jar


## Implementation Conventions

- Program exit codes: [sysexits.h](https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=0&manpath=FreeBSD+4.3-RELEASE&format=html)
