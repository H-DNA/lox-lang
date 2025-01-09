package com.lox.object;

import java.util.ArrayList;

import com.lox.Environment;
import com.lox.InterpreterException;

public class BuiltinClasses {
  public static LoxClass LObject = new LoxClass("Object", new ArrayList<>());
  public static LoxClass LNumber = new LoxClass("Number", BuiltinClasses.LObject, new ArrayList<>());
  public static LoxClass LString = new LoxClass("String", BuiltinClasses.LObject, new ArrayList<>());
  public static LoxClass LBoolean = new LoxClass("Boolean", BuiltinClasses.LObject, new ArrayList<>());
  public static LoxClass LNil = new LoxClass("Nil", BuiltinClasses.LObject, new ArrayList<>());
  public static LoxClass LCallable = new LoxClass("Callable", BuiltinClasses.LObject, new ArrayList<>());
  public static LoxClass LFunction = new LoxClass("Function", BuiltinClasses.LCallable, new ArrayList<>());
  public static LoxClass LBoundedFunction = new LoxClass("BoundedFunction", BuiltinClasses.LCallable, new ArrayList<>());
  public static LoxClass LClass = new LoxClass("Class", BuiltinClasses.LCallable, new ArrayList<>());
}
