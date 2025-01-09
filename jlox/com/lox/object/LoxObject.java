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
    if (this.fields.containsKey(prop)) {
      return this.fields.get(prop);
    }
    LoxFunction unboundedFunc = null;
    LoxClass curCls = this.cls;
    while (unboundedFunc == null && curCls != BuiltinClasses.LObject) {
      unboundedFunc = curCls.lookupMethod(prop);
      curCls = curCls.supercls;
    }
    if (unboundedFunc == null) {
      unboundedFunc = BuiltinClasses.LObject.lookupMethod(prop);
    }
    return unboundedFunc == null ? LoxNil.singleton : new LoxBoundedFunction(this, unboundedFunc);
  }
  public void set(String prop, LoxObject value) throws InterpreterException {
    this.fields.put(prop, value);
  }

  public LoxObject(LoxClass cls) {
    this.cls = cls;
    this.fields = new HashMap<>();
  }
}
