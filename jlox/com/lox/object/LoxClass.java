package com.lox.object;

import java.util.List;

import com.lox.Interpreter;
import com.lox.InterpreterException;

public class LoxClass extends LoxCallable {
  public final String name;

  public LoxClass(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return String.format("<class %s>", this.name);
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) throws InterpreterException {
    return new LoxInstance(this);
  }
}
