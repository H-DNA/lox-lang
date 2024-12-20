package com.lox;

import java.util.ArrayList;
import java.util.List;

import com.lox.ast.Expr;
import com.lox.ast.Program;
import com.lox.ast.Stmt;
import com.lox.ast.Token;
import com.lox.ast.TokenType;
import com.lox.utils.Pair;

public class Parser {
  private final List<Token> tokens;
  private int currentOffset = 0;
  private Program program = new Program();
  private List<ParserException> errors = new ArrayList<>();

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public Pair<Program, List<ParserException>> parse() throws ParserException {
    if (this.isAtEnd()) return new Pair<>(this.program, this.errors);
    return new Pair<>(this.program, this.errors);
  }

  private boolean isAtEnd() {
    return this.currentOffset == tokens.size();
  }

  private Expr expression() {
    assert !this.isAtEnd();

    return this.equality();
  }

  private Expr equality() {
    assert !this.isAtEnd();

    Expr left = this.comparison();
    while (this.match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
      Token op = this.tokens.get(this.currentOffset - 1);
      Expr right = this.comparison();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr comparison() {
    assert !this.isAtEnd();

    Expr left = this.term();
    while (this.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token op = this.tokens.get(this.currentOffset - 1);
      Expr right = this.term();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr term() {
    assert !this.isAtEnd();

    Expr left = this.factor();
    while (this.match(TokenType.MINUS, TokenType.PLUS)) {
      Token op = this.tokens.get(this.currentOffset - 1);
      Expr right = this.factor();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr factor() {
    assert !this.isAtEnd();

    Expr left = this.unary();
    while (this.match(TokenType.STAR, TokenType.SLASH)) {
      Token op = this.tokens.get(this.currentOffset - 1);
      Expr right = this.unary();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private boolean match(TokenType... types) {
    assert !this.isAtEnd();

    final TokenType currentType = this.tokens.get(this.currentOffset).type;
    for (TokenType type: types) {
      if (type.equals(currentType)) {
        this.currentOffset += 1;
        return true;
      }
    }
    return false;
  }
}
