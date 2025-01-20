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
import com.lox.object.LoxBoolean;
import com.lox.object.LoxClass;
import com.lox.object.LoxFunction;
import com.lox.object.LoxNil;
import com.lox.object.LoxNumber;
import com.lox.object.LoxObject;
import com.lox.object.LoxString;
import com.lox.utils.Pair;

public class Interpreter {
  public void evaluate(List<Stmt> stmts) throws InterpreterException {
    Environment env = new Environment();
    for (Stmt stmt : stmts) {
      this.evaluateStmt(stmt, env);
    }
  }

  public void evaluate(List<Stmt> stmts, Environment env) throws InterpreterException {
    for (Stmt stmt : stmts) {
      this.evaluateStmt(stmt, env);
    }
  }

  public LoxObject evaluateStmt(Stmt stmt, Environment env) throws InterpreterException {
    return switch (stmt) {
      case Stmt.PrintStmt p -> {
        System.out.println(this.evaluateExpr(p.expr, env).toString());
        yield LoxNil.NIL;
      }
      case Stmt.ExprStmt e -> this.evaluateExpr(e.expr, env);
      case Stmt.DeclStmt d -> {
        env.define(d.id.lexeme, d.expr == null ? LoxNil.NIL : this.evaluateExpr(d.expr, env));
        yield LoxNil.NIL;
      }
      case Stmt.IfStmt i -> {
        final LoxObject condValue = this.evaluateExpr(i.cond, env);
        final Environment blockEnv = new Environment(env);
        if (ValueUtils.isTruthy(condValue)) {
          yield this.evaluateStmt(i.thenBranch, blockEnv);
        } else {
          yield i.elseBranch != null ? this.evaluateStmt(i.elseBranch, blockEnv) : LoxNil.NIL;
        }
      }
      case Stmt.WhileStmt w -> {
        final Environment blockEnv = new Environment(env);
        while (ValueUtils.isTruthy(this.evaluateExpr(w.cond, blockEnv))) {
          this.evaluateStmt(w.body, blockEnv);
        }
        yield LoxNil.NIL;
      }
      case Stmt.ForStmt f -> {
        final Environment blockEnv = new Environment(env);
        this.evaluateStmt(f.init, blockEnv);
        while (ValueUtils.isTruthy(this.evaluateStmt(f.cond, blockEnv))) {
          this.evaluateStmt(f.body, blockEnv);
          this.evaluateExpr(f.post, blockEnv);
        }
        yield LoxNil.NIL;
      }
      case Stmt.BlockStmt b -> {
        final Environment blockEnv = new Environment(env);
        LoxObject lastValue = LoxNil.NIL;
        for (Stmt s : b.stmts) {
          lastValue = this.evaluateStmt(s, blockEnv);
        }
        yield lastValue;
      }
      case Stmt.FuncStmt f -> {
        env.define(f.name.lexeme, new LoxFunction.LoxUserFunction(f, env));
        yield LoxNil.NIL;
      }
      case Stmt.ReturnStmt r -> {
        throw new NonLocalJump.Return(this.evaluateExpr(r.expr, env));
      }
      case Stmt.ClsStmt c -> {
        List<LoxFunction> methods = new ArrayList<>();
        for (FuncStmt func : c.methods) {
          methods.add(new LoxFunction.LoxUserFunction(func, env));
        }
        LoxClass cls = null;
        if (c.supercls == null) {
          cls = new LoxClass(c.name.lexeme, methods);
        } else {
          LoxObject supercls = env.get(c.supercls.lexeme);
          if (!(supercls instanceof LoxClass)) {
            throw new InterpreterException(String.format("'%s' is not a class", c.supercls.lexeme));
          }
          cls = new LoxClass(c.name.lexeme, (LoxClass) supercls, methods);
        }
        env.define(c.name.lexeme, cls);
        yield LoxNil.NIL;
      }
      default -> throw new Error("Non-exhaustive check");
    };
  }

  public LoxObject evaluateExpr(Expr expr, Environment env) throws InterpreterException {
    return switch (expr) {
      case Expr.Binary b -> this.evaluateBinary(b, env);
      case Expr.Unary u -> this.evaluateUnary(u, env);
      case Expr.Grouping g -> this.evaluateGrouping(g, env);
      case Expr.Variable v -> this.evaluateVariable(v, env);
      case Expr.Literal l -> this.evaluateLiteral(l, env);
      case Expr.This t -> env.get(SpecialSymbols.THIS_OBJECT);
      case Expr.Call c -> {
        LoxObject callee = this.evaluateExpr(c.callee, env);
        List<LoxObject> arguments = new ArrayList<>();
        for (Expr arg : c.params) {
          arguments.add(this.evaluateExpr(arg, env));
        }
        if (callee instanceof LoxFunction) {
          yield this.evaluateFunctionCall((LoxFunction) callee, arguments, env);
        } else if (callee instanceof LoxClass) {
          yield this.evaluateClassConstructor((LoxClass) callee, arguments, env);
        } else {
          throw new InterpreterException("Callee is not of Callable type");
        }
      }
      case Expr.Get g -> this.evaluateExpr(g.object, env).get(g.property.lexeme);
      case Expr.Set s -> {
        final LoxObject value = this.evaluateExpr(s.value, env);
        this.evaluateExpr(s.object, env).set(s.property.lexeme, value);
        yield value;
      }
      case Expr.SuperGet s -> {
        final LoxClass superCls = (LoxClass) env.get(SpecialSymbols.SUPER_CLASS);
        final LoxObject thisObj = env.get(SpecialSymbols.THIS_OBJECT);

        yield thisObj.getMethod(s.member.lexeme, superCls);
      }
      case Expr.SuperCall s -> {
        final LoxClass superCls = (LoxClass) env.get(SpecialSymbols.SUPER_CLASS);
        final LoxObject thisObj = env.get(SpecialSymbols.THIS_OBJECT);

        List<LoxObject> arguments = new ArrayList<>();
        for (Expr arg : s.params) {
          arguments.add(this.evaluateExpr(arg, env));
        }
        final LoxObject constructor = thisObj.getMethod("constructor", superCls);
        if (!TypecheckUtils.isNil(constructor)) {
          this.evaluateFunctionCall((LoxFunction) constructor, arguments, env);
        }
        yield LoxNil.NIL;
      }
      default -> throw new Error("Non-exhaustive check");
    };
  }

  private LoxObject evaluateClassConstructor(LoxClass kls, List<LoxObject> args, Environment env)
      throws InterpreterException {
    final LoxObject blankObj = new LoxObject() {
      @Override
      public String toString() {
        return String.format("<instance %s>", this.cls().name);
      }

      @Override
      public LoxClass cls() {
        return kls;
      }
    };

    final LoxObject res = blankObj.getMethod("constructor");
    if (res == LoxNil.NIL) {
      if (args.size() > 0) {
        throw new InterpreterException(String.format("Expected %s argument(s) but got %s", 0, args.size()));
      }
      return blankObj;
    }

    this.evaluateFunctionCall((LoxFunction) res, args, env);

    return blankObj;
  }

  private LoxObject evaluateFunctionCall(LoxFunction callee, List<LoxObject> args, Environment env)
      throws InterpreterException {
    return switch (callee) {
      case LoxFunction.LoxUserFunction u -> this.evaluateUserFunction(u, args, env);
      case LoxFunction.LoxForeignFunction f -> this.evaluateForeignFunction(f, args, env);
      default -> throw new Error("Unhandled LoxFunction subclass");
    };
  }

  private LoxObject evaluateUserFunction(LoxFunction.LoxUserFunction func, List<LoxObject> args, Environment env)
      throws InterpreterException {
    if (func.arity() != args.size()) {
      throw new InterpreterException(String.format("Expected %s argument(s) but got %s", func.arity(), args.size()));
    }

    try {
      final Environment bodyEnv = new Environment(func.env());
      for (int i = 0; i < func.node.params.size(); ++i) {
        bodyEnv.define(func.node.params.get(i).lexeme, args.get(i));
      }

      this.evaluate(func.node.body.stmts, bodyEnv);
      return LoxNil.NIL;
    } catch (NonLocalJump.Return r) {
      return r.value;
    }
  }

  private LoxObject evaluateForeignFunction(LoxFunction.LoxForeignFunction func, List<LoxObject> args, Environment env)
      throws InterpreterException {
    if (func.arity() != args.size()) {
      throw new InterpreterException(String.format("Expected %s argument(s) but got %s", func.arity(), args.size()));
    }

    return func.call(args);
  }

  private LoxObject evaluateBinary(Expr.Binary bin, Environment env) throws InterpreterException {
    if (bin.op.type == TokenType.EQUAL) {
      final LoxObject right = this.evaluateExpr(bin.right, env);
      env.assign(((Variable) bin.left).var.lexeme, right);
      return right;
    }
    if (bin.op.type == TokenType.OR) {
      final LoxObject left = this.evaluateExpr(bin.left, env);
      if (ValueUtils.isTruthy(left)) {
        return left;
      }
      final LoxObject right = this.evaluateExpr(bin.right, env);
      return right;
    }
    if (bin.op.type == TokenType.AND) {
      final LoxObject left = this.evaluateExpr(bin.left, env);
      if (ValueUtils.isFalsy(left)) {
        return left;
      }
      final LoxObject right = this.evaluateExpr(bin.right, env);
      return right;
    }
    final LoxObject left = this.evaluateExpr(bin.left, env);
    final LoxObject right = this.evaluateExpr(bin.right, env);
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
          yield ValueUtils.getLoxBool(false);
        }
        yield ValueUtils.getLoxBool(ValueUtils.equals(left, right));
      }
      case TokenType.BANG_EQUAL -> {
        if (!TypecheckUtils.isSameType(left, right)) {
          yield ValueUtils.getLoxBool(true);
        }
        yield ValueUtils.getLoxBool(!ValueUtils.equals(left, right));
      }
      case TokenType.LESS -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '<' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValueUtils.getLoxBool(((LoxNumber) left).value < ((LoxNumber) right).value);
      }
      case TokenType.LESS_EQUAL -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '<=' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValueUtils.getLoxBool(((LoxNumber) left).value <= ((LoxNumber) right).value);
      }
      case TokenType.GREATER -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '>' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValueUtils.getLoxBool(((LoxNumber) left).value > ((LoxNumber) right).value);
      }
      case TokenType.GREATER_EQUAL -> {
        if (!TypecheckUtils.isNumber(left) || !TypecheckUtils.isNumber(right)) {
          throw new InterpreterException(String.format("Unsupported operator '>=' on %s and %s",
              TypecheckUtils.typenameOf(left), TypecheckUtils.typenameOf(right)));
        }
        yield ValueUtils.getLoxBool(((LoxNumber) left).value >= ((LoxNumber) right).value);
      }
      default -> throw new Error(String.format("Unreachable: Unexpected binary operator '%s'", bin.op.lexeme));
    };
  }

  private LoxObject evaluateUnary(Expr.Unary un, Environment env) throws InterpreterException {
    final LoxObject inner = this.evaluateExpr(un.inner, env);
    return switch (un.op.type) {
      case TokenType.BANG -> {
        yield ValueUtils.getLoxBool(ValueUtils.isFalsy(inner));
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

  private LoxObject evaluateGrouping(Expr.Grouping gr, Environment env) throws InterpreterException {
    return this.evaluateExpr(gr.inner, env);
  }

  private LoxObject evaluateLiteral(Expr.Literal lit, Environment env) {
    if (lit.value.literal == null) {
      return LoxNil.NIL;
    }
    return switch (lit.value.literal) {
      case Double d -> new LoxNumber(d);
      case String s -> new LoxString(s);
      case Boolean b -> ValueUtils.getLoxBool(b);
      default -> throw new Error(String.format("Unreachable: Unexpected literal type"));
    };
  }

  private LoxObject evaluateVariable(Expr.Variable var, Environment env) throws InterpreterException {
    return env.get(var.var.lexeme);
  }
}

class TypecheckUtils {
  public static boolean isNumber(LoxObject obj) {
    return obj.instanceOf(LoxNumber.OBJECT);
  }

  public static boolean isString(LoxObject obj) {
    return obj.instanceOf(LoxString.OBJECT);
  }

  public static boolean isBoolean(LoxObject obj) {
    return obj.instanceOf(LoxBoolean.OBJECT);
  }

  public static boolean isNil(LoxObject obj) {
    return obj.instanceOf(LoxNil.OBJECT);
  }

  public static boolean isCallable(LoxObject obj) {
    return obj.instanceOf(LoxFunction.OBJECT) || obj.instanceOf(LoxClass.OBJECT);
  }

  public static boolean isSameType(LoxObject obj1, LoxObject obj2) {
    return obj1.cls() == obj2.cls();
  }

  public static String typenameOf(LoxObject obj) {
    return obj.cls().name;
  }
}

class ValueUtils {
  public static boolean isFalsy(LoxObject obj) {
    if (TypecheckUtils.isBoolean(obj)) {
      return !((LoxBoolean) obj).value;
    }
    return TypecheckUtils.isNil(obj);
  }

  public static boolean isTruthy(LoxObject obj) {
    return !ValueUtils.isFalsy(obj);
  }

  public static LoxBoolean getLoxBool(boolean b) {
    return b ? LoxBoolean.TRUE : LoxBoolean.FALSE;
  }

  public static boolean equals(LoxObject o1, LoxObject o2) {
    if (!TypecheckUtils.isSameType(o1, o2)) {
      return false;
    }
    if (TypecheckUtils.isNumber(o1)) {
      return ((LoxNumber) o1).value == ((LoxNumber) o2).value;
    }
    if (TypecheckUtils.isNil(o1)) {
      return true;
    }
    if (TypecheckUtils.isBoolean(o1)) {
      return ((LoxBoolean) o1).value == ((LoxBoolean) o2).value;
    }
    if (TypecheckUtils.isString(o1)) {
      return ((LoxString) o1).value == ((LoxString) o2).value;
    }
    return o1 == o2;
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
