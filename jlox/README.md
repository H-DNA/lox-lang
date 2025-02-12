# jlox

A Java reimplementation of the Lox programming language, with some modifications to the core grammar and tokenizer listed below.

## Design choices

### Philosophy

- Code as you go - go back and abstract if it's needed.
- Some duplication is acceptable - I don't want to strictly follow DRY if it only means to reduce some duplication.

### Supporting `this`

### Supporting `super`

## Specification

This is the specification of the Lox programming language, introduced in [Crafting interprerters](https://craftinginterpreters.com/). There are some modifications to the semantics.

### Tokens

#### Keywords

Keywords are reserved identifiers and have specific meanings. Usage outside of these specific meaning is not allowed.

```
and class else
false for fun
if nil or
print return true
var while super this
```

#### Comment

Only single-line comments are supported:

```
// this is a comment
```

#### Number literals

```
[0-9]*.?[0-9]*
```

The integral part is mandatory. The decimal dot is optional. The decimal part is also optional.

Examples:

```
1.0
1.
1
```

#### String literals

```
"[^"]*"
```

Escape sequences are not supported.

#### Boolean literals

Boolean literals are the keywords: `true` and `false`.

#### User-defined identifiers

User-defined identifiers are sequences of underscores, digits and letters. They must not start with a digit.

```
this_is_an_identifier
_this_is_too
_th1s_also_1s
```

#### Operator

```
+ - * / .
! = != < <= >= >
```

#### Delimiters

```
; ( ) [ ] { }
```

### Syntax

#### Grammar

```
program ::= declaration*
declaration ::= varDeclaration | functionDeclaration | classDeclaration | statement
statement ::= blockStatement | ifStatement | whileStatement | forStatement | printStatement | returnStatement | expressionStatement
classDeclaration ::= "class" identifier (< identifier)? "{" functionDeclaration* "}"
functionDeclaration ::= "fun" identifier "(" (identifier (, identifier)*)? ")" blockStatement
varDeclaration ::= "var" identifier (= expression)? ;
blockStatement ::= "{" declaration* "}"
whileStatement ::= "while" "(" expression ')" statement
forStatement ::= "for" "(" (varDeclaration | expressionStatement) expressionStatement expression ")" statement
ifStatement ::= "if" "(" expression ")" statement ("else" statement)?
returnStatement ::= "return" expression :
printStatement ::= "print" expression ;
expressionStatement ::= expression ;
expression ::= assignment | setExpression | logicalOr
assignment ::= variable = expression | getExpression = expression
logicalOr ::= logicalAnd | logicalOr "or" logicalAnd
logicalAnd ::= equality | logicalAnd "and" equality
equality ::= comparison | equality (== | !=) comparison
comparison ::= term | comparison (> | >= | < | <=) term
term ::= factor | term (+ | -) factor
factor ::= unary | factor (* | /) unary
unary ::= call | (! | -) unary
call ::= primary | call ("(" (expression (, expression)*)? ")" | . identifier)
primary ::= number | string | "true" | "false" | "nil" | "(" expression ")" | identifier | super | "this"
super ::= "super" "(" (expression (, expression)*)? ")" | "super" . identifier
```

#### Variables

#### Functions

#### Classes

### Type system

### Semantics

#### Scoping

#### `this`

#### Inheritance

#### `super`
