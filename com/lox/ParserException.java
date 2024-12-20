package com.lox;

public class ParserException extends Exception {
  public final int startOffset;
  public final int endOffset;
  public final String message;

  public ParserException(String message, int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.message = message;
  }
}
