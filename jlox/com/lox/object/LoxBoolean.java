package com.lox.object;

import com.lox.InterpreterException;

public class LoxBoolean extends LoxObject {
  public final boolean value;

  public LoxBoolean(boolean value) {
    super(BuiltinClasses.LBoolean);
    this.value = value;
  }

  @Override
  public Boolean value() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.value().toString();
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("Boolean is immutable");
  }
}
