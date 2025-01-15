package com.lox.object;

import java.util.ArrayList;

import com.lox.InterpreterException;

public class LoxBoolean extends LoxObject {
  public static final LoxClass OBJECT = new LoxClass("Boolean", LoxBoolean.OBJECT, new ArrayList<>());

  public final boolean value;

  private LoxBoolean(boolean value) {
    super();
    this.value = value;
  }
  
  @Override
  public LoxClass cls() {
    return LoxBoolean.OBJECT;
  }

  @Override
  public String toString() {
    return String.valueOf(this.value);
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("Boolean is immutable");
  }

  public static LoxBoolean FALSE = new LoxBoolean(false);
  public static LoxBoolean TRUE = new LoxBoolean(true);
}
