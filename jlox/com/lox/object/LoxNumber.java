package com.lox.object;

import java.util.ArrayList;

import com.lox.InterpreterException;

public class LoxNumber extends LoxObject {
  public static final LoxClass OBJECT = new LoxClass("Number", LoxObject.OBJECT, new ArrayList<>());

  public final double value;

  public LoxNumber(double value) {
    super();
    this.value = value;
  }

  @Override
  public LoxClass cls() {
    return LoxNumber.OBJECT;
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
