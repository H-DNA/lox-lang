package com.lox.ast;

import java.util.List;

public abstract class Stmt extends SyntaxNode {
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

  public static class DeclStmt extends Stmt {
    public final Token id;
    public final Expr expr;

    public DeclStmt(Token id, Expr expr) {
      this.id = id;
      this.expr = expr;
    }
  }

  public static class IfStmt extends Stmt {
    public final Expr cond;
    public final Stmt thenBranch;
    public final Stmt elseBranch;

    public IfStmt(Expr cond, Stmt thenBranch, Stmt elseBranch) {
      this.cond = cond;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }
  }

  public static class BlockStmt extends Stmt {
    public final List<Stmt> stmts;

    public BlockStmt(List<Stmt> stmts) {
      this.stmts = stmts;
    }
  }
}
