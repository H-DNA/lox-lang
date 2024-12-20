package com.lox;

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
  public void testLiteral() throws Exception {
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("\"true\"")), "\"true\"");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("true")), "true");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("\"false\"")), "\"false\"");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("false")), "false");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("0")), "0");
  }

  @Test
  public void testVariable() throws Exception {
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("a")), "a");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("a1")), "a1");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("_a1")), "_a1");
  }

  @Test
  public void testUnary() throws Exception {
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("-1")), "(- 1)");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("!1")), "(! 1)");

    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("-   1")), "(- 1)");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("!   1")), "(! 1)");

    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("!\"\"")), "(! \"\")");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("-\"\"")), "(- \"\")");

    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("! -1")), "(! (- 1))");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("- !1")), "(- (! 1))");

    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("!!1")), "(! (! 1))");
    assertEquals(TestUtils.prettyPrintExpr(TestUtils.parseExpr("--1")), "(- (- 1))");
  }
}

class TestUtils {
  static Expr parseExpr(String source) throws Exception {
    Method method = Parser.class.getDeclaredMethod("expression");
    method.setAccessible(true);

    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);

    return (Expr)method.invoke(parser);
  }

  static String prettyPrintExpr(Expr expr) {
    PrettyPrinter printer = new PrettyPrinter();
    return printer.printExpr(expr);
  }
}
