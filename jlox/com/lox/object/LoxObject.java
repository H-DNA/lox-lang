package com.lox.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.lox.InterpreterException;
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
    Pair<LoxFunction, LoxClass> res = this.cls().lookupMethod(prop);
    return res == null ? LoxNil.NIL : new LoxBoundFunction(res.second, this, res.first);
  }

  public void set(String prop, LoxObject value) throws InterpreterException {
    this.fields.put(prop, value);
  }

  public LoxBoundFunction getMethod(String prop) throws InterpreterException {
    Pair<LoxFunction, LoxClass> res = this.cls().lookupMethod(prop);
    return res == null ? null : new LoxBoundFunction(res.second, this, res.first);
  }
  public LoxBoundFunction getMethod(String prop, LoxClass startCls) throws InterpreterException {
    if (startCls != null && !this.cls().isSubclass(startCls)) {
      throw new Error("Lookup method must start from a superclass");
    }

    Pair<LoxFunction, LoxClass> res = this.cls().lookupMethod(prop, startCls);
    return res == null ? null : new LoxBoundFunction(res.second, this, res.first);
  }
}
