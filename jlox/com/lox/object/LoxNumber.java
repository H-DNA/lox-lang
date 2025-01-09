package com.lox.object;

import com.lox.InterpreterException;

public class LoxNumber extends LoxObject {
  public final double value;

  public LoxNumber(double value) {
    super(BuiltinClasses.LNumber);
    this.value = value;
  }

  @Override
  public Number value() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.value().toString();
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("Number is immutable");
  }
}
