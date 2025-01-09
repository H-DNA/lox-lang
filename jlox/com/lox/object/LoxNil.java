package com.lox.object;

public class LoxNil extends LoxObject {
  public LoxNil() {
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
}
