# jlox

A Java reimplementation of the Lox programming language, with some modifications to the core grammar and tokenizer listed below.

## Design choices

### Philosophy

- Code as you go - go back and abstract if it's needed.
- Some duplication is acceptable - I don't want to strictly follow DRY if it only means to reduce some duplication.

### Supporting `this`

`this` has some special semantic: right at function (method) definition, it's unknown which value it is bound to.

When does `this` get bound? We know that `this` must at least be bound when we call `object.method()`. Preferably, I want binding of `this` to persist in this case (sidenote: JS differs in this regard):
```
var method = object.method;
method(); // valid
```

That means the access expression `object.method` must do something special: It must return `method`, with `this` bound to `object`. Therefore, we have answered the first question, `this` is bound when we call `object.method`.

The next question is how to bind & evaluate `this` in `method`? I'm pretty positive that `this`, although being a keyword, behaves just like a normal identifier. Therefore, it's seem natural that `this` should be bound using the same mechanism as normal identifiers. Currently, bindings of identifiers to values are stored inside the function's environment. Therefore, `object.method` should return a function with binding of (`this`, `object`) injected into the function environment.

### Supporting `super`

`super` can be bound right at function definition. `super` also behaves mostly like normal identifiers, but must appear in more restricted forms: `super.prop` or `super()`. Using the same line of thought, I decided that `object.method` should also inject something into the returned function, specifically (`super`, `superclass`). However, if we evaluate `super` like normal identifiers, `super.method()` would return `superclass.method()`, which is wrong. Therefore, the interpreter has to handle `super` specially: Exclusive logic to handle `super.prop` and `super()`:
- The interpreter lookups the binding for `super` to get the superclass.
- The interpreter lookup `prop` (or `constructor`) from the superclass.
- The interpreter returns the looked up `prop` for `super.prop` or call the constructor for `super()`.

### Early resolving identifiers

See the bug in the **Scoping** section below.

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
unary ::= primary | call | getExpression | (! | -) unary
call ::= (call | getExpression | primary) ("(" (expression (, expression)*)? ")"
getExpression ::= (call | getExpression | primary) . identifier
primary ::= number | string | "true" | "false" | "nil" | "(" expression ")" | identifier | super | "this"
super ::= "super" "(" (expression (, expression)*)? ")" | "super" . identifier
```

#### Variables

Variables can be defined using the `var` construct:

```
var v = 10;
var t; // same as `var t = nil`
```

Redefinition is an error:

```
var v = 10;
var v = 3;
```

Use of a variable before definition is an error:

```
var v = non_existent;
```

#### Print statement

The `print` statement can be used to output a value to stdout:

```
var a = "Hello World!";
print a;
// Hello World!
```

#### Functions

Function can be defined using the `fun` construct:

```
fun f(x, y) {
  return x + y;
}
```

Recursive or mutually recursive functions can be defined:

```
fun f() {
  g();
}

fun g() {
  f();
}
```

#### Control flow

Conditional control flow can be implemented using the `if` construct:

```
if (1) {
  print true; 
}
```

Only `false` and `nil` are falsy. All other values are truthy.

While loops can be implemented using the `while` construct:

```
while (true) {
  do_something();
}
```

For loops can be implemented using the `for` construct:

```
for (var i = 3; i < 10; i = i + 1) {
   print i;
}

var t;
for (t = 10; t >= 0; t = t - 1) {
   print t;
} 
```

#### Classes

Classes can be defined using the `class` construct:

```
class A {
  fun constructor() {
     this.a = 3;
  }
  fun getA() {
     return this.a;
  }
  fun setA(a) {
     this.a = a;
  }
}
```

Inheritance is specified using the `<` symbol:

```
class B {
  fun constructor() {
     super();
     this.b = 3;
  }
  fun setB(a, b) {
     super.setA(a);
     this.b = b;
  }
}
```

Only single inheritance is supported.

### Type system

In Lox, everything is an object, even `nil`. That is, you can access members or call methods on anything:
```
(3).toString()
"lox".toString()
```

The primitive types are: `Nil`, `Number`, `String`, `Boolean`. They are immutable, in the sense that no mutation can be made on these objects.
```
(3).a = 3; // error
```

Functions and classes are of type `Function` and `Class` accordingly.

New types can be defined using the `class` construct.

Every class is a subclass of `Object`.

### Semantics

#### Scoping

Lox follows lexical scoping.

```
var t;
var b = 10;
{
  var a = 3;
  fun f() {
     print a;
     print b;
  }
  t = f();
}
var a = 4;
t();  // 3 10
b = 2;
t();  // 3 2
```

Bug: Names are not early resolved, but are looked up on demand. This results in weird behavior:  Definition of a non-local variables can lead to the same name resolves to different variables in different scope:
```
var i = 3;
fun f() {
  fun g() {
     print i;
  }
  g(); // 3
  var i = "another variable";
  g(); // another variable
}
```
This is mostly considered undesired behavior.

#### `this`

The keyword `this` is an expression that resolves to the owner object inside a method. Outside of a method, accessing `this` would throw an error.

Methods can be extracted out and `this` still refers to the correct object:
```
class A {
  fun f() {
     this.a = 3;
  }
  fun g() {
     print this.a;
  }
}

var a = A();
var f = a.f;
var g = a.g;
f();
g(); // 3
```

#### Inheritance

A class can inherit methods from other classes called superclasses. Method lookup of a class traverses the ancestor chain, starting from the class itself, and return the first method with the desired name that it finds.

Methods in the subclass will shadow methods in the superclass with the same name.

Object fields in classes do not exist in different namespaces per class. This means that both `a` in these two constructors are the same:
```
class A {
  fun constructor() {
    this.a = 3;
  }
}

class B < A {
  fun constructor() {
    this.a = 10;
  }
}
```

#### `super`

The keyword `super` is used when you want to call a method of a superclass.
```
super(); // call the superclass's constructor
super.method; // return the first method in the ancestor chain starting from the current immediate superclass
```
