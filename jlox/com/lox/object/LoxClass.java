package com.lox.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.SpecialSymbols;
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
    for (LoxFunction method : methods) {
      this.methods.put(method.name(), method);
    }
  }

  public LoxClass(String name, LoxClass supercls, List<LoxFunction> methods) {
    super();
    this.name = name;
    this.supercls = supercls;
    this.methods = new HashMap<>();
    for (LoxFunction method : methods) {
      this.methods.put(method.name(), method);
    }
  }

  public boolean isSubclass(LoxClass cls) {
    if (cls == this || cls == LoxObject.OBJECT)
      return true;
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

  public LoxObject lookupOwnMethod(String name) {
    final LoxFunction res = this.methods.getOrDefault(name, null);
    if (res == null) {
      return LoxNil.NIL;
    }
    final Map<String, LoxObject> symbols = new HashMap<>();
    symbols.put(SpecialSymbols.THIS_CLASS, this);
    symbols.put(SpecialSymbols.SUPER_CLASS, this.supercls);
    return res.concatEnv(symbols);
  }

  public LoxObject lookupMethod(String name) {
    LoxObject res = LoxNil.NIL;
    LoxClass curCls = this;
    while (res == LoxNil.NIL && curCls != LoxObject.OBJECT) {
      res = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return res == LoxNil.NIL ? LoxObject.OBJECT.lookupOwnMethod(name) : res;
  }

  public LoxObject lookupMethod(String name, LoxClass startCls) {
    if (!this.isSubclass(startCls)) {
      throw new Error("Lookup method must start from a superclass");
    }

    LoxObject res = LoxNil.NIL;
    LoxClass curCls = startCls;
    while (res == LoxNil.NIL && curCls != LoxObject.OBJECT) {
      res = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return res == LoxNil.NIL ? LoxObject.OBJECT.lookupOwnMethod(name) : res;
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
