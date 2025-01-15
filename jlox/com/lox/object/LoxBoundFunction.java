package com.lox.object;

import java.util.ArrayList;
import java.util.List;

import com.lox.Environment;
import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.NonLocalJump;
import com.lox.ast.Token;
import com.lox.ast.Stmt.FuncStmt;
import com.lox.object.LoxClass;
import com.lox.object.LoxObject;

public class LoxBoundFunction extends LoxFunction {
  public static final LoxClass OBJECT = new LoxClass("BoundFunction", LoxFunction.OBJECT, new ArrayList<>());

  public final LoxFunction funcObj;
  public final LoxObject owner;
  public final LoxClass ownerCls;

  public LoxBoundFunction(LoxClass cls, LoxObject owner, LoxFunction funcObj) throws InterpreterException {
    super();
    if (funcObj instanceof LoxBoundFunction) {
      throw new InterpreterException("Cannot bound a BoundFunction");
    }
    this.funcObj = funcObj;
    this.owner = owner;
    this.ownerCls = cls;
  }

  public Environment env() {
    return this.funcObj.env();
  }

  @Override
  public LoxClass cls() {
    return LoxBoundFunction.OBJECT;
  }

  @Override
  public String name() {
    return this.funcObj.name();
  }

  @Override
  public int arity() {
    return this.funcObj.arity();
  }

  @Override
  public String toString() {
    return String.format("<bound function %s>", this.funcObj.name());
  }
}
