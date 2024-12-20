package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.Program;
import com.lox.ast.SyntaxNode;

public class Interpreter {
  public Object evaluate(SyntaxNode node) {
    return new Object();
  }

  public Object evaluateExpr(Expr expr) {
    return switch(expr) {
      case Expr.Binary b -> this.evaluateBinary(b);
      case Expr.Unary u -> this.evaluateUnary(u);
      case Expr.Grouping g -> this.evaluateGrouping(g);
      case Expr.Variable v -> this.evaluateVariable(v);
      case Expr.Literal l -> this.evaluateLiteral(l);
      default -> throw new Error("Non-exhaustive check");
    };
  }

  public Object evaluateBinary(Expr.Binary bin) {
    throw new Error("Unimplemented");
  }

  public Object evaluateUnary(Expr.Unary un) {
    throw new Error("Unimplemented");
  }

  public Object evaluateGrouping(Expr.Grouping gr) {
    throw new Error("Unimplemented");
  }

  public Object evaluateLiteral(Expr.Literal lit) {
    throw new Error("Unimplemented");
  }

  public Object evaluateVariable(Expr.Variable var) {
    throw new Error("Unimplemented");
  }
}
