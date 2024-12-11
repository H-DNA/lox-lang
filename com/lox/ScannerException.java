package com.lox;

public class ScannerException extends Exception {
  public final int startOffset;
  public final int endOffset;
  public final String message;

  public ScannerException(String message, int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.message = message;
  }
}
