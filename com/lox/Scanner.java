package com.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private final List<ScannerException> errors = new ArrayList<>();

  private int currentOffset = 0;

  public Scanner(String source) {
    this.source = source;
  }

  public Pair<List<Token>, List<ScannerException>> tokenize() {
    if (this.isAtEnd())
      return new Pair<>(this.tokens, this.errors);

    while (!this.isAtEnd()) {
      try {
        this.extractNextToken();
      } catch (ScannerException e) {
        this.errors.add(e);
      }
    }
    this.tokens.add(new Token(TokenType.EOF, "", null, this.currentOffset, this.currentOffset));
    return new Pair<>(this.tokens, this.errors);
  }

  private void extractNextToken() throws ScannerException {
    assert !this.isAtEnd();

    int startOffset = currentOffset;
    char c = this.advance();
    switch (c) {
      case '(':
        this.tokens.add(new Token(
            TokenType.LEFT_PAREN,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case ')':
        this.tokens.add(new Token(
            TokenType.RIGHT_PAREN,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '{':
        this.tokens.add(new Token(
            TokenType.LEFT_BRACE,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '}':
        this.tokens.add(new Token(
            TokenType.RIGHT_BRACE,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case ',':
        this.tokens.add(new Token(
            TokenType.COMMA,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '.':
        this.tokens.add(new Token(
            TokenType.DOT,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '-':
        this.tokens.add(new Token(
            TokenType.MINUS,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '+':
        this.tokens.add(new Token(
            TokenType.PLUS,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case ';':
        this.tokens.add(new Token(
            TokenType.SEMICOLON,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '*':
        this.tokens.add(new Token(
            TokenType.STAR,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '!':
        this.tokens.add(new Token(
            this.match('=') ? TokenType.BANG_EQUAL : TokenType.BANG,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '=':
        this.tokens.add(new Token(
            this.match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '<':
        this.tokens.add(new Token(
            this.match('=') ? TokenType.LESS_EQUAL : TokenType.LESS,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '>':
        this.tokens.add(new Token(
            this.match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '/':
        if (this.match('/')) {
          while (this.peek() != '\n' && !this.isAtEnd())
            this.advance();
          break;
        }
        this.tokens.add(new Token(
            TokenType.SLASH,
            this.source.substring(startOffset, this.currentOffset),
            null,
            startOffset,
            this.currentOffset));
        break;
      case '"':
        this.extractString();
        break;
      default:
        if (ScannerUtils.isSpace(c))
          break;
        if (ScannerUtils.isAlpha(c)) {
          this.extractIdentifier();
          break;
        }
        if (ScannerUtils.isDigit(c)) {
          this.extractNumber();
          break;
        }
        throw new ScannerException("Unknown character: " + Character.valueOf(c), startOffset, this.currentOffset);
    }
  }

  private boolean isAtEnd() {
    return this.currentOffset >= source.length();
  }

  private void extractString() throws ScannerException {
    assert this.source.charAt(this.currentOffset - 1) == '"';

    int startOffset = this.currentOffset - 1;

    while (this.peek() != '"' && !this.isAtEnd()) {
      this.advance();
    }

    if (this.isAtEnd()) {
      throw new ScannerException("Unterminated string literal", startOffset, this.currentOffset);
    }

    this.advance(); // Consume the closing '"'

    this.tokens.add(new Token(
        TokenType.STRING,
        this.source.substring(startOffset, this.currentOffset),
        this.source.substring(startOffset + 1, this.currentOffset - 1),
        startOffset,
        this.currentOffset));
  }

  private void extractNumber() throws ScannerException {
    assert ScannerUtils.isDigit(this.source.charAt(this.currentOffset - 1));

    int startOffset = this.currentOffset - 1;

    // Match invalid numerical literals also
    while (ScannerUtils.isAlphaNumericOrUnderscore(this.peek()))
      this.advance();

    // NOTE: Accept "1." as Double
    if (this.peek() == '.') {
      this.advance();
      // Match invalid numerical literals also
      while (ScannerUtils.isAlphaNumericOrUnderscore(this.peek()))
        this.advance();
    }

    try {
      this.tokens.add(new Token(
          TokenType.NUMBER,
          this.source.substring(startOffset, this.currentOffset),
          Double.parseDouble(source.substring(startOffset, this.currentOffset)),
          startOffset,
          this.currentOffset));
    } catch (Exception e) {
      // Double.parseDouble will fail if the numerical literal is invalid
      throw new ScannerException("Invalid literal", startOffset, this.currentOffset);
    }
  }

  private void extractIdentifier() {
    assert ScannerUtils.isAlpha(this.source.charAt(this.currentOffset - 1));

    int startOffset = this.currentOffset - 1;

    while (ScannerUtils.isAlphaNumericOrUnderscore(this.peek()))
      this.advance();

    String text = source.substring(startOffset, this.currentOffset);
    TokenType type = ScannerUtils.keywords.get(text);
    if (type == null)
      type = TokenType.IDENTIFIER;

    this.tokens.add(new Token(
        type,
        this.source.substring(startOffset, this.currentOffset),
        null,
        startOffset,
        this.currentOffset));
  }

  private char advance() {
    assert !this.isAtEnd();
    char c = this.source.charAt(this.currentOffset);
    this.currentOffset += 1;
    return c;
  }

  private boolean match(char expected) {
    assert !this.isAtEnd();
    if (expected != this.source.charAt(this.currentOffset))
      return false;
    this.currentOffset += 1;
    return true;
  }

  private char peek() {
    if (isAtEnd())
      return '\0';
    return this.source.charAt(this.currentOffset);
  }
}

class ScannerUtils {
  static boolean isDigit(char c) {
    return Character.isDigit(c);
  }

  static boolean isAlpha(char c) {
    return Character.isLetter(c);
  }

  static boolean isAlphaOrUnderscore(char c) {
    return ScannerUtils.isAlpha(c) || c == '_';
  }

  static boolean isSpace(char c) {
    return Character.isSpaceChar(c);
  }

  static boolean isAlphaNumeric(char c) {
    return ScannerUtils.isDigit(c) || ScannerUtils.isAlpha(c);
  }

  static boolean isAlphaNumericOrUnderscore(char c) {
    return ScannerUtils.isDigit(c) || ScannerUtils.isAlphaOrUnderscore(c);
  }

  static final Map<String, TokenType> keywords;
  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }
}
