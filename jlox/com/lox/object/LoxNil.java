package com.lox.object;

public class LoxNil extends LoxObject {
  public LoxNil() {}

  @Override
  public Object value() {
    return null;
  }

  @Override
  public String toString() {
    return "nil";
  }
}
