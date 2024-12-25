package com.lox;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lox.*;
import com.lox.ast.Expr;
import com.lox.ast.Stmt;
import com.lox.ast.SyntaxNode;
import com.lox.ast.Token;
import com.lox.ast.TokenType;
import com.lox.utils.Pair;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {
  @Test
  public void testLiteralStmt() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("\"true\";"), "\"true\"");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("true;"), "true");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("\"false\";"), "\"false\"");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("false;"), "false");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("0;"), "0");
  }

  @Test
  public void testVariable() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("a;"), "a");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("a1;"), "a1");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("_a1;"), "_a1");
  }

  @Test
  public void testUnary() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("-1;"), "(- 1)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("!1;"), "(! 1)");

    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("-   1;"), "(- 1)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("!   1;"), "(! 1)");

    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("!\"\";"), "(! \"\")");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("-\"\";"), "(- \"\")");

    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("! -1;"), "(! (- 1))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("- !1;"), "(- (! 1))");

    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("!!1;"), "(! (! 1))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("--1;"), "(- (- 1))");
  }

  @Test
  public void testGrouping() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(1);"), "(group 1)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(\"abc\");"), "(group \"abc\")");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(true);"), "(group true)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(false);"), "(group false)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(1 + 2);"), "(group (+ 1 2))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(! 2);"), "(group (! 2))");
  }

  @Test
  public void testBinary() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 + 2;"), "(+ 1 2)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 + (2);"), "(+ 1 (group 2))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 + (2 + 3);"), "(+ 1 (group (+ 2 3)))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 + 2 * 3;"), "(+ 1 (* 2 3))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 * 2 + 3;"), "(+ (* 1 2) 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("(1 + 2) * 3;"), "(* (group (+ 1 2)) 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 - 2 == 3;"), "(== (- 1 2) 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 - 2 * 4 == 3 / 5;"), "(== (- 1 (* 2 4)) (/ 3 5))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("1 - 2 * 4 == 3 / 5 != 6 >= 3;"), "(!= (== (- 1 (* 2 4)) (/ 3 5)) (>= 6 3))");
  }

  @Test
  public void testInvalidGroup() throws Throwable {
    ParserTestUtils.assertOneError(ParserTestUtils.parse("(1 + 2"), "Expect a closing parenthesis ')'");

    ParserTestUtils.assertOneError(ParserTestUtils.parse("1 + (2"), "Expect a closing parenthesis ')'");
  }

  @Test
  public void testInvalidPrimary() throws Throwable {
    ParserTestUtils.assertOneError(ParserTestUtils.parse("+1 + 2"),
        "Expect a numeric literal, string literal, variable or grouping expression");

    ParserTestUtils.assertOneError(ParserTestUtils.parse("+2"),
        "Expect a numeric literal, string literal, variable or grouping expression");
  }

  @Test
  public void testPrintStmt() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print a + 2;"), "(print (+ a 2))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 + (a);"), "(print (+ 1 (group a)))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 + (b + 3);"),
        "(print (+ 1 (group (+ b 3))))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 + 2 * 3;"), "(print (+ 1 (* 2 3)))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 * 2 + 3;"), "(print (+ (* 1 2) 3))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print (1 + 2) * 3;"),
        "(print (* (group (+ 1 2)) 3))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 - 2 == 3;"), "(print (== (- 1 2) 3))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 - 2 * 4 == 3 / 5;"),
        "(print (== (- 1 (* 2 4)) (/ 3 5)))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("print 1 - 2 * 4 == 3 / 5 != 6 >= 3;"),
        "(print (!= (== (- 1 (* 2 4)) (/ 3 5)) (>= 6 3)))");
  }

  @Test
  public void testVarDecl() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("var x = 1 + 2;"), "(define x (+ 1 2))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("var yy = 1 + (2);"),
        "(define yy (+ 1 (group 2)))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("var _z1;"), "(define _z1)");
  }

  @Test
  public void testInvalidVarDecl() throws Throwable {
    ParserTestUtils.assertOneError(ParserTestUtils.parse("var"), "Expect an identifier");
    ParserTestUtils.assertOneError(ParserTestUtils.parse("var x ="), "Expect a numeric literal, string literal, variable or grouping expression");
  }
}

class ParserTestUtils {
  static Pair<List<Stmt>, List<ParserException>> parse(String source) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    return parser.parse();
  }

  static String prettyPrint(List<Stmt> stmts) {
    PrettyPrinter printer = new PrettyPrinter();
    return printer.print(stmts);
  }

  static void assertNoErrorAndResultEquals(Pair<List<Stmt>, List<ParserException>> res, String prettyPrintedText) {
    assertEquals(res.second.size(), 0);
    assertEquals(ParserTestUtils.prettyPrint(res.first), prettyPrintedText);
  }

  static void assertOneError(Pair<List<Stmt>, List<ParserException>> res, String firstErrorMessage) {
    assertEquals(res.second.size(), 1);
    assertEquals(res.second.get(0).message, firstErrorMessage);
  }
}
