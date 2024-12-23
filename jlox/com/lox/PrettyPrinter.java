package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;

public class PrettyPrinter {
  public String printStmt(Stmt stmt) {
    return switch (stmt) {
      case Stmt.DeclStmt d -> d.expr == null ? String.format("(define %s)", d.id.lexeme) : String.format("(define %s %s)", d.id.lexeme, this.printExpr(d.expr));
      case Stmt.ExprStmt e -> this.printExpr(e.expr);
      case Stmt.PrintStmt p -> String.format("(print %s)", this.printExpr(p.expr));
      default -> throw new Error("Non-exhaustive check");
    };
  }

  public String printExpr(Expr expr) {
    return switch (expr) {
      case Expr.Unary u -> String.format("(%s %s)", u.op.lexeme, this.printExpr(u.inner));
      case Expr.Binary b -> String.format("(%s %s %s)", b.op.lexeme, this.printExpr(b.left), this.printExpr(b.right));
      case Expr.Literal l -> l.value.lexeme;
      case Expr.Grouping g -> String.format("(group %s)", this.printExpr(g.inner));
      case Expr.Variable v -> String.format("%s", v.var.lexeme);
      default -> throw new Error("Non-exhaustive check");
    };
  }
}
