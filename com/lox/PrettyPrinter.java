package com.lox;

import com.lox.ast.Expr;

public class PrettyPrinter {
  public String printExpr(Expr expr) {
    return switch (expr) {
      case Expr.Unary u -> String.format("(%s %s)", u.op, this.printExpr(u.inner));
      case Expr.Binary b -> String.format("(%s %s %s)", b.op, this.printExpr(b.left), this.printExpr(b.right));
      case Expr.Literal l -> l.value.lexeme;
      case Expr.Grouping g -> String.format("(group %s)", this.printExpr(g.inner));
      default -> throw new Error("Non-exhaustive check");
    };
  }
}
