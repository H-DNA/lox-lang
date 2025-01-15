package com.lox.object;

import com.lox.InterpreterException;

public class LoxString extends LoxObject {
  public final String value;

  public LoxString(String value) {
    super(BuiltinClasses.LString);
    this.value = value;
  }

  @Override
  public String toString() {
    return "\"" + this.value + "\"";
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("String is immutable");
  }
}
