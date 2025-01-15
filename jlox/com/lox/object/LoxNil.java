package com.lox.object;

import java.util.ArrayList;

import com.lox.InterpreterException;

public class LoxNil extends LoxObject {
  public final static LoxClass OBJECT = new LoxClass("Nil", LoxObject.OBJECT, new ArrayList<>());
  public final static LoxNil NIL = new LoxNil();

  private LoxNil() {
    super();
  }

  @Override
  public LoxClass cls() {
    return LoxNil.OBJECT;
  }

  @Override
  public String toString() {
    return "nil";
  }

  @Override
  public void set(String prop, LoxObject value) throws InterpreterException {
    throw new InterpreterException("Nil is immutable");
  }
}
