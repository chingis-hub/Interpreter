# Interpreter

A tree-walking interpreter for a small custom programming language, written in Kotlin.

## Run

Edit `program.txt`, then run it from the terminal:

**macOS / Linux**
```bash
./gradlew -q run --console=plain < program.txt
```

**Windows (PowerShell)**
```powershell
Get-Content program.txt | .\gradlew.bat -q run --console=plain
```

## Test

```bash
./gradlew test
```

## Language

### Basics

```
# variables
x = 10
y = (x + 2) * 3

# if / else
if x > 5 then y = 1 else y = 0

# while — comma separates multiple statements in the body
while x > 0 do y = y + x, x = x - 1

# functions (recursive)
fun fact(n) { if n <= 0 then return 1 else return n * fact(n - 1) }
result = fact(5)
```

Operators: `+ - * / %` · `== != < > <= >=` · `and or not`

`and` and `or` short-circuit: the right operand is not evaluated if the left determines the result.

After the program finishes, all top-level variables are printed in declaration order:
```
x: 10
result: 120
```

### Built-in functions

| Function | Description |
|---|---|
| `print(x)` | Prints `x` to stdout; returns `x` |
| `abs(x)` | Absolute value |
| `floor(x)` | Floor (round toward negative infinity) |
| `sqrt(x)` | Square root |

### Errors

Errors are written to stderr with source location:
```
[line 3:7] undefined variable 'y'
[line 5:1] division by zero
```

## Design decisions

| Question | Decision |
|----------|----------|
| Number type | `Double`; printed without `.0` when the value is whole |
| Division | Always float — `5 / 2 = 2.5` |
| `and` / `or` | Short-circuit — right side skipped when result is determined by left |
| `while` body | Consumes all remaining comma-separated statements in the current context |
| `if` branches | Each branch is exactly one statement; a comma or `else` ends it |
| Function scope | Fresh scope with access to globals; no closures |
| Missing `return` | Function returns `0` implicitly |
| Call stack | Capped at 500 frames; exceeding it throws a runtime error |
| Errors | Written to stderr with `[line L:C]` location |

## Architecture

```
stdin → lex() → parse() → evaluate() → stdout
```

Each pipeline stage is an independent function in `Main.kt` and can be called separately:

```kotlin
fun lex(source: String): List<Token>        // throws LexerError
fun parse(tokens: List<Token>): List<Stmt>  // throws ParseError
fun evaluate(stmts: List<Stmt>): String     // throws RuntimeError
fun interpret(source: String): String       // chains all three
```

### Package layout

```
lexer/      Token, Lexer
ast/        Expr, Stmt  (sealed class hierarchies)
parser/     Parser
runtime/    Value, Environment, Interpreter, Builtins
error/      InterpreterError (sealed), LexerError, ParseError, RuntimeError
```

- **Lexer** — tokenises source; tracks line and column; newlines delimit top-level statements
- **Parser** — recursive descent; propagates a terminator set so `while` bodies are greedy and `if` branches are not
- **Interpreter** — tree-walking evaluator; `return` uses a `ReturnSignal` throwable for control flow; built-ins are registered as `Value.Native` entries in the global environment
- **Error hierarchy** — all errors extend `InterpreterError(message, line, col)`; `Main.kt` has a single catch that formats the location
