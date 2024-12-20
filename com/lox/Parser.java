package com.lox;

import java.util.ArrayList;
import java.util.List;

import com.lox.ast.Program;
import com.lox.ast.Stmt;
import com.lox.ast.Token;
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
}
