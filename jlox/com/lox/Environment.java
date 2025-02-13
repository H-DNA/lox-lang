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
  // This is for the sole purpose of creating a global environment
  private Environment() {
    this.parent = null;
    this.globals = this;
    this.values = new HashMap<>();
  }

  public static Environment createGlobals() throws InterpreterException {
    final Environment globals = new Environment();
    globals.define("clock", new LoxFunction.LoxForeignFunction("clock") {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public LoxObject call(List<LoxObject> arguments) {
        return new LoxNumber((double) System.currentTimeMillis() / 1000.0);
      }
    });

    globals.define("toString", new LoxFunction.LoxForeignFunction("toString") {
      @Override
      public int arity() {
        return 1;
      }

      @Override
      public LoxObject call(List<LoxObject> arguments) {
        return new LoxString(arguments.get(0).toString());
      }
    });

    globals.define("String", LoxString.OBJECT);
    globals.define("Boolean", LoxBoolean.OBJECT);
    globals.define("Number", LoxNumber.OBJECT);
    globals.define("Object", LoxObject.OBJECT);

    return globals;
  }

  public final Environment parent;
  private final Environment globals;
  private final Map<String, LoxObject> values;

  public Environment(Environment parent) {
    this.parent = parent;
    this.globals = this.parent.globals;
    this.values = new HashMap<>();
  }

  public Environment(Environment parent, Map<String, LoxObject> symbols) {
    this.parent = parent;
    this.globals = this.parent.globals;
    this.values = symbols;
  }

  // Declare that an identifier exists in this environment, but its value is not
  // defined yet
  // var x = 3; is equivalent to env.declare("x"); env.assign("x", 3);
  public void declare(String name) throws InterpreterException {
    if (values.containsKey(name)) {
      throw new InterpreterException("Redeclared variable '" + name + "'");
    }
    this.values.put(name, null);
  }

  public void assign(String name, LoxObject value) throws InterpreterException {
    for (Environment env = this; env != null; env = env.parent) {
      if (env.values.containsKey(name)) {
        if (!env.isDefined(name)) {
          throw new InterpreterException("Variable '" + name + "' used before defined");
        }
        env.values.put(name, value);
        return;
      }
    }
    throw new InterpreterException("Undefined variable '" + name + "'");
  }

  public void define(String name, LoxObject value) throws InterpreterException {
    if (this.isDefined(name)) {
      throw new InterpreterException("Redeclared variable '" + name + "'");
    }
    this.values.put(name, value);
  }

  public LoxObject get(String name) throws InterpreterException {
    for (Environment env = this; env != null; env = env.parent) {
      if (env.values.containsKey(name)) {
        if (!env.isDefined(name)) {
          throw new InterpreterException("Variable '" + name + "' used before defined");
        }
        return env.values.get(name);
      }
    }
    throw new InterpreterException("Undefined variable '" + name + "'");
  }

  public boolean isDefined(String name) {
    return this.values.containsKey(name) && this.values.get(name) != null;
  }
}
