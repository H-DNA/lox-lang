package com.lox;

public class Token {
  final TokenType type;
  final String lexeme;
  final Object literal; // value of `lexeme` interpreted as `type` in the program

  final int startOffset;
  final int endOffset;

  public Token(TokenType type, String lexeme, Object literal, int startOffset, int endOffset) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }
}
