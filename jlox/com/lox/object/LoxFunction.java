package com.lox.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

  public abstract LoxFunction concatEnv(Map<String, LoxObject> symbols);

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

    @Override
    public Environment env() {
      return null;
    }

    @Override
    public LoxFunction concatEnv(Map<String, LoxObject> env) {
      return this;
    }
  }

  public static class LoxUserFunction extends LoxFunction {
    public Environment enclosingEnv;
    public FuncStmt node;

    public LoxUserFunction(FuncStmt node, Environment env) {
      this.node = node;
      this.enclosingEnv = env;
    }

    @Override
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

    @Override
    public LoxFunction concatEnv(Map<String, LoxObject> symbols) {
      final Environment env = new Environment(this.enclosingEnv, symbols);
      return new LoxUserFunction(this.node, env);
    }
  }
}
