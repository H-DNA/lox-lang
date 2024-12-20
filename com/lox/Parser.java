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

  public Pair<Program, List<ParserException>> parse() {
    if (this.isAtEnd()) return new Pair<>(this.program, this.errors);
    return new Pair<>(this.program, this.errors);
  }

  private boolean isAtEnd() {
    return this.currentOffset == tokens.size();
  }

  private Expr expression() throws ParserException {
    assert !this.isAtEnd();

    return this.equality();
  }

  private Expr equality() throws ParserException {
    assert !this.isAtEnd();

    Expr left = this.comparison();
    while (this.match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
      Token op = this.previous();
      Expr right = this.comparison();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr comparison() throws ParserException {
    assert !this.isAtEnd();

    Expr left = this.term();
    while (this.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token op = this.previous();
      Expr right = this.term();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr term() throws ParserException {
    assert !this.isAtEnd();

    Expr left = this.factor();
    while (this.match(TokenType.MINUS, TokenType.PLUS)) {
      Token op = this.previous();
      Expr right = this.factor();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr factor() throws ParserException {
    assert !this.isAtEnd();

    Expr left = this.unary();
    while (this.match(TokenType.STAR, TokenType.SLASH)) {
      Token op = this.previous();
      Expr right = this.unary();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr unary() throws ParserException {
    assert !this.isAtEnd();

    if (this.match(TokenType.BANG, TokenType.MINUS)) {
      Token op = this.previous();
      Expr inner = this.unary();
      return new Expr.Unary(op, inner);
    }

    return this.primary();
  }

  private Expr primary() throws ParserException {
    assert !this.isAtEnd();

    if (this.match(TokenType.NUMBER)) {
      final Token number = this.previous();
      return new Expr.Literal(number);
    } else if (this.match(TokenType.STRING)) {
      final Token string = this.previous();
      return new Expr.Literal(string);
    } else if (this.match(TokenType.TRUE)) {
      final Token trueToken = this.previous();
      return new Expr.Literal(trueToken);
    } else if (this.match(TokenType.FALSE)) {
      final Token trueToken = this.previous();
      return new Expr.Literal(trueToken);
    } else if (this.match(TokenType.LEFT_PAREN)) {
      final Expr inner = this.expression();
      if (!this.match(TokenType.RIGHT_PAREN)) this.synchronizeGrouping();
      return new Expr.Grouping(inner);
    }

    throw new ParserException("Expect a numeric literal, string literal, variable or grouping expression", this.current().startOffset, this.current().endOffset);
  }

  private void synchronizeGrouping() throws ParserException {
    assert !this.isAtEnd();

    Token invalidToken = this.current();
    while (!this.isAtEnd() && !this.dryMatch(TokenType.RIGHT_PAREN, TokenType.RIGHT_BRACE, TokenType.SEMICOLON)) {
      this.currentOffset += 1;
    }

    ParserException exception = new ParserException("Expect a closing parenthesis ')'", invalidToken.startOffset, invalidToken.endOffset);
    if (this.isAtEnd() || !this.dryMatch(TokenType.RIGHT_PAREN)) {
      throw exception;
    }
    this.errors.add(exception);
  }

  private Token previous() {
    assert this.currentOffset <= this.tokens.size();

    return this.tokens.get(this.currentOffset - 1);
  }

  private Token current() {
    assert !this.isAtEnd();

    return this.tokens.get(this.currentOffset);
  }

  // Match without advancing the current offset
  private boolean dryMatch(TokenType ...types) {
    assert !this.isAtEnd();

    final TokenType currentType = this.tokens.get(this.currentOffset).type;
    for (TokenType type: types) {
      if (type.equals(currentType)) {
        return true;
      }
    }
    return false;
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
