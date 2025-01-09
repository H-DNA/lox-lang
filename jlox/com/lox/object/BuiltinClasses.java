package com.lox.object;

import com.lox.Environment;
import com.lox.InterpreterException;

public class BuiltinClasses {
  public static LoxClass LObject = new LoxClass("Object");
  public static LoxClass LNumber = new LoxClass("Number", BuiltinClasses.LObject);
  public static LoxClass LString = new LoxClass("String", BuiltinClasses.LObject);
  public static LoxClass LBoolean = new LoxClass("Boolean", BuiltinClasses.LObject);
  public static LoxClass LNil = new LoxClass("Nil", BuiltinClasses.LObject);
  public static LoxClass LCallable = new LoxClass("Callable", BuiltinClasses.LObject);
  public static LoxClass LFunction = new LoxClass("Function", BuiltinClasses.LCallable);
  public static LoxClass LClass = new LoxClass("Class", BuiltinClasses.LCallable);
}
