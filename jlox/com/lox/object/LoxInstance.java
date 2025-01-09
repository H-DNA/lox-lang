package com.lox.object;

public class LoxInstance extends LoxObject {
  public LoxInstance(LoxClass cls) {
    super(cls);
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
