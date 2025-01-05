package com.lox;

import java.util.HashMap;
import java.util.Map;

import com.lox.object.LoxObject;

public class Environment {
  public final Environment parent;
  private final Map<String, LoxObject> values = new HashMap<>();

  public Environment() {
    this.parent = null;
  }

  public Environment(Environment parent) {
    this.parent = parent;
  }

  public void define(String name, LoxObject value) throws InterpreterException {
    if (values.containsKey(name)) {
      throw new InterpreterException("Redeclared variable '" + name + "'");
    }
    this.values.put(name, value);
  }

  public void assign(String name, LoxObject value) throws InterpreterException {
    for (Environment env = this; env != null; env = env.parent) {
      if (env.values.containsKey(name)) {
        env.values.put(name, value);
        return;
      }
    }
    throw new InterpreterException("Undefined variable '" + name + "'");
  }

  public LoxObject get(String name) throws InterpreterException {
    for (Environment env = this; env != null; env = env.parent) {
      if (env.values.containsKey(name)) {
        return env.values.get(name);
      }
    }
    throw new InterpreterException("Undefined variable '" + name + "'");
  }
}
