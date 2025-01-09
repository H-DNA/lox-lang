package com.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.ast.Expr;
import com.lox.ast.Stmt;
import com.lox.ast.TokenType;
import com.lox.ast.Expr.Variable;
import com.lox.ast.Stmt.FuncStmt;
import com.lox.object.BuiltinClasses;
import com.lox.object.LoxBoolean;
import com.lox.object.LoxCallable;
import com.lox.object.LoxClass;
import com.lox.object.LoxFunction;
import com.lox.object.LoxNil;
import com.lox.object.LoxNumber;
import com.lox.object.LoxObject;
import com.lox.object.LoxString;

public class Interpreter {
  private Environment globals = new Environment();
  public Environment env = this.globals;

  public Interpreter() throws InterpreterException {
    Builtins.bootstrapFunctions(this.globals);
  }

  public void evaluate(List<Stmt> stmts) throws InterpreterException {
    for (Stmt stmt : stmts) {
      this.evaluateStmt(stmt);
    }
  }

  public LoxObject evaluateStmt(Stmt stmt) throws InterpreterException {
    return switch (stmt) {
      case Stmt.PrintStmt p -> {
        System.out.println(this.evaluateExpr(p.expr).toString());
        yield LoxNil.singleton;
      }
      case Stmt.ExprStmt e -> this.evaluateExpr(e.expr);
      case Stmt.DeclStmt d -> {
        this.env.define(d.id.lexeme, d.expr == null ? LoxNil.singleton : this.evaluateExpr(d.expr));
        yield LoxNil.singleton;
      }
      case Stmt.IfStmt i -> {
        final LoxObject condValue = this.evaluateExpr(i.cond);
        if (ValuecheckUtils.isTruthy(condValue)) {
          yield this.evaluateStmt(i.thenBranch);
        } else {
          yield i.elseBranch != null ? this.evaluateStmt(i.elseBranch) : LoxNil.singleton;
        }
      }
      case Stmt.WhileStmt w -> {
        while (ValuecheckUtils.isTruthy(this.evaluateExpr(w.cond))) {
          this.evaluateStmt(w.body);
        }
        yield LoxNil.singleton;
      }
      case Stmt.ForStmt f -> {
        this.env = new Environment(this.env);
        this.evaluateStmt(f.init);
        while (ValuecheckUtils.isTruthy(this.evaluateStmt(f.cond))) {
          this.evaluateStmt(f.body);
          this.evaluateExpr(f.post);
        }
        this.env = this.env.parent;
        yield LoxNil.singleton;
      }
      case Stmt.BlockStmt b -> {
        this.env = new Environment(this.env);
        LoxObject lastValue = LoxNil.singleton;
        for (Stmt s : b.stmts) {
          lastValue = this.evaluateStmt(s);
        }
        this.env = this.env.parent;
        yield lastValue;
      }
      case Stmt.FuncStmt f -> {
        this.env.define(f.name.lexeme, new LoxFunction(f, this.env));
        yield LoxNil.singleton;
      }
      case Stmt.ReturnStmt r -> {
        throw new NonLocalJump.Return(this.evaluateExpr(r.expr));
      }
      case Stmt.ClsStmt c -> {
        List<LoxFunction> methods = new ArrayList<>();
        for (FuncStmt func: c.methods) {
          methods.add(new LoxFunction(func, this.env));
        }
        LoxClass cls = new LoxClass(c.name.lexeme, methods);
        this.env.define(c.name.lexeme, cls);
        yield LoxNil.singleton;
      }
      default -> throw new Error("Non-exhaustive check");
    };
  }

  public LoxObject evaluateExpr(Expr expr) throws InterpreterException {
    return switch (expr) {
      case Expr.Binary b -> this.evaluateBinary(b);
      case Expr.Unary u -> this.evaluateUnary(u);
      case Expr.Grouping g -> this.evaluateGrouping(g);
      case Expr.Variable v -> this.evaluateVariable(v);
      case Expr.Literal l -> this.evaluateLiteral(l);
      case Expr.Call c -> {
        LoxObject callee = this.evaluateExpr(c.callee);
        List<LoxObject> arguments = new ArrayList<>();
        for (Expr arg : c.params) {
          arguments.add(this.evaluateExpr(arg));
        }
        if (!TypecheckUtils.isCallable(callee)) {
          throw new InterpreterException("Callee is not of Callable type");
        }
        LoxCallable function = (LoxCallable) callee;
        if (function.arity() != arguments.size()) {
          throw new InterpreterException(
              String.format("Arity mismatch: Expected %s but got %s", function.arity(), arguments.size()));
        }
        yield function.call(this, arguments);
      }
      case Expr.Get g -> this.evaluateExpr(g.object).get(g.property.lexeme);
      case Expr.Set s -> {
        final LoxObject value = this.evaluateExpr(s.value);
        this.evaluateExpr(s.object).set(s.property.lexeme, value);
        yield value;
      }
      default -> throw new Error("Non-exhaustive check");
    };
  }

  private LoxObject evaluateBinary(Expr.Binary bin) throws InterpreterException {
    if (bin.op.type == TokenType.EQUAL) {
      final LoxObject right = this.evaluateExpr(bin.right);
      this.env.assign(((Variable) bin.left).var.lexeme, right);
      return right;
    }
    if (bin.op.type == TokenType.OR) {
      final LoxObject left = this.evaluateExpr(bin.left);
      if (ValuecheckUtils.isTruthy(left)) {
        return left;
      }
      final LoxObject right = this.evaluateExpr(bin.right);
      return right;
    }
    if (bin.op.type == TokenType.AND) {
      final LoxObject left = this.evaluateExpr(bin.left);
      if (ValuecheckUtils.isFalsy(left)) {
        return left;
      }
      final LoxObject right = this.evaluateExpr(bin.right);
      return right;
    }
    final LoxObject left = this.evaluateExpr(bin.left);
    final LoxObject right = this.evaluateExpr(bin.right);
    return switch (bin.op.type) {
      case TokenType.PLUS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '+' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber) left).value + ((LoxNumber) right).value);
      }
      case TokenType.MINUS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '-' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber) left).value - ((LoxNumber) right).value);
      }
      case TokenType.STAR -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '*' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber) left).value * ((LoxNumber) right).value);
      }
      case TokenType.SLASH -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '/' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield new LoxNumber(((LoxNumber) left).value / ((LoxNumber) right).value);
      }
      case TokenType.EQUAL_EQUAL -> {
        if (!TypecheckUtils.isSameType(left, right)) {
          yield ValuecheckUtils.getBooleanSingleton(false);
        }
        yield ValuecheckUtils.getBooleanSingleton(left.value().equals(right.value()));
      }
      case TokenType.BANG_EQUAL -> {
        if (!TypecheckUtils.isSameType(left, right)) {
          yield ValuecheckUtils.getBooleanSingleton(true);
        }
        yield ValuecheckUtils.getBooleanSingleton(!left.value().equals(right.value()));
      }
      case TokenType.LESS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '<' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValuecheckUtils.getBooleanSingleton(((LoxNumber) left).value < ((LoxNumber) right).value);
      }
      case TokenType.LESS_EQUAL -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '<=' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValuecheckUtils.getBooleanSingleton(((LoxNumber) left).value <= ((LoxNumber) right).value);
      }
      case TokenType.GREATER -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '>' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValuecheckUtils.getBooleanSingleton(((LoxNumber) left).value > ((LoxNumber) right).value);
      }
      case TokenType.GREATER_EQUAL -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '>=' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValuecheckUtils.getBooleanSingleton(((LoxNumber) left).value >= ((LoxNumber) right).value);
      }
      default -> throw new Error(String.format("Unreachable: Unexpected binary operator '%s'", bin.op.lexeme));
    };
  }

  private LoxObject evaluateUnary(Expr.Unary un) throws InterpreterException {
    final LoxObject inner = this.evaluateExpr(un.inner);
    return switch (un.op.type) {
      case TokenType.BANG -> {
        yield ValuecheckUtils.getBooleanSingleton(ValuecheckUtils.isFalsy(inner));
      }
      case TokenType.MINUS -> {
        if (!TypecheckUtils.isNumber(inner)) {
          throw new InterpreterException(
              String.format("Unsupported operator '-' on %s", TypecheckUtils.typenameOf(inner)));
        }
        yield new LoxNumber(-((LoxNumber) inner).value);
      }
      default -> throw new Error(String.format("Unreachable: Unexpected unary operator '%s'", un.op.lexeme));
    };
  }

  private LoxObject evaluateGrouping(Expr.Grouping gr) throws InterpreterException {
    return this.evaluateExpr(gr.inner);
  }

  private LoxObject evaluateLiteral(Expr.Literal lit) {
    if (lit.value.literal == null) {
      return LoxNil.singleton;
    }
    return switch (lit.value.literal) {
      case Double d -> new LoxNumber(d);
      case String s -> new LoxString(s);
      case Boolean b -> ValuecheckUtils.getBooleanSingleton(b);
      default -> throw new Error(String.format("Unreachable: Unexpected literal type"));
    };
  }

  private LoxObject evaluateVariable(Expr.Variable var) throws InterpreterException {
    return this.env.get(var.var.lexeme);
  }
}

class TypecheckUtils {
  public static boolean isNumber(LoxObject obj) {
    return obj.instanceOf(BuiltinClasses.LNumber);
  }

  public static boolean isString(LoxObject obj) {
    return obj.instanceOf(BuiltinClasses.LString);
  }

  public static boolean isBoolean(LoxObject obj) {
    return obj.instanceOf(BuiltinClasses.LBoolean);
  }

  public static boolean isNil(LoxObject obj) {
    return obj.instanceOf(BuiltinClasses.LNil);
  }

  public static boolean isCallable(LoxObject obj) {
    return obj.instanceOf(BuiltinClasses.LCallable);
  }

  public static boolean isSameType(LoxObject obj1, LoxObject obj2) {
    return typenameOf(obj1) == typenameOf(obj2);
  }

  public static String typenameOf(LoxObject obj) {
    return obj.cls.name;
  }
}

class ValuecheckUtils {
  public static boolean isFalsy(LoxObject obj) {
    if (TypecheckUtils.isBoolean(obj)) {
      return !((LoxBoolean) obj).value;
    }
    return TypecheckUtils.isNil(obj);
  }

  public static boolean isTruthy(LoxObject obj) {
    return !ValuecheckUtils.isFalsy(obj);
  }
  
  public static LoxBoolean getBooleanSingleton(boolean b) {
    return b ? LoxBoolean.trueSingleton : LoxBoolean.falseSingleton;
  }
}

class StringifyUtils {
  public static String stringify(LoxObject obj) {
    return switch (obj) {
      case LoxNumber n -> String.valueOf(n.value);
      case LoxString s -> s.value;
      case LoxBoolean b -> String.valueOf(b.value);
      case LoxNil nil -> "nil";
      default -> String.valueOf(obj);
    };
  }
}

class Builtins {
  public static void bootstrapFunctions(Environment globals) throws InterpreterException {
    globals.define("clock", new LoxCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) {
        return new LoxNumber((double) System.currentTimeMillis() / 1000.0);
      }

      @Override
      public Object value() {
        return this;
      }

      @Override
      public String toString() {
        return "<native fn>";
      }
    });

    globals.define("toString", new LoxCallable() {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) {
        return new LoxString(arguments.get(0).toString());
      }

      @Override
      public Object value() {
        return this;
      }

      @Override
      public String toString() {
        return "<native fn>";
      }
    });

    globals.define("String", BuiltinClasses.LString);
    globals.define("Boolean", BuiltinClasses.LBoolean);
    globals.define("Number", BuiltinClasses.LNumber);
    globals.define("Object", BuiltinClasses.LObject);
  }
}
