package com.lox;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lox.*;
import com.lox.ast.Expr;
import com.lox.ast.SyntaxNode;
import com.lox.ast.Token;
import com.lox.ast.TokenType;
import com.lox.utils.Pair;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
  @Test
  public void testLiteral() throws Throwable {
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("\"true\"")), "\"true\"");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("true")), "true");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("\"false\"")), "\"false\"");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("false")), "false");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("0")), "0");
  }

  @Test
  public void testVariable() throws Throwable {
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("a")), "a");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("a1")), "a1");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("_a1")), "_a1");
  }

  @Test
  public void testUnary() throws Throwable {
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("-1")), "(- 1)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("!1")), "(! 1)");

    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("-   1")), "(- 1)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("!   1")), "(! 1)");

    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("!\"\"")), "(! \"\")");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("-\"\"")), "(- \"\")");

    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("! -1")), "(! (- 1))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("- !1")), "(- (! 1))");

    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("!!1")), "(! (! 1))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("--1")), "(- (- 1))");
  }

  @Test
  public void testGrouping() throws Throwable {
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(1)")), "(group 1)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(\"abc\")")), "(group \"abc\")");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(true)")), "(group true)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(false)")), "(group false)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(1 + 2)")), "(group (+ 1 2))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(! 2)")), "(group (! 2))");
  }

  @Test
  public void testBinary() throws Throwable {
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 + 2")), "(+ 1 2)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 + (2)")), "(+ 1 (group 2))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 + (2 + 3)")), "(+ 1 (group (+ 2 3)))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 + 2 * 3)")), "(+ 1 (* 2 3))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 * 2 + 3)")), "(+ (* 1 2) 3)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("(1 + 2) * 3)")), "(* (group (+ 1 2)) 3)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 - 2 == 3)")), "(== (- 1 2) 3)");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 - 2 * 4 == 3 / 5")), "(== (- 1 (* 2 4)) (/ 3 5))");
    assertEquals(ParserTestUtils.prettyPrintExpr(ParserTestUtils.parseExpr("1 - 2 * 4 == 3 / 5 != 6 >= 3")), "(!= (== (- 1 (* 2 4)) (/ 3 5)) (>= 6 3))");
  }

  @Test
  public void testInvalidGroup() throws Throwable {
    try {
      ParserTestUtils.parseExpr("(1 + 2");
    } catch (ParserException e) {
      assertEquals(e.message, "Expect a closing parenthesis ')'");
    }

    try {
      ParserTestUtils.parseExpr("1 + (2");
    } catch (ParserException e) {
      assertEquals(e.message, "Expect a closing parenthesis ')'");
    }
  }

  @Test
  public void testInvalidPrimary() throws Throwable {
    try {
      ParserTestUtils.parseExpr("+1 + 2");
    } catch (ParserException e) {
      assertEquals(e.message, "Expect a numeric literal, string literal, variable or grouping expression");
    }

    try {
      ParserTestUtils.parseExpr("+2");
    } catch (ParserException e) {
      assertEquals(e.message, "Expect a numeric literal, string literal, variable or grouping expression");
    }
  }
}

class ParserTestUtils {
  static Expr parseExpr(String source) throws Throwable {
    Method method = Parser.class.getDeclaredMethod("expression");
    method.setAccessible(true);

    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);

    try {
      return (Expr)method.invoke(parser);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  static String prettyPrintExpr(Expr expr) {
    PrettyPrinter printer = new PrettyPrinter();
    return printer.printExpr(expr);
  }
}
