package com.lox;

public class InterpreterException extends Exception {
  public final String message;

  public InterpreterException(String message) {
    this.message = message;
  }
}
