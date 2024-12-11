package com.lox;

public class ScannerException extends Exception {
  final int startOffset;
  final int endOffset;
  final String message;

  public ScannerException(String message, int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.message = message;
  }
}
