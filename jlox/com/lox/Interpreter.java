package com.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;
import com.lox.ast.TokenType;
import com.lox.object.LoxBoolean;
import com.lox.object.LoxNil;
import com.lox.object.LoxNumber;
import com.lox.object.LoxObject;
import com.lox.object.LoxString;

public class Interpreter {
  private Environment env = new Environment();

  public Interpreter() {}

  public void evaluate(List<Stmt> stmts) throws InterpreterException {
    for (Stmt stmt: stmts) {
      this.evaluateStmt(stmt);
    }
  }

  public LoxObject evaluateStmt(Stmt stmt) throws InterpreterException {
    return switch(stmt) {
      case Stmt.PrintStmt p -> {
        System.out.println(StringifyUtils.stringify(this.evaluateExpr(p.expr)));
        yield new LoxNil();
      }
      case Stmt.ExprStmt e -> this.evaluateExpr(e.expr);
      case Stmt.DeclStmt d -> {
        this.env.define(d.id.lexeme, this.evaluateExpr(d.expr));
        yield new LoxNil();
      }
      default -> throw new Error("Non-exhaustive check");
    };
  }

  public LoxObject evaluateExpr(Expr expr) throws InterpreterException {
    return switch(expr) {
      case Expr.Binary b -> this.evaluateBinary(b);
      case Expr.Unary u -> this.evaluateUnary(u);
      case Expr.Grouping g -> this.evaluateGrouping(g);
      case Expr.Variable v -> this.evaluateVariable(v);
      case Expr.Literal l -> this.evaluateLiteral(l);
      default -> throw new Error("Non-exhaustive check");
    };
  }

  private LoxObject evaluateBinary(Expr.Binary bin) throws InterpreterException {
    final LoxObject left = this.evaluateExpr(bin.left);
    final LoxObject right = this.evaluateExpr(bin.right);
    return switch (bin.op.type) {
      case TokenType.PLUS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '+' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber)left).value + ((LoxNumber)right).value);
      }
      case TokenType.MINUS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '-' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber)left).value - ((LoxNumber)right).value);
      }
      case TokenType.STAR -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '*' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber)left).value * ((LoxNumber)right).value);
      }
      case TokenType.SLASH -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '+' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber)left).value / ((LoxNumber)right).value);
      }
      case TokenType.EQUAL_EQUAL -> {
        if (!TypecheckUtils.isSameType(left, right)) {
          yield new LoxBoolean(false);
        }
        yield new LoxBoolean(left.value().equals(right.value()));
      }
      case TokenType.BANG_EQUAL -> {
        if (!TypecheckUtils.isSameType(left, right)) {
          yield new LoxBoolean(true);
        }
        yield new LoxBoolean(!left.value().equals(right.value()));
      }
      case TokenType.LESS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '<' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxBoolean(((LoxNumber)left).value < ((LoxNumber)right).value);
      }
      case TokenType.LESS_EQUAL -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '<=' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxBoolean(((LoxNumber)left).value <= ((LoxNumber)right).value);
      }
      case TokenType.GREATER -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '>' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxBoolean(((LoxNumber)left).value > ((LoxNumber)right).value);
      }
      case TokenType.GREATER_EQUAL -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '>=' on %s and %s", TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxBoolean(((LoxNumber)left).value >= ((LoxNumber)right).value);
      }
      default -> throw new Error(String.format("Unreachable: Unexpected binary operator '%s'", bin.op.lexeme));
    };
  }

  private LoxObject evaluateUnary(Expr.Unary un) throws InterpreterException {
    final LoxObject inner = this.evaluateExpr(un.inner);
    return switch(un.op.type) {
      case TokenType.BANG -> {
        if (TypecheckUtils.isBoolean(inner)) {
          yield new LoxBoolean(!((LoxBoolean)inner).value);
        }
        yield new LoxBoolean(false);
      }
      case TokenType.MINUS -> {
        if (!TypecheckUtils.isNumber(inner)) {
          throw new InterpreterException(String.format("Unsupported operator '-' on %s", TypecheckUtils.typenameOf(inner)));
        }
        yield new LoxNumber(-((LoxNumber)inner).value);
      }
      default -> throw new Error(String.format("Unreachable: Unexpected unary operator '%s'", un.op.lexeme));
    };
  }

  private LoxObject evaluateGrouping(Expr.Grouping gr) throws InterpreterException {
    return this.evaluateExpr(gr.inner);
  }

  private LoxObject evaluateLiteral(Expr.Literal lit) {
    return switch (lit.value.literal) {
      case Double d -> new LoxNumber(d);
      case String s -> new LoxString(s);
      case Boolean b -> new LoxBoolean(b);
      default -> throw new Error(String.format("Unreachable: Unexpected literal type"));
    };
  }

  private LoxObject evaluateVariable(Expr.Variable var) throws InterpreterException {
    return this.env.get(var.var.lexeme);
  }
}

class Environment {
  private final Map<String, LoxObject> values = new HashMap<>();

  public void define(String name, LoxObject value) {
    this.values.put(name, value);
  }

  public LoxObject get(String name) throws InterpreterException {
    if (values.containsKey(name)) {
      return this.values.get(name);
    }

    throw new InterpreterException("Undefined variable '" + name + "'");
  }
}

class TypecheckUtils {
  public static boolean isNumber(LoxObject obj) {
    return obj instanceof LoxNumber;
  }

  public static boolean isString(LoxObject obj) {
    return obj instanceof LoxString;
  }

  public static boolean isBoolean(LoxObject obj) {
    return obj instanceof LoxBoolean;
  }

  public static boolean isNil(LoxObject obj) {
    return obj instanceof LoxNil;
  }

  public static boolean isSameType(LoxObject obj1, LoxObject obj2) {
    return typenameOf(obj1) == typenameOf(obj2);
  }

  public static String typenameOf(LoxObject obj) {
    return switch(obj) {
      case LoxNumber n -> "number";
      case LoxString s -> "string";
      case LoxBoolean b -> "boolean";
      case LoxNil nil -> "nil";
      default -> "object";
    };
  }
}

class StringifyUtils {
  public static String stringify(LoxObject obj) {
    return switch(obj) {
      case LoxNumber n -> String.valueOf(n.value);
      case LoxString s -> s.value;
      case LoxBoolean b -> String.valueOf(b.value);
      case LoxNil nil -> "nil";
      default -> String.valueOf(obj);
    };
  }
}
