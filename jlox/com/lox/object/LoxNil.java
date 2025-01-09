package com.lox.object;

import com.lox.InterpreterException;

public class LoxNil extends LoxObject {
  public final static LoxNil singleton = new LoxNil();

  LoxNil() {
    super(BuiltinClasses.LNil);
  }

  @Override
  public Object value() {
    return null;
  }

  @Override
  public String toString() {
    return "nil";
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("Nil is immutable");
  }
}
