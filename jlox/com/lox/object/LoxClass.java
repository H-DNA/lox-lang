package com.lox.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.object.LoxObject;
import com.lox.utils.Pair;

public class LoxClass extends LoxObject {
  public static final LoxClass OBJECT = new LoxClass("Class", LoxObject.OBJECT, new ArrayList<>());

  public final String name;
  public final LoxClass supercls;
  public final Map<String, LoxFunction> methods;

  public LoxClass(String name, List<LoxFunction> methods) {
    super();
    this.name = name;
    this.supercls = LoxObject.OBJECT;
    this.methods = new HashMap<>();
    for (LoxFunction method: methods) {
      this.methods.put(method.name(), method);
    }
  }

  public LoxClass(String name, LoxClass supercls, List<LoxFunction> methods) {
    super();
    this.name = name;
    this.supercls = supercls;
    this.methods = new HashMap<>();
    for (LoxFunction method: methods) {
      this.methods.put(method.name(), method);
    }
  }

  public boolean isSubclass(LoxClass cls) {
    if (cls == this || cls == LoxObject.OBJECT) return true;
    LoxClass curCls = this.supercls;
    while (curCls != LoxObject.OBJECT) {
      if (curCls == cls) {
        return true;
      }
      curCls = curCls.supercls;
    }
    return false;
  }
  public boolean isSuperclass(LoxClass cls) {
    return this == cls || cls.isSubclass(this);
  }

  public Pair<LoxFunction, LoxClass> lookupOwnMethod(String name) {
    final LoxFunction res = this.methods.getOrDefault(name, null);
    return res == null ? null : new Pair(res, this);
  }

  public Pair<LoxFunction, LoxClass> lookupMethod(String name) {
    Pair<LoxFunction, LoxClass> res = null;
    LoxClass curCls = this;
    while (res == null && curCls != LoxObject.OBJECT) {
      res = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return res == null ? LoxObject.OBJECT.lookupOwnMethod(name) : res;
  }
  public Pair<LoxFunction, LoxClass> lookupMethod(String name, LoxClass startCls) {
    if (!this.isSubclass(startCls)) {
      throw new Error("Lookup method must start from a superclass");
    }

    Pair<LoxFunction, LoxClass> res = null;
    LoxClass curCls = startCls;
    while (res == null && curCls != LoxObject.OBJECT) {
      res = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return res == null ? LoxObject.OBJECT.lookupOwnMethod(name) : res;
  }

  @Override
  public String toString() {
    return String.format("<class %s>", this.name);
  }

  @Override
  public LoxClass cls() {
    return LoxClass.OBJECT;
  }
}
