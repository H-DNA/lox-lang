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

public class InterpreterTest {
  @Test
  public void testLiteral() throws Throwable {
    assertEquals(InterpreterTestUtils.evaluateExpr("\"true\""), "true");
    assertEquals(InterpreterTestUtils.evaluateExpr("false"), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("true"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("0"), 0.0);
  }

  @Test
  public void testUnary() throws Throwable {
    assertEquals(InterpreterTestUtils.evaluateExpr("-1"), -1.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("!1"), false);

    assertEquals(InterpreterTestUtils.evaluateExpr("-   1"), -1.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("!   1"), false);

    assertEquals(InterpreterTestUtils.evaluateExpr("!\"\""), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("! -1"), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("!0"), false);

    assertEquals(InterpreterTestUtils.evaluateExpr("!!1"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("--1"), 1.0);
  }

  @Test
  public void testGrouping() throws Throwable {
    assertEquals(InterpreterTestUtils.evaluateExpr("(1)"), 1.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("(\"abc\")"), "abc");
    assertEquals(InterpreterTestUtils.evaluateExpr("(true)"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("(false)"), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("(1 + 2)"), 3.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("(! 2)"), false);
  }

  @Test
  public void testBinary() throws Throwable {
    assertEquals(InterpreterTestUtils.evaluateExpr("1 + 2"), 3.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("1 + (2)"), 3.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("1 + (2 + 3)"), 6.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("1 + 2 * 3)"), 7.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("1 * 2 + 3)"), 5.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("(1 + 2) * 3)"), 9.0);
    assertEquals(InterpreterTestUtils.evaluateExpr("1 - 2 == 3)"), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("1 - 2 * 4 == -7"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("2 / 1 == 2)"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("2 / 1 >= 1)"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("2 / 1 > 1)"), true);
    assertEquals(InterpreterTestUtils.evaluateExpr("2 / 1 <= 1)"), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("2 / 1 < 1)"), false);
    assertEquals(InterpreterTestUtils.evaluateExpr("2 / 1 != 1)"), true);
  }
}

class InterpreterTestUtils {
  static Object evaluateExpr(String source) throws Throwable {
    Method method = Parser.class.getDeclaredMethod("expression");
    method.setAccessible(true);

    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);

    Interpreter interpreter = new Interpreter();
    try {
      final Expr expr = (Expr)method.invoke(parser);
      return interpreter.evaluateExpr(expr).value();
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }
}
