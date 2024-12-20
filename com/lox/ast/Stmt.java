package com.lox.ast;

public abstract class Stmt {
  public static class ExprStmt extends Stmt {
    public final Expr expr;

    public ExprStmt(Expr expr) {
      this.expr = expr;
    }
  }

  public static class PrintStmt extends Stmt {
    public final Expr expr;

    public PrintStmt(Expr expr) {
      this.expr = expr;
    }
  }
}
