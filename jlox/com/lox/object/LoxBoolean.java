package com.lox.object;

public class LoxBoolean extends LoxObject {
  public final boolean value;

  public LoxBoolean(boolean value) {
    super(BuiltinClasses.LBoolean);
    this.value = value;
  }

  public Boolean value() {
    return this.value;
  }

  public String toString() {
    return this.value().toString();
  }
}
