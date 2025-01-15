package com.lox.object;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lox.Interpreter;
import com.lox.InterpreterException;

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
    this.name = name;
    this.supercls = supercls;
    this.methods = new HashMap<>();
    for (LoxFunction method: methods) {
      this.methods.put(method.func.name.lexeme, method);
    }
  }

  public LoxFunction lookupOwnMethod(String name) {
    return this.methods.getOrDefault(name, null);
  }

  public LoxFunction lookupMethod(String name) {
    LoxFunction unboundedFunc = null;
    LoxClass curCls = this;
    while (unboundedFunc == null && curCls != BuiltinClasses.LObject) {
      unboundedFunc = curCls.lookupOwnMethod(name);
      curCls = curCls.supercls;
    }
    return unboundedFunc == null ? BuiltinClasses.LObject.lookupOwnMethod(name) : unboundedFunc;
  }

  @Override
  public String toString() {
    return String.format("<class %s>", this.name);
  }

  @Override
  public int arity() {
    final LoxFunction constructor = this.lookupOwnMethod("constructor");
    return constructor == null ? 0 : constructor.arity();
  }

  @Override
  public LoxObject call(Interpreter interpreter, List<LoxObject> arguments) throws InterpreterException {
    final LoxObject blankObj = new LoxObject(this) {
      @Override
      public String toString() {
        return String.format("<instance %s>", this.cls.name);
      }

      @Override
      public Object value() {
        return this;
      }
    };

    final LoxFunction constructor = this.lookupMethod("constructor");
    if (constructor == null) {
      return blankObj;
    }

    final LoxBoundedFunction boundedConstructor = new LoxBoundedFunction(blankObj, constructor);
    boundedConstructor.call(interpreter, arguments);

    return blankObj;
  }
}
