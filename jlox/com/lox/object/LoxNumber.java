package com.lox.object;

import com.lox.InterpreterException;

public class LoxNumber extends LoxObject {
  public final double value;

  public LoxNumber(double value) {
    super(BuiltinClasses.LNumber);
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(this.value);
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("Number is immutable");
  }
}
