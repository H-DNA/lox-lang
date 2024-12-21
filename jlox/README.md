# jlox

A Java reimplementation of the Lox programming language, with some modifications to the core grammar and tokenizer listed below.

## Specification

### Tokens

### Grammar

## Approach

Abstract only when needed:
  - On the `SyntaxNode`s, instead of implementing the Visitor pattern as in the book, I just pattern match on the types. I think this is fine, as the number of `SyntaxNode` classes is quite fixed. Using pattern matching, related functions that perform pattern matching need to be changed when new constructs are introduced. However, in such cases, the Visitor would need to be changed anyways, so I don't think the pattern is really worth the complexity.
  - Some abstractions are avoided, such as abstractions that are supposed to be created only becaused it is reused frequently. This leads to some code duplications (see [Scanner](./com/lox/Scanner.java)). However, the duplications are local in a file (better yet, in a method). Thus, it's still manageable while still making room for further improvements.
