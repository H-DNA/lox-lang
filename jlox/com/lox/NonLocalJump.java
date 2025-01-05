package com.lox;

import com.lox.object.LoxObject;

public class NonLocalJump extends InterpreterException {
  NonLocalJump(String message) {
    super(message);
  }

  public static class Return extends NonLocalJump {
    final public LoxObject value;

    public Return(LoxObject value) {
      super("Cannot `return` outside a function body");
      this.value = value;
    }
  }
}
