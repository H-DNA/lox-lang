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
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("nil;"), "nil");
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
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("a = b = 3;"), "(= a (= b 3))");
    ParserTestUtils.assertErrors(ParserTestUtils.parse("a = 2 = 3;"), new String[] {"Invalid assignment target"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("1 = a = 3;"), new String[] {"Invalid assignment target"});
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("a and b or 3;"), "(or (and a b) 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("a or b or 3;"), "(or (or a b) 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("a or b and 3;"), "(or a (and b 3))");
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
    ParserTestUtils.assertErrors(ParserTestUtils.parse("(1 + 2"), new String[] {"Expect a closing parenthesis ')'"});

    ParserTestUtils.assertErrors(ParserTestUtils.parse("1 + (2"), new String[] {"Expect a closing parenthesis ')'"});
  }

  @Test
  public void testInvalidPrimary() throws Throwable {
    ParserTestUtils.assertErrors(ParserTestUtils.parse("+1 + 2"),
        new String[] {"Expect a numeric literal, string literal, variable or grouping expression"});

    ParserTestUtils.assertErrors(ParserTestUtils.parse("+2"),
        new String[] {"Expect a numeric literal, string literal, variable or grouping expression"});
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
    ParserTestUtils.assertErrors(ParserTestUtils.parse("var"), new String[] {"Expect an identifier"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("var x ="), new String[] {"Expect a numeric literal, string literal, variable or grouping expression"});
  }

  @Test
  public void testBlock() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("{}"), "(block)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("var a = 3; { var b = 3; }"), "(define a 3)\n(block (define b 3))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("var a = 3; { var b = 3; } var c = 3;"), "(define a 3)\n(block (define b 3))\n(define c 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("{ var a = 3; var b = a + 1; c; }"), "(block (define a 3) (define b (+ a 1)) c)");
  }

  @Test
  void testInvalidBlock() throws Throwable {
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{"), new String[] {"EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ var "), new String[] {"Expect an identifier", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ var a"), new String[] {"Expect '=' or an ending semicolon ';'", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ fun"), new String[] {"Expect an identifier", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ fun x"), new String[] {"Expect an opening parenthesis '('", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ fun x("), new String[] {"Expect an identifier", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ fun x()"), new String[] {"Expect an opening brace '{'", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ fun x() {"), new String[] {"EOF reached while parsing block statement", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ var a = 3; "), new String[] {"EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ var a = ; var c = 3; "), new String[] {"Expect a numeric literal, string literal, variable or grouping expression", "EOF reached while parsing block statement"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("{ var a = d var c = 3; "), new String[] {"Expect an ending semicolon ';'", "EOF reached while parsing block statement"});
  }

  @Test
  void testRecoverBlock() throws Throwable {
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("{ var a = d var c = 3; "), "(block (define a d))");
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("{ fun f() var a = b; var d = e;"), "(block (define d e))");
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("{ fun f() { var a = b; var d = e;"), "(block (fun (f) (block (define a b) (define d e))))");
  }

  @Test
  public void testIfStmt() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("if (x) x;"), "(if x then x)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("if (x) x; else x;"), "(if x then x else x)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("if (x == 1) x + 1; else x - 1;"), "(if (== x 1) then (+ x 1) else (- x 1))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("if (x == 1) if (x != 2) 3; else 2;"), "(if (== x 1) then (if (!= x 2) then 3 else 2))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("if (x == 1) if (x != 2) 3; else 2; else x - 1;"), "(if (== x 1) then (if (!= x 2) then 3 else 2) else (- x 1))");
  }

  @Test
  public void testWhileStmt() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("while (x) x;"), "(while x do x)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("while (x) { x + 1; print x; }"), "(while x do (block (+ x 1) (print x)))");
  }

  @Test
  public void testForStmt() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("for (var x = 0; x < 10; x = x + 1) x;"), "(for (define x 0) (< x 10) (= x (+ x 1)) do x)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("for (var x = 0; x < 10; x = x + 1) { x + 1; print x; }"), "(for (define x 0) (< x 10) (= x (+ x 1)) do (block (+ x 1) (print x)))");
  }

  @Test
  public void testCall() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("f(3);"), "(f 3)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("f();"), "(f)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("f(1, 2);"), "(f 1 2)");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("f(1, 2 + 3);"), "(f 1 (+ 2 3))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("f(1, f(1));"), "(f 1 (f 1))");
  }

  @Test
  public void testFunc() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("fun f() {}"), "(fun (f) (block))");
    ParserTestUtils.assertErrors(ParserTestUtils.parse("fun f(a,) {}"), new String[] {"Expect an identifier"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("fun f(a, {}"), new String[] {"Expect an identifier"});
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("fun f(a, {}"), "");
    ParserTestUtils.assertErrors(ParserTestUtils.parse("fun f(,) {}"), new String[] {"Expect an identifier"});
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("fun f(a, b) {}"), "(fun (f a b) (block))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("fun f(a, b) { return a + b; }"), "(fun (f a b) (block (return (+ a b))))");
  }

  @Test
  public void testClass() throws Throwable {
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("class C {}"), "(class (C))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("class C {fun f() {} }"), "(class (C) (fun (f) (block)))");
    ParserTestUtils.assertNoErrorAndResultEquals(ParserTestUtils.parse("class C {fun f() {} fun g(a, b) { f(); } }"), "(class (C) (fun (f) (block)) (fun (g a b) (block (f))))");
  }

  @Test
  public void testRecoverClass() throws Throwable {
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("class C {"), "(class (C))");
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("class C {fun f() {"), "(class (C) (fun (f) (block)))");
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("class C {fun f() }"), "(class (C))");
    ParserTestUtils.assertHasErrorsAndResultEquals(ParserTestUtils.parse("class C {fun f() } fun g() {}"), "(class (C))\n(fun (g) (block))");
  }

  @Test
  public void testInvalidClass() throws Throwable {
    ParserTestUtils.assertErrors(ParserTestUtils.parse("class C {"), new String[] {"EOF reached while parsing class body"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("class C {fun f() {"), new String[] {"EOF reached while parsing block statement", "EOF reached while parsing class body"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("class C {fun f() }"), new String[] {"Expect an opening brace '{'"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("class C {fun f() } fun g() {}"), new String[] {"Expect an opening brace '{'"});
    ParserTestUtils.assertErrors(ParserTestUtils.parse("class C { var a = 3; fun f() } fun g() {}"), new String[] {"Expect method declaration in class body", "Expect an opening brace '{'"});
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

  static void assertHasErrorsAndResultEquals(Pair<List<Stmt>, List<ParserException>> res, String prettyPrintedText) {
    assertNotEquals(res.second.size(), 0);
    assertEquals(ParserTestUtils.prettyPrint(res.first), prettyPrintedText);
  }

  static void assertErrors(Pair<List<Stmt>, List<ParserException>> res, String[] errorMessages) {
    assertEquals(res.second.size(), errorMessages.length);
    for (int i = 0; i < res.second.size(); ++i) {
      assertEquals(res.second.get(i).message, errorMessages[i]);
    }
  }
}
