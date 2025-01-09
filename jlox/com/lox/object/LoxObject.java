package com.lox.object;

import java.util.HashMap;
import java.util.Map;

import com.lox.InterpreterException;

public abstract class LoxObject {
  public abstract Object value();
  public abstract String toString();
  public boolean instanceOf(LoxClass cls) {
    LoxClass curCls = this.cls;
    while (curCls != BuiltinClasses.LObject) {
      if (curCls == cls) {
        return true;
      }
      curCls = curCls.supercls;
    }
    return cls == BuiltinClasses.LObject;
  }
  public final LoxClass cls;

  private Map<String, LoxObject> fields;
  public LoxObject get(String prop) throws InterpreterException {
    if (!this.fields.containsKey(prop)) {
      return LoxNil.singleton;
    }
    return this.fields.get(prop);
  }
  public void set(String prop, LoxObject value) throws InterpreterException {
    this.fields.put(prop, value);
  }

  public LoxObject(LoxClass cls) {
    this.cls = cls;
    this.fields = new HashMap<>();
  }
}
