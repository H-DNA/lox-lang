package com.lox.object;

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
  public LoxClass cls;

  public LoxObject(LoxClass cls) {
    this.cls = cls;
  }
}
