package com.lox.object;

import java.util.ArrayList;

import com.lox.InterpreterException;

public class LoxString extends LoxObject {
  public static final LoxClass OBJECT = new LoxClass("String", LoxObject.OBJECT, new ArrayList<>());

  public final String value;

  public LoxString(String value) {
    super();
    this.value = value;
  }

  @Override
  public LoxClass cls() {
    return LoxString.OBJECT;
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
