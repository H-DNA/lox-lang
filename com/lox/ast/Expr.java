package com.lox.ast;

public abstract class Expr {
  public static class Binary extends Expr {
    public final Expr left;
    public final Token op;
    public final Expr right;

    public Binary(Expr left, Token op, Expr right) {
      this.left = left;
      this.op = op;
      this.right = right;
    }
  }

  public static class Unary extends Expr {
    public final Token op;
    public final Expr inner;

    public Unary(Token op, Expr inner) {
      this.op = op;
      this.inner = inner;
    }
  }

  public static class Literal extends Expr {
    public final Token value;

    public Literal(Token value) {
      this.value = value;
    }
  }

  public static class Primary extends Expr {
    public final Token value;

    public Primary(Token value) {
      this.value = value;
    }
  }

  public static class Grouping extends Expr {
    public final Expr inner;

    public Grouping(Expr inner) {
      this.inner = inner;
    }
  }
}
