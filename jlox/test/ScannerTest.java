package com.lox;

import java.util.List;
import com.lox.*;
import com.lox.ast.Token;
import com.lox.ast.TokenType;
import com.lox.utils.Pair;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ScannerTest {
  @Test
  public void testIdentifiers() {
    String source = """
          andy formless fo _ _123 _abc ab123
          abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_
        """;

    Scanner scanner = new Scanner(source);
    Pair<List<Token>, List<ScannerException>> res = scanner.tokenize();

    List<Token> tokens = res.first;
    List<ScannerException> errors = res.second;

    // Test errors
    assertEquals(errors.size(), 0);

    // Test tokens
    assertEquals(tokens.size(), 9); // 8 identifiers + 1 EOF

    assertEquals(tokens.get(0).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(0).lexeme, "andy");
    assertEquals(tokens.get(0).literal, null);

    assertEquals(tokens.get(1).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(1).lexeme, "formless");
    assertEquals(tokens.get(1).literal, null);

    assertEquals(tokens.get(2).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(2).lexeme, "fo");
    assertEquals(tokens.get(2).literal, null);

    assertEquals(tokens.get(3).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(3).lexeme, "_");
    assertEquals(tokens.get(3).literal, null);

    assertEquals(tokens.get(4).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(4).lexeme, "_123");
    assertEquals(tokens.get(4).literal, null);

    assertEquals(tokens.get(5).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(5).lexeme, "_abc");
    assertEquals(tokens.get(5).literal, null);

    assertEquals(tokens.get(6).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(6).lexeme, "ab123");
    assertEquals(tokens.get(6).literal, null);

    assertEquals(tokens.get(7).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(7).lexeme, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_");
    assertEquals(tokens.get(7).literal, null);

    assertEquals(tokens.get(8).type, TokenType.EOF);
    assertEquals(tokens.get(8).literal, null);
  }

  @Test
  public void testKeywords() {
    String source = """
          and class else false for fun if nil or return true var while super
        """;

    Scanner scanner = new Scanner(source);
    Pair<List<Token>, List<ScannerException>> res = scanner.tokenize();

    List<Token> tokens = res.first;
    List<ScannerException> errors = res.second;

    // Test errors
    assertEquals(errors.size(), 0);

    // Test tokens
    assertEquals(tokens.size(), 15); // 14 keywords + 1 EOF

    assertEquals(tokens.get(0).type, TokenType.AND);
    assertEquals(tokens.get(0).lexeme, "and");
    assertEquals(tokens.get(0).literal, null);

    assertEquals(tokens.get(1).type, TokenType.CLASS);
    assertEquals(tokens.get(1).lexeme, "class");
    assertEquals(tokens.get(1).literal, null);

    assertEquals(tokens.get(2).type, TokenType.ELSE);
    assertEquals(tokens.get(2).lexeme, "else");
    assertEquals(tokens.get(2).literal, null);

    assertEquals(tokens.get(3).type, TokenType.FALSE);
    assertEquals(tokens.get(3).lexeme, "false");
    assertEquals(tokens.get(3).literal, false);

    assertEquals(tokens.get(4).type, TokenType.FOR);
    assertEquals(tokens.get(4).lexeme, "for");
    assertEquals(tokens.get(4).literal, null);

    assertEquals(tokens.get(5).type, TokenType.FUN);
    assertEquals(tokens.get(5).lexeme, "fun");
    assertEquals(tokens.get(5).literal, null);

    assertEquals(tokens.get(6).type, TokenType.IF);
    assertEquals(tokens.get(6).lexeme, "if");
    assertEquals(tokens.get(6).literal, null);

    assertEquals(tokens.get(7).type, TokenType.NIL);
    assertEquals(tokens.get(7).lexeme, "nil");
    assertEquals(tokens.get(7).literal, null);

    assertEquals(tokens.get(8).type, TokenType.OR);
    assertEquals(tokens.get(8).lexeme, "or");
    assertEquals(tokens.get(8).literal, null);

    assertEquals(tokens.get(9).type, TokenType.RETURN);
    assertEquals(tokens.get(9).lexeme, "return");
    assertEquals(tokens.get(9).literal, null);

    assertEquals(tokens.get(10).type, TokenType.TRUE);
    assertEquals(tokens.get(10).lexeme, "true");
    assertEquals(tokens.get(10).literal, true);

    assertEquals(tokens.get(11).type, TokenType.VAR);
    assertEquals(tokens.get(11).lexeme, "var");
    assertEquals(tokens.get(11).literal, null);

    assertEquals(tokens.get(12).type, TokenType.WHILE);
    assertEquals(tokens.get(12).lexeme, "while");
    assertEquals(tokens.get(12).literal, null);

    assertEquals(tokens.get(13).type, TokenType.SUPER);
    assertEquals(tokens.get(13).lexeme, "super");
    assertEquals(tokens.get(13).literal, null);

    assertEquals(tokens.get(14).type, TokenType.EOF);
    assertEquals(tokens.get(14).literal, null);
  }

  @Test
  public void testNumbers() {
    String source = """
          123
          123.456
          123.

          123.abc
          1a3.bc
          13a.02
        """;

    Scanner scanner = new Scanner(source);
    Pair<List<Token>, List<ScannerException>> res = scanner.tokenize();

    List<Token> tokens = res.first;
    List<ScannerException> errors = res.second;

    // Test errors
    assertEquals(errors.size(), 3);

    assertEquals(errors.get(0).message, "Invalid literal");
    assertEquals(errors.get(1).message, "Invalid literal");
    assertEquals(errors.get(2).message, "Invalid literal");

    // Test tokens
    assertEquals(tokens.size(), 4); // 3 numbers + 1 EOF

    assertEquals(tokens.get(0).type, TokenType.NUMBER);
    assertEquals(tokens.get(0).lexeme, "123");
    assertEquals(tokens.get(0).literal, 123.0);

    assertEquals(tokens.get(1).type, TokenType.NUMBER);
    assertEquals(tokens.get(1).lexeme, "123.456");
    assertEquals(tokens.get(1).literal, 123.456);

    assertEquals(tokens.get(2).type, TokenType.NUMBER);
    assertEquals(tokens.get(2).lexeme, "123.");
    assertEquals(tokens.get(2).literal, 123.);

    assertEquals(tokens.get(3).type, TokenType.EOF);
    assertEquals(tokens.get(3).literal, null);
  }

  @Test
  public void testPunctuators() {
    String source = """
          (){};,+-*! == <= = >= != < > /.
        """;

    Scanner scanner = new Scanner(source);
    Pair<List<Token>, List<ScannerException>> res = scanner.tokenize();

    List<Token> tokens = res.first;
    List<ScannerException> errors = res.second;

    // Test errors
    assertEquals(errors.size(), 0);

    // Test tokens
    assertEquals(tokens.size(), 20); // 19 punctuators + 1 EOF

    assertEquals(tokens.get(0).type, TokenType.LEFT_PAREN);
    assertEquals(tokens.get(0).lexeme, "(");
    assertEquals(tokens.get(0).literal, null);

    assertEquals(tokens.get(1).type, TokenType.RIGHT_PAREN);
    assertEquals(tokens.get(1).lexeme, ")");
    assertEquals(tokens.get(1).literal, null);

    assertEquals(tokens.get(2).type, TokenType.LEFT_BRACE);
    assertEquals(tokens.get(2).lexeme, "{");
    assertEquals(tokens.get(2).literal, null);

    assertEquals(tokens.get(3).type, TokenType.RIGHT_BRACE);
    assertEquals(tokens.get(3).lexeme, "}");
    assertEquals(tokens.get(3).literal, null);

    assertEquals(tokens.get(4).type, TokenType.SEMICOLON);
    assertEquals(tokens.get(4).lexeme, ";");
    assertEquals(tokens.get(4).literal, null);

    assertEquals(tokens.get(5).type, TokenType.COMMA);
    assertEquals(tokens.get(5).lexeme, ",");
    assertEquals(tokens.get(5).literal, null);

    assertEquals(tokens.get(6).type, TokenType.PLUS);
    assertEquals(tokens.get(6).lexeme, "+");
    assertEquals(tokens.get(6).literal, null);

    assertEquals(tokens.get(7).type, TokenType.MINUS);
    assertEquals(tokens.get(7).lexeme, "-");
    assertEquals(tokens.get(7).literal, null);

    assertEquals(tokens.get(8).type, TokenType.STAR);
    assertEquals(tokens.get(8).lexeme, "*");
    assertEquals(tokens.get(8).literal, null);

    assertEquals(tokens.get(9).type, TokenType.BANG);
    assertEquals(tokens.get(9).lexeme, "!");
    assertEquals(tokens.get(9).literal, null);

    assertEquals(tokens.get(10).type, TokenType.EQUAL_EQUAL);
    assertEquals(tokens.get(10).lexeme, "==");
    assertEquals(tokens.get(10).literal, null);

    assertEquals(tokens.get(11).type, TokenType.LESS_EQUAL);
    assertEquals(tokens.get(11).lexeme, "<=");
    assertEquals(tokens.get(11).literal, null);

    assertEquals(tokens.get(12).type, TokenType.EQUAL);
    assertEquals(tokens.get(12).lexeme, "=");
    assertEquals(tokens.get(12).literal, null);

    assertEquals(tokens.get(13).type, TokenType.GREATER_EQUAL);
    assertEquals(tokens.get(13).lexeme, ">=");
    assertEquals(tokens.get(13).literal, null);

    assertEquals(tokens.get(14).type, TokenType.BANG_EQUAL);
    assertEquals(tokens.get(14).lexeme, "!=");
    assertEquals(tokens.get(14).literal, null);

    assertEquals(tokens.get(15).type, TokenType.LESS);
    assertEquals(tokens.get(15).lexeme, "<");
    assertEquals(tokens.get(15).literal, null);

    assertEquals(tokens.get(16).type, TokenType.GREATER);
    assertEquals(tokens.get(16).lexeme, ">");
    assertEquals(tokens.get(16).literal, null);

    assertEquals(tokens.get(17).type, TokenType.SLASH);
    assertEquals(tokens.get(17).lexeme, "/");
    assertEquals(tokens.get(17).literal, null);

    assertEquals(tokens.get(18).type, TokenType.DOT);
    assertEquals(tokens.get(18).lexeme, ".");
    assertEquals(tokens.get(18).literal, null);

    assertEquals(tokens.get(19).type, TokenType.EOF);
    assertEquals(tokens.get(19).literal, null);
  }

  @Test
  public void testStrings() {
    String source = """
          ""
          "string"
          "
        """;

    Scanner scanner = new Scanner(source);
    Pair<List<Token>, List<ScannerException>> res = scanner.tokenize();

    List<Token> tokens = res.first;
    List<ScannerException> errors = res.second;

    // Test errors
    assertEquals(errors.size(), 1);

    assertEquals(errors.get(0).message, "Unterminated string literal");

    // Test tokens
    assertEquals(tokens.size(), 3); // 2 strings + 1 EOF

    assertEquals(tokens.get(0).type, TokenType.STRING);
    assertEquals(tokens.get(0).lexeme, "\"\"");
    assertEquals(tokens.get(0).literal, "");

    assertEquals(tokens.get(1).type, TokenType.STRING);
    assertEquals(tokens.get(1).lexeme, "\"string\"");
    assertEquals(tokens.get(1).literal, "string");

    assertEquals(tokens.get(2).type, TokenType.EOF);
    assertEquals(tokens.get(2).literal, null);
  }
}
