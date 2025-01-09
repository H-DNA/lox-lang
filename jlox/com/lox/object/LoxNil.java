package com.lox.object;

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
}
