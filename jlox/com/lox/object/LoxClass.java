package com.lox.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.utils.Pair;

public class LoxClass extends LoxCallable {
  public final String name;
  public final LoxClass supercls;
  public final Map<String, LoxFunction> methods;

  public LoxClass(String name, List<LoxFunction> methods) {
    super(BuiltinClasses.LClass);
    this.name = name;
    this.supercls = BuiltinClasses.LObject;
    this.methods = new HashMap<>();
    for (LoxFunction method: methods) {
      this.methods.put(method.func.name.lexeme, method);
    }
  }

  public LoxClass(String name, LoxClass supercls, List<LoxFunction> methods) {
    super(BuiltinClasses.LClass);
    this.name = name;
    this.supercls = supercls;
    this.methods = new HashMap<>();
    for (LoxFunction method: methods) {
      this.methods.put(method.func.name.lexeme, method);
    }
  }

  public boolean isSubclass(LoxClass cls) {
    if (cls == this || cls == BuiltinClasses.LObject) return true;
    LoxClass curCls = this.supercls;
    while (curCls != BuiltinClasses.LObject) {
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
    while (res == null && curCls != BuiltinClasses.LObject) {
      res = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return res == null ? BuiltinClasses.LObject.lookupOwnMethod(name) : res;
  }
  public Pair<LoxFunction, LoxClass> lookupMethod(String name, LoxClass startCls) {
    if (startCls != null && !this.isSubclass(startCls)) {
      throw new Error("Lookup method must start from a superclass");
    }

    Pair<LoxFunction, LoxClass> res = null;
    LoxClass curCls = startCls == null ? this : startCls;
    while (res == null && curCls != BuiltinClasses.LObject) {
      res = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return res == null ? new Pair(BuiltinClasses.LObject.lookupOwnMethod(name), BuiltinClasses.LObject) : res;
  }

  @Override
  public String toString() {
    return String.format("<class %s>", this.name);
  }

  @Override
  public int arity() {
    final Pair<LoxFunction, LoxClass> res = this.lookupOwnMethod("constructor");
    return res == null ? 0 : res.first.arity();
  }

  @Override
  public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) throws InterpreterException {
    final LoxObject blankObj = new LoxObject(this) {
      @Override
      public String toString() {
        return String.format("<instance %s>", this.cls.name);
      }
    };

    final Pair<LoxFunction, LoxClass> res = this.lookupMethod("constructor");
    if (res == null) {
      return blankObj;
    }

    final LoxBoundedFunction boundedConstructor = new LoxBoundedFunction(blankObj, res.second, res.first);
    boundedConstructor.call(interpreter, arguments);

    return blankObj;
  }
}
