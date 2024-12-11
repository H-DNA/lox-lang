package com.lox;

import java.util.List;
import com.lox.*;
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

    assertEquals(tokens.get(1).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(1).lexeme, "formless");

    assertEquals(tokens.get(2).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(2).lexeme, "fo");

    assertEquals(tokens.get(3).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(3).lexeme, "_");

    assertEquals(tokens.get(4).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(4).lexeme, "_123");

    assertEquals(tokens.get(5).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(5).lexeme, "_abc");

    assertEquals(tokens.get(6).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(6).lexeme, "ab123");

    assertEquals(tokens.get(7).type, TokenType.IDENTIFIER);
    assertEquals(tokens.get(7).lexeme, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_");

    assertEquals(tokens.get(8).type, TokenType.EOF);
  }
}
