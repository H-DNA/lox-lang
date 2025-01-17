package com.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.object.LoxBoolean;
import com.lox.object.LoxFunction;
import com.lox.object.LoxNumber;
import com.lox.object.LoxObject;
import com.lox.object.LoxString;

public class Environment {
  public static final Environment globals = new Environment(null);
  static {
    try {
      Environment.globals.define("clock", new LoxFunction.LoxForeignFunction("clock") {
        @Override
        public int arity() {
          return 0;
        }

        @Override
        public LoxObject call(List<LoxObject> arguments) {
          return new LoxNumber((double) System.currentTimeMillis() / 1000.0);
        }
      });

      Environment.globals.define("toString", new LoxFunction.LoxForeignFunction("toString") {
        @Override
        public int arity() {
          return 1;
        }

        @Override
        public LoxObject call(List<LoxObject> arguments) {
          return new LoxString(arguments.get(0).toString());
        }
      });

      Environment.globals.define("String", LoxString.OBJECT);
      Environment.globals.define("Boolean", LoxBoolean.OBJECT);
      Environment.globals.define("Number", LoxNumber.OBJECT);
      Environment.globals.define("Object", LoxObject.OBJECT);
    } catch (InterpreterException e) {
      throw new RuntimeException(e.message);
    }
  }

  public final Environment parent;
  private final Map<String, LoxObject> values = new HashMap<>();

  public Environment() {
    this.parent = globals;
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
