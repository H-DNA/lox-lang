package com.lox.object;

import java.util.ArrayList;
import java.util.List;

import com.lox.Environment;
import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.NonLocalJump;
import com.lox.ast.Stmt.FuncStmt;

public abstract class LoxFunction extends LoxObject {
  public static final LoxClass OBJECT = new LoxClass("Function", LoxObject.OBJECT, new ArrayList<>());

  public abstract String name();

  public abstract int arity();

  public abstract Environment env();

  @Override
  public LoxClass cls() {
    return LoxFunction.OBJECT;
  }

  public static abstract class LoxForeignFunction extends LoxFunction {
    public final String fname;

    public LoxForeignFunction(String name) {
      super();
      this.fname = name;
    }

    public abstract LoxObject call(List<LoxObject> arguments) throws InterpreterException;

    @Override
    public String name() {
      return this.fname;
    }

    @Override
    public String toString() {
      return String.format("<native function '%s'>", this.fname);
    }

    public Environment env() {
      return Interpreter.globals;
    }
  }

  public static class LoxUserFunction extends LoxFunction {
    public Environment enclosingEnv;
    public FuncStmt node;

    public LoxUserFunction(FuncStmt node, Environment env) {
      this.node = node;
      this.enclosingEnv = env;
    }

    public Environment env() {
      return this.enclosingEnv;
    }

    @Override
    public String name() {
      return this.node.name.lexeme;
    }

    @Override
    public LoxClass cls() {
      return LoxFunction.OBJECT;
    }

    @Override
    public int arity() {
      return this.node.params.size();
    }

    @Override
    public String toString() {
      return String.format("<function %s>", this.node.name.lexeme);
    }
  }
}
