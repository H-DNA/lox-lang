package com.lox;

import java.util.ArrayList;
import java.util.List;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;
import com.lox.ast.Token;
import com.lox.ast.TokenType;
import com.lox.ast.Stmt.DeclStmt;
import com.lox.ast.Stmt.ExprStmt;
import com.lox.ast.Stmt.IfStmt;
import com.lox.ast.Stmt.PrintStmt;
import com.lox.utils.Pair;

public class Parser {
  private final List<Token> tokens;
  private int currentOffset = 0;
  private List<Stmt> stmts = new ArrayList<Stmt>();
  private List<ParserException> errors = new ArrayList<>();

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  public Pair<List<Stmt>, List<ParserException>> parse() {
    if (this.isAtEnd()) return new Pair<>(this.stmts, this.errors);
    while (!this.isAtEnd() && !this.match(TokenType.EOF)) {
      try {
        this.stmts.add(this.declaration());
      } catch (SynchronizationException e) {
        this.synchronizeStatement();
      }
    }
    return new Pair<>(this.stmts, this.errors);
  }

  private void synchronizeStatement() {
    assert !this.isAtEnd();

    while (!this.isAtEnd() && !this.match(TokenType.RIGHT_BRACE, TokenType.SEMICOLON)) {
      this.currentOffset += 1;
    }
  } 

  private boolean isAtEnd() {
    return this.currentOffset == tokens.size();
  }

  private void synchronizeSimpleStatement() {
    assert !this.isAtEnd();

    while (!this.isAtEnd() && !this.match(TokenType.SEMICOLON)) {
      this.currentOffset += 1;
    }
  }

  private Stmt declaration() throws SynchronizationException {
    assert !this.isAtEnd();

    if (this.match(TokenType.VAR)) return this.varDeclaration();
    return this.statement();
  }

  private Stmt statement() throws SynchronizationException {
    assert !this.isAtEnd();

    if (this.match(TokenType.IF)) return this.ifStatement();
    if (this.match(TokenType.PRINT)) return this.printStatement();
    return this.expressionStatement();
  }

  private DeclStmt varDeclaration() throws SynchronizationException {
    assert !this.isAtEnd();

    assert this.previous().type == TokenType.VAR;

    if (!this.match(TokenType.IDENTIFIER)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect an identifier", invalidToken.startOffset, invalidToken.endOffset));
      throw new SynchronizationException();
    }

    final Token id = this.previous();

    if (this.match(TokenType.SEMICOLON)) {
      return new DeclStmt(id, null);
    }

    if (!this.match(TokenType.EQUAL)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect '=' or an ending semicolon ';'", invalidToken.startOffset, invalidToken.endOffset));
      throw new SynchronizationException();
    }
    
    final Expr expr = this.expression();

    if (!this.match(TokenType.SEMICOLON)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect an ending semicolon ';'", invalidToken.startOffset, invalidToken.endOffset));
      this.synchronizeSimpleStatement();
    }

    return new DeclStmt(id, expr);
  }

  private IfStmt ifStatement() throws SynchronizationException {
    assert !this.isAtEnd();

    assert this.previous().type == TokenType.IF;

    if (!this.match(TokenType.LEFT_PAREN)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect an opening parenthesis '('", invalidToken.startOffset, invalidToken.endOffset));
      throw new SynchronizationException();
    }

    final Expr cond = this.expression();

    if (!this.match(TokenType.RIGHT_PAREN)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect a closing parenthesis ')'", invalidToken.startOffset, invalidToken.endOffset));
      throw new SynchronizationException();
    }

    final Stmt thenBranch = this.statement();

    if (!this.match(TokenType.ELSE)) {
      return new IfStmt(cond, thenBranch, null);
    }

    final Stmt elseBranch = this.statement();

    return new IfStmt(cond, thenBranch, elseBranch);
  }

  private PrintStmt printStatement() throws SynchronizationException {
    assert !this.isAtEnd();

    assert this.previous().type == TokenType.PRINT;

    final Expr expr = this.expression();
    if (!this.match(TokenType.SEMICOLON)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect an ending semicolon ';'", invalidToken.startOffset, invalidToken.endOffset));
      this.synchronizeSimpleStatement();
    }

    return new PrintStmt(expr);
  }

  private ExprStmt expressionStatement() throws SynchronizationException {
    assert !this.isAtEnd();

    final Expr expr = this.expression();
    if (!this.match(TokenType.SEMICOLON)) {
      final Token invalidToken = this.current();
      this.errors.add(new ParserException("Expect an ending semicolon ';'", invalidToken.startOffset, invalidToken.endOffset));
      this.synchronizeSimpleStatement();
    }

    return new ExprStmt(expr);
  }

  private Expr expression() throws SynchronizationException {
    assert !this.isAtEnd();

    return this.equality();
  }

  private Expr equality() throws SynchronizationException {
    assert !this.isAtEnd();

    Expr left = this.comparison();
    while (this.match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)) {
      Token op = this.previous();
      Expr right = this.comparison();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr comparison() throws SynchronizationException {
    assert !this.isAtEnd();

    Expr left = this.term();
    while (this.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token op = this.previous();
      Expr right = this.term();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr term() throws SynchronizationException {
    assert !this.isAtEnd();

    Expr left = this.factor();
    while (this.match(TokenType.MINUS, TokenType.PLUS)) {
      Token op = this.previous();
      Expr right = this.factor();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr factor() throws SynchronizationException {
    assert !this.isAtEnd();

    Expr left = this.unary();
    while (this.match(TokenType.STAR, TokenType.SLASH)) {
      Token op = this.previous();
      Expr right = this.unary();
      left = new Expr.Binary(left, op, right);
    }
    return left;
  }

  private Expr unary() throws SynchronizationException {
    assert !this.isAtEnd();

    if (this.match(TokenType.BANG, TokenType.MINUS)) {
      Token op = this.previous();
      Expr inner = this.unary();
      return new Expr.Unary(op, inner);
    }

    return this.primary();
  }

  private Expr primary() throws SynchronizationException {
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
    } else if (this.match(TokenType.NIL)) {
      final Token nilToken = this.previous();
      return new Expr.Literal(nilToken);
    } else if (this.match(TokenType.LEFT_PAREN)) {
      final Expr inner = this.expression();
      if (!this.match(TokenType.RIGHT_PAREN)) {
        final Token invalidToken = this.current();
        this.errors.add(new ParserException("Expect a closing parenthesis ')'", invalidToken.startOffset, invalidToken.endOffset));
        this.synchronizeGrouping();
      }
      return new Expr.Grouping(inner);
    } else if (this.match(TokenType.IDENTIFIER)) {
      final Token identifier = this.previous();
      return new Expr.Variable(identifier);
    }

    this.errors.add(new ParserException("Expect a numeric literal, string literal, variable or grouping expression", this.current().startOffset, this.current().endOffset));
    throw new SynchronizationException();
  }

  private void synchronizeGrouping() throws SynchronizationException {
    assert !this.isAtEnd();

    while (!this.isAtEnd() && !this.dryMatch(TokenType.RIGHT_PAREN, TokenType.RIGHT_BRACE, TokenType.SEMICOLON)) {
      this.currentOffset += 1;
    }

    if (this.isAtEnd() || !this.match(TokenType.RIGHT_PAREN)) {
      throw new SynchronizationException();
    }
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
    if (this.isAtEnd()) return false;

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

class SynchronizationException extends Exception {}
