package com.lox.object;

public abstract class LoxObject {
  public abstract Object value();
  public abstract String toString();
  public LoxClass cls;

  public LoxObject(LoxClass cls) {
    this.cls = cls;
  }
}
