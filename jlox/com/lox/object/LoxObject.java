package com.lox.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.lox.InterpreterException;
import com.lox.SpecialSymbols;
import com.lox.utils.Pair;

public abstract class LoxObject {
  public static final LoxClass OBJECT = new LoxClass("Object", new ArrayList<>());

  private Map<String, LoxObject> fields;

  public LoxObject() {
    this.fields = new HashMap<>();
  }

  public abstract LoxClass cls();

  public abstract String toString();

  public boolean instanceOf(LoxClass cls) {
    return this.cls().isSubclass(cls);
  }

  public LoxObject get(String prop) throws InterpreterException {
    if (this.fields.containsKey(prop)) {
      return this.fields.get(prop);
    }
    return this.getMethod(prop);
  }

  public void set(String prop, LoxObject value) throws InterpreterException {
    this.fields.put(prop, value);
  }

  public LoxObject getMethod(String prop) throws InterpreterException {
    final LoxObject res = this.cls().lookupMethod(prop);
    if (res == LoxNil.NIL) {
      return res;
    }
    final Map<String, LoxObject> symbols = new HashMap<>();
    symbols.put(SpecialSymbols.THIS_OBJECT, this);
    return ((LoxFunction) res).concatEnv(symbols);
  }

  public LoxObject getMethod(String prop, LoxClass startCls) throws InterpreterException {
    if (!this.cls().isSubclass(startCls)) {
      throw new Error("Lookup method must start from a superclass");
    }

    final LoxObject res = this.cls().lookupMethod(prop, startCls);
    if (res == LoxNil.NIL) {
      return res;
    }
    final Map<String, LoxObject> symbols = new HashMap<>();
    symbols.put(SpecialSymbols.THIS_OBJECT, this);
    return ((LoxFunction) res).concatEnv(symbols);
  }
}
