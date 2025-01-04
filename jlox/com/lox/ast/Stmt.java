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

  public static class ReturnStmt extends Stmt {
    public final Expr expr;

    public ReturnStmt(Expr expr) {
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

  public static class FuncStmt extends Stmt {
    public final Token name;
    public final List<Token> params;
    public final BlockStmt body;

    public FuncStmt(Token name, List<Token> params, BlockStmt body) {
      this.name = name;
      this.params = params;
      this.body = body;
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

  public static class WhileStmt extends Stmt {
    public final Expr cond;
    public final Stmt body;

    public WhileStmt(Expr cond, Stmt body) {
      this.cond = cond;
      this.body = body;
    }
  }

  public static class ForStmt extends Stmt {
    public final Stmt init;
    public final ExprStmt cond;
    public final Expr post;
    public final Stmt body;

    public ForStmt(Stmt init, ExprStmt cond, Expr post, Stmt body) {
      assert init instanceof DeclStmt || init instanceof ExprStmt;
      this.init = init;
      this.cond = cond;
      this.post = post;
      this.body = body;
    }
  }

  public static class BlockStmt extends Stmt {
    public final List<Stmt> stmts;

    public BlockStmt(List<Stmt> stmts) {
      this.stmts = stmts;
    }
  }
}
