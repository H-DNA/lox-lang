package com.lox.object;

import java.util.List;

import com.lox.Interpreter;

public abstract class LoxCallable extends LoxObject {
  public Object value() {
    return this;
  }
  public abstract LoxObject call(Interpreter interpreter, List<LoxObject> arguments);
  public abstract int arity();
  public abstract String toString();
}
