package com.lox.object;

import java.util.List;

import com.lox.Interpreter;
import com.lox.InterpreterException;

public class LoxClass extends LoxCallable {
  public final String name;
  public final LoxClass supercls;

  public LoxClass(String name) {
    super(BuiltinClasses.LClass);
    this.name = name;
    this.supercls = BuiltinClasses.LObject;
  }

  public LoxClass(String name, LoxClass supercls) {
    this.name = name;
    this.supercls = supercls;
  }

  @Override
  public String toString() {
    return String.format("<class %s>", this.name);
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) throws InterpreterException {
    return new LoxInstance(this);
  }
}
