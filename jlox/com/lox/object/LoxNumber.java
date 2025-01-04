package com.lox.object;

public class LoxNumber extends LoxObject {
  public final double value;

  public LoxNumber(double value) {
    this.value = value;
  }

  @Override
  public Number value() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.value().toString();
  }
}
