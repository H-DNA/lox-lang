package com.lox.object;

import java.util.List;

import com.lox.Environment;
import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.NonLocalJump;
import com.lox.ast.Token;

public class LoxBoundedFunction extends LoxCallable {
  private LoxObject owner;
  private LoxObject cls;
  private LoxFunction func;
  
  public LoxBoundedFunction(LoxObject owner, LoxClass cls, LoxFunction func) {
    super(BuiltinClasses.LBoundedFunction);
    this.owner = owner;
    this.cls = cls;
    this.func = func;
  }

  public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) throws InterpreterException {
    assert this.arity() == arguments.size();

    final Environment paramEnv = new Environment(func.env);
    paramEnv.define("this", this.owner);
    paramEnv.define("$CLASS", this.cls);
    for (int i = 0; i < this.func.func.params.size(); ++i) {
      paramEnv.define(this.func.func.params.get(i).lexeme, arguments.get(i));
    }

    final Environment bodyEnv = new Environment(paramEnv);
    final Environment lastEnv = interpreter.env;
    interpreter.env = bodyEnv;

    try {
      interpreter.evaluate(this.func.func.body.stmts);
      return LoxNil.singleton;
    } catch (NonLocalJump.Return r) {
      return r.value;
    } finally {
      interpreter.env = lastEnv;
    }
  }

  public int arity() {
    return this.func.func.params.size();
  }

  public String toString() {
    return String.format("<bounded function %s>", func.func.name.lexeme);
  }
}
