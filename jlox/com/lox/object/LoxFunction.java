package com.lox.object;

import java.util.List;

import com.lox.Environment;
import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.NonLocalJump;
import com.lox.ast.Token;
import com.lox.ast.Stmt.FuncStmt;

public class LoxFunction extends LoxCallable {
  private Environment env;
  private FuncStmt func;
  
  public LoxFunction(FuncStmt func, Environment env) {
    super(BuiltinClasses.LFunction);
    this.func = func;
    this.env = env;
  }

  public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) throws InterpreterException {
    assert this.arity() == arguments.size();

    final Environment paramEnv = new Environment(this.env);
    for (int i = 0; i < this.func.params.size(); ++i) {
      paramEnv.define(this.func.params.get(i).lexeme, arguments.get(i));
    }

    final Environment bodyEnv = new Environment(paramEnv);
    final Environment lastEnv = interpreter.env;
    interpreter.env = bodyEnv;

    try {
      interpreter.evaluate(this.func.body.stmts);
      return LoxNil.singleton;
    } catch (NonLocalJump.Return r) {
      return r.value;
    } finally {
      interpreter.env = lastEnv;
    }
  }

  public int arity() {
    return func.params.size();
  }

  public String toString() {
    return String.format("<function %s>", func.name.lexeme);
  }
}
