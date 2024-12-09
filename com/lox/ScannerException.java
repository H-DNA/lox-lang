package com.lox;

class ScannerException extends Exception {
  final int startOffset;
  final int endOffset;
  final String message;

  ScannerException(String message, int startOffset, int endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
    this.message = message;
  }
}
