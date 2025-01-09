package com.lox.object;

public class LoxInstance extends LoxObject {
  private LoxClass cls;

  public LoxInstance(LoxClass cls) {
    this.cls = cls;
  }

  @Override
  public String toString() {
    return String.format("<instance %s>", this.cls.name);
  }

  @Override
  public Object value() {
    return this;
  }
}
