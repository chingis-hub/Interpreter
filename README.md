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

## Language at a glance

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

After the program finishes, all top-level variables are printed in declaration order:
```
x: 10
result: 120
```

## Design decisions

| Question | Decision |
|----------|----------|
| Number type | `Double`; printed without `.0` when the value is whole |
| Division | Always float — `5 / 2 = 2.5` |
| `while` body | Consumes all remaining comma-separated statements in the current context |
| `if` branches | Each branch is exactly one statement; a comma ends it |
| Function scope | Fresh scope with access to globals; no closures |
| Missing `return` | Function returns `0` implicitly |
| Errors | Written to stderr with line number |

## Architecture

```
stdin → Lexer → Parser → Interpreter → stdout
```

- **Lexer** — tokenises source; newlines delimit top-level statements
- **Parser** — recursive descent; propagates a terminator set so `while` bodies are greedy and `if` branches are not
- **Interpreter** — tree-walking evaluator; `return` uses a `ReturnSignal` exception for control flow
