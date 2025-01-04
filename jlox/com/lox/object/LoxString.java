package com.lox.object;

public class LoxString extends LoxObject {
  public final String value;

  public LoxString(String value) {
    this.value = value;
  }

  @Override
  public String value() {
    return this.value;
  }

  @Override
  public String toString() {
    return "\"" + this.value().toString() + "\"";
  }
}
