package com.lox;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
import com.lox.object.LoxBoolean;
import com.lox.object.LoxNil;
import com.lox.object.LoxNumber;
import com.lox.object.LoxObject;
import com.lox.object.LoxString;
import com.lox.utils.Pair;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class InterpreterTest {
  @Test
  public void testLiteral() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("\"true\";", "true");
    InterpreterTestUtils.assertLastStmtEquals("false;", false);
    InterpreterTestUtils.assertLastStmtEquals("true;", true);
    InterpreterTestUtils.assertLastStmtEquals("0;", 0.0);
  }

  @Test
  public void testUnary() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("-1;", -1.0);
    InterpreterTestUtils.assertLastStmtEquals("!1;", false);

    InterpreterTestUtils.assertLastStmtEquals("-   1;", -1.0);
    InterpreterTestUtils.assertLastStmtEquals("!   1;", false);

    InterpreterTestUtils.assertLastStmtEquals("!\"\";", false);
    InterpreterTestUtils.assertLastStmtEquals("! -1;", false);
    InterpreterTestUtils.assertLastStmtEquals("!0;", false);

    InterpreterTestUtils.assertLastStmtEquals("!!1;", true);
    InterpreterTestUtils.assertLastStmtEquals("--1;", 1.0);

    InterpreterTestUtils.assertLastStmtEquals("!   nil;", true);
    InterpreterTestUtils.assertLastStmtEquals("nil;", null);

    InterpreterTestUtils.assertLastStmtEquals("!!0;", true);
    InterpreterTestUtils.assertLastStmtEquals("!!1;", true);
    InterpreterTestUtils.assertLastStmtEquals("!!\"a\";", true);
    InterpreterTestUtils.assertLastStmtEquals("!!\"\";", true);
    InterpreterTestUtils.assertLastStmtEquals("!0;", false);
    InterpreterTestUtils.assertLastStmtEquals("!1;", false);
    InterpreterTestUtils.assertLastStmtEquals("!\"a\";", false);
    InterpreterTestUtils.assertLastStmtEquals("!\"\";", false);
  }

  @Test
  public void testGrouping() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("(1);", 1.0);
    InterpreterTestUtils.assertLastStmtEquals("(nil);", null);
    InterpreterTestUtils.assertLastStmtEquals("!(nil);", true);
    InterpreterTestUtils.assertLastStmtEquals("(\"abc\");", "abc");
    InterpreterTestUtils.assertLastStmtEquals("(true);", true);
    InterpreterTestUtils.assertLastStmtEquals("(false);", false);
    InterpreterTestUtils.assertLastStmtEquals("(1 + 2);", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("(! 2);", false);
  }

  @Test
  public void testBinary() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("1 + 2;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("1 + (2);", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("1 + (2 + 3);", 6.0);
    InterpreterTestUtils.assertLastStmtEquals("1 + 2 * 3;", 7.0);
    InterpreterTestUtils.assertLastStmtEquals("1 * 2 + 3;", 5.0);
    InterpreterTestUtils.assertLastStmtEquals("(1 + 2) * 3;", 9.0);
    InterpreterTestUtils.assertLastStmtEquals("1 - 2 == 3;", false);
    InterpreterTestUtils.assertLastStmtEquals("1 - 2 * 4 == -7;", true);
    InterpreterTestUtils.assertLastStmtEquals("2 / 1 == 2;", true);
    InterpreterTestUtils.assertLastStmtEquals("2 / 1 >= 1;", true);
    InterpreterTestUtils.assertLastStmtEquals("2 / 1 > 1;", true);
    InterpreterTestUtils.assertLastStmtEquals("2 / 1 <= 1;", false);
    InterpreterTestUtils.assertLastStmtEquals("2 / 1 < 1;", false);
    InterpreterTestUtils.assertLastStmtEquals("2 / 1 != 1;", true);
  }

  @Test
  public void testLogical() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("true and 3;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("true and nil;", null);
    InterpreterTestUtils.assertLastStmtEquals("true and \"abc\";", "abc");
    InterpreterTestUtils.assertLastStmtEquals("true and false;", false);

    InterpreterTestUtils.assertLastStmtEquals("nil and 3;", null);
    InterpreterTestUtils.assertLastStmtEquals("false and \"abc\";", false);
    InterpreterTestUtils.assertLastStmtEquals("false and 3;", false);

    InterpreterTestUtils.assertLastStmtEquals("nil or 3;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("false or \"abc\";", "abc");
    InterpreterTestUtils.assertLastStmtEquals("false or 3;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("false or false;", false);
    InterpreterTestUtils.assertLastStmtEquals("false or nil;", null);

    InterpreterTestUtils.assertLastStmtEquals("3.0 or nil;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("\"abc\" or nil;", "abc");
    InterpreterTestUtils.assertLastStmtEquals("true or nil;", true);
    InterpreterTestUtils.assertLastStmtEquals("true or false;", true);
  }

  @Test
  public void testVarDecl() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("var x; x;", null);
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; x;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 1 + 2; x;", 3.0);
    InterpreterTestUtils.assertErrorMessageIs("var x = y; x;", "Undefined variable 'y'");
    InterpreterTestUtils.assertErrorMessageIs("var x = y + 1; x;", "Undefined variable 'y'");
    InterpreterTestUtils.assertErrorMessageIs("var x = x; x;", "Undefined variable 'x'");
    InterpreterTestUtils.assertErrorMessageIs("var x = 1; var x = 3; x;", "Redeclared variable 'x'");
    InterpreterTestUtils.assertLastStmtEquals("var y = 1 + 2; var x = y * 2; x;", 6.0);
  }

  @Test
  public void testAssignment() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; x = 4;", 4.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; var y = 5; x = y = 10;", 10.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; var y = 5; x = y = 10; x;", 10.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; var y = 5; x = y = 10; y;", 10.0);
  }

  @Test
  public void testPrintStmt() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("var x = 3; print x;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("var x = \"3.02\"; print x;", "\"3.02\"\n");
    InterpreterTestUtils.assertStdoutIs("print 1 + 2 + 3;", "6.0\n");
    InterpreterTestUtils.assertStdoutIs("var x = 10; var y = x * 2; print y + 1 + 2 + 3;", "26.0\n");
    InterpreterTestUtils.assertStdoutIs("var x = 3; print x or false;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("var x = 3; print x and false;", "false\n");
  }

  @Test
  public void testIfStmt() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("var x = 10; if (x) x + 1; else x - 1;", 11.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 0; if (x) x + 1; else x - 1;", 1.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = false; var y = 1; if (x) y + 1; else y - 1;", 0.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = nil; var y = 1; if (x) y + 1; else y - 1;", 0.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = nil; var y = 1; if (x) y + 1; else y - 1;", 0.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = nil; var y = 1; if (!x) if (x) y + 1; else y + 2; else y - 1;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = nil; var y = 1; if (!x) if (x) y + 1; else y + 2; else y - 1;", 3.0);
  }

  @Test
  public void testBlockStmt() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("{ var c = 3; }", null);
    InterpreterTestUtils.assertLastStmtEquals("{ var c = 3; c + 1 }", 4.0);
    InterpreterTestUtils.assertLastStmtEquals("{ var c = 4.0; if (c) c + 1; }", 5.0);
    InterpreterTestUtils.assertLastStmtEquals("{ var c = 4.0; if (!c) c + 1; }", null);
    InterpreterTestUtils.assertLastStmtEquals("{ var c = 4.0; if (!c) c + 1; else c * 0 }", 0.0);
    InterpreterTestUtils.assertLastStmtEquals("var c = nil; if (c) { 4.0; } else { 5.0 }", 5.0);
    InterpreterTestUtils.assertLastStmtEquals("var c = true; if (c) { 4.0; } else { 5.0 }", 4.0);
  }

  @Test
  public void testWhileStmt() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("var a = 0; var sum = 0; while (a < 10) { sum = sum + a; a = a + 1; } sum;", 45.0);
    InterpreterTestUtils.assertLastStmtEquals("var a = 1; while (a < 10) a = a * 2; a;", 16.0);
  }
  
  @Test
  public void testForStmt() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("var sum = 0; for (var a = 0; a < 10; a = a + 1) { sum = sum + a; } sum;", 45.0);
    InterpreterTestUtils.assertLastStmtEquals("var sum = 0; var a; for (a = 0; a < 10; a = a + 1) { sum = sum + a; } sum;", 45.0);
    InterpreterTestUtils.assertLastStmtEquals("var a; for (a = 1; a < 10; a = a * 2) {} a;", 16.0);
  }
  
  @Test
  public void testLexicalScoping() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("var a = 3; { print a; }", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = 4; print a; }", "4.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = 4; } print a;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = 4; { var a = 5; print a; } }", "5.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = 4; { var a = 5; } print a; }", "4.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = 4; { var a = 5; } } print a;", "3.0\n");

    InterpreterTestUtils.assertErrorMessageIs("var a = 3; { var b = 4; { var c = 5; } } print c;", "Undefined variable 'c'");
    InterpreterTestUtils.assertErrorMessageIs("var a = 3; { var b = 4; { var c = 5; } print c; }", "Undefined variable 'c'");
    InterpreterTestUtils.assertErrorMessageIs("var a = 3; { var b = 4; { var c = 5; } } print b;", "Undefined variable 'b'");

    InterpreterTestUtils.assertStdoutIs("var a = 3; { a = 4; print a; }", "4.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { a = a + 1; } print a;", "4.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var b = 3; { a = a + b; } } print a;", "6.0\n");

    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = a + 1; } print a;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("var a = 3; { var a = a + 1; print a; }", "4.0\n");
    InterpreterTestUtils.assertStdoutIs("var outer; { var a = 3; fun inner() { return a; } outer = inner; } print outer();", "3.0\n");
    InterpreterTestUtils.assertErrorMessageIs("var outer; { fun inner() { return a; } outer = inner; } { var a = 3; print outer(); }", "Undefined variable 'a'");
  }

  @Test
  public void testTypeMismatch() throws Throwable {
    InterpreterTestUtils.assertErrorMessageIs("1 + \"3\"", "Unsupported operator '+' on Number and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" + 3", "Unsupported operator '+' on String and Number");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" + \"3\"", "Unsupported operator '+' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" * \"3\"", "Unsupported operator '*' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" - \"3\"", "Unsupported operator '-' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" / \"3\"", "Unsupported operator '/' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" > \"3\"", "Unsupported operator '>' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" >= \"3\"", "Unsupported operator '>=' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" <= \"3\"", "Unsupported operator '<=' on String and String");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" < \"3\"", "Unsupported operator '<' on String and String");
  }

  @Test
  public void testCallable() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("toString(1);", "1.0");
    InterpreterTestUtils.assertLastStmtEquals("toString(true);", "true");
    InterpreterTestUtils.assertLastStmtEquals("toString(false);", "false");
    InterpreterTestUtils.assertLastStmtEquals("toString(nil);", "nil");
    InterpreterTestUtils.assertLastStmtEquals("toString(clock);", "<native function 'clock'>");
    InterpreterTestUtils.assertErrorMessageIs("toString();", "Expected 1 argument(s) but got 0");
  }

  @Test
  public void testFuncStmt() throws Throwable {
    InterpreterTestUtils.assertErrorMessageIs("fun func() { return 3; } func(3);", "Expected 0 argument(s) but got 1");
    InterpreterTestUtils.assertLastStmtEquals("fun func() { return 3; } func();", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("fun func() { 3; } func();", null);
    InterpreterTestUtils.assertLastStmtEquals("fun func(i) { if (i == 0) return 0; else return func(i - 1) + 1; } func(10);", 10.0);

    InterpreterTestUtils.assertErrorMessageIs("return 10;", "Cannot `return` outside a function body");
    InterpreterTestUtils.assertErrorMessageIs("{ return 10; }", "Cannot `return` outside a function body");
    InterpreterTestUtils.assertErrorMessageIs("while (true) { return 10; }", "Cannot `return` outside a function body");
    InterpreterTestUtils.assertLastStmtEquals("while (false) { return 10; }", null);

    InterpreterTestUtils.assertLastStmtEquals("fun func(i) { if (i == 0) { return 0; } else { return func(i - 1) + 1; } } func(10);", 10.0);
    InterpreterTestUtils.assertLastStmtEquals("fun func(i) { if (i == 0) { return 0; } else return func(i - 1) + 1; } func(10);", 10.0);
    InterpreterTestUtils.assertLastStmtEquals("fun func(i) { if (i == 0) { return 0; } else return func(i - 1) + 1; } func(10);", 10.0);
  }

  @Test
  public void testClsStmt() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C {} print C;", "<class C>\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f() {} fun g() {}} print C;", "<class C>\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f() {} fun g() {}} print C();", "<instance C>\n");
  }

  @Test
  public void testGetExpr() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("print (3).a;", "nil\n");
    InterpreterTestUtils.assertStdoutIs("class C { } print C().a;", "nil\n");
    InterpreterTestUtils.assertStdoutIs("class C { } print C().a;", "nil\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f() {} } print C().a;", "nil\n");
  }

  @Test
  public void testMethod() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C { fun f() {} } print C().f;", "<bound function f>\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f() {} } print C().f();", "nil\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f(a) { this.a = a; } fun g() { return this.a; } } C().f(10); print C().g();", "nil\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f(a) { this.a = a; } fun g() { this.f(4); return this.a; } } var c = C(); c.f(10); print c.g();", "4.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f(a) { this.a = a; } fun g() { this.f(this.a + 1); return this.a; } } var c = C(); c.f(10); print c.g();", "11.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f(a) { this.a = a; } fun g() { return this.a; } } var c = C(); c.f(10); print c.g();", "10.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun f() { return this; } } print C().f();", "<instance C>\n");
    InterpreterTestUtils.assertStdoutIs("fun g() { return 3; } class C { fun g() { return g(); } } var c = C(); print c.g();", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun g() { fun g() { return 3; } return g(); } } var c = C(); print c.g();", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun g(a) { this.a = a; } } var c = C(); c.g(1); print c.a; c.g(2); print c.a;", "1.0\n2.0\n");
    InterpreterTestUtils.assertErrorMessageIs("fun g() { return this; } class C { fun g() { return g(); } } var c = C(); print c.g();", "Cannot access `this` inside an unbound function");
  }

  @Test
  public void testExtractMethod() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C { fun g(a) { this.a = a; } } var c = C(); var g = c.g; g(1); print c.a; g(2); print c.a;", "1.0\n2.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun g(a) { this.a = a; } } var c = C(); var g = c.g; print g == c.g;", "false\n");
    InterpreterTestUtils.assertStdoutIs("fun f() { print \"f\"; } class C {} var c = C(); c.f = f; c.f();", "\"f\"\n");
    InterpreterTestUtils.assertErrorMessageIs("fun f() { print this; } class C {} var c = C(); c.f = f; c.f();", "Cannot access `this` inside an unbound function");
  }

  @Test
  public void testSuperclass() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C { fun g(a) { this.a = a; } } class D < C {} var d = D(); var g = d.g; g(1); print d.a; g(2); print d.a;", "1.0\n2.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun g() { print this.a; } } class D < C {} var d = D(); d.a = 3; d.g();", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun g() { print this.f(); } } class D < C { fun f() { return 4.0; } } var d = D(); d.g();", "4.0\n");
  }
  
  @Test
  public void testSetExpr() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C { } print C().a = 3;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { } print C().a = 3; print C().a", "3.0\nnil\n");
    InterpreterTestUtils.assertStdoutIs("class C { } var c = C(); c.a = 3; print c.a;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { } var c = C(); c.a = 3; print c.a = 4.0; print c.a;", "4.0\n4.0\n");

    InterpreterTestUtils.assertErrorMessageIs("(3).a = 3;", "Number is immutable");
    InterpreterTestUtils.assertErrorMessageIs("var a = 3; a.a = 3;", "Number is immutable");
    InterpreterTestUtils.assertErrorMessageIs("var a = 3 + 1; a.a = 3;", "Number is immutable");
    InterpreterTestUtils.assertErrorMessageIs("var a = 3 + 1; (a + 1).a = 3;", "Number is immutable");

    InterpreterTestUtils.assertErrorMessageIs("nil.a = 3;", "Nil is immutable");
    InterpreterTestUtils.assertErrorMessageIs("true.a = 3;", "Boolean is immutable");
    InterpreterTestUtils.assertErrorMessageIs("false.a = 3;", "Boolean is immutable");
    InterpreterTestUtils.assertErrorMessageIs("\"abcd\".a = 3;", "String is immutable");
  }

  @Test
  public void testConstructor() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { this.a = 3; } } var c = C(); print c.a;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { } } var c = C(); print c.a;", "nil\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor(a) { this.a = a; } } var c = C(\"abc\"); print c.a;", "\"abc\"\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor(a, b) { this.sum = a + b; } } var c = C(1, 2); print c.sum;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor(a, b) { this.a = a; this.b = b; this.sum = this.sum(); } fun sum() { return this.a + this.b; } } var c = C(1, 2); print c.a; print c.b; print c.sum;", "1.0\n2.0\n3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor(a, b) { this.a = a; return b; this.b = b; } } var c = C(1, 2); print c.a; print c.b;", "1.0\nnil\n");
  }

  @Test
  public void testSuperConstructor() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { this.a = 3; } } class D < C { fun a() { return 10; } } var d = D(); print d.a;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { this.a = 3; } } class D < C { fun b() { return this.a; } } var d = D(); print d.b();", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { this.b(); } } class D < C { fun constructor() { super(); this.a = 3; } fun b() { this.d = 10; } } var d = D(); print d.a; print d.d", "3.0\n10.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { this.c = 3; } } class D < C { fun constructor() { super(); this.d = 10; } } var d = D(); print d.c; print d.d;", "3.0\n10.0\n");
    InterpreterTestUtils.assertStdoutIs("class C { fun constructor() { this.c = 3; } } class C2 < C {} class D < C2 { fun constructor() { super(); this.d = 10; } } var d = D(); print d.c; print d.d;", "3.0\n10.0\n");
    InterpreterTestUtils.assertStdoutIs("class B { fun constructor() { this.b = 4; } } class C < B { fun constructor() { super(); this.c = 3; } } class D < C { fun constructor() { super(); this.d = 10; } } var d = D(); print d.b; print d.c; print d.d;", "4.0\n3.0\n10.0\n");
  }

  @Test
  public void testSuperMethod() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("class B { fun p() { print \"b\"; } } class C < B { fun p() { super.p(); print \"c\"; }} var c = C(); c.p();", "\"b\"\n\"c\"\n");
    InterpreterTestUtils.assertStdoutIs("class B { fun p() { print \"b\"; } } class C < B { fun p() { super.p(); print \"c\"; }} class D < C { fun p() { super.p(); print \"d\"; }} var d = D(); d.p();", "\"b\"\n\"c\"\n\"d\"\n");
    InterpreterTestUtils.assertErrorMessageIs("class B { fun p() { super.p(); }} B().p();", "Callee is not of Callable type");
  }
}

class InterpreterTestUtils {
  static Object rawValueOf(LoxObject obj) {
    if (obj instanceof LoxNumber) {
      return ((LoxNumber)obj).value;
    }
    if (obj instanceof LoxString) {
      return ((LoxString)obj).value;
    }
    if (obj instanceof LoxNil) {
      return null;
    }
    if (obj instanceof LoxBoolean) {
      return ((LoxBoolean)obj).value;
    }
    return obj;
  }

  static void assertLastStmtEquals(String source, Object target) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    List<Stmt> stmts = parser.parse().first; 

    Interpreter interpreter = new Interpreter();
    Environment env = new Environment();
    LoxObject res = LoxNil.NIL;
    for (Stmt stmt: stmts) {
      res = interpreter.evaluateStmt(stmt, env);
    }
    assertEquals(rawValueOf(res), target);
  }

  static void assertErrorMessageIs(String source, String target) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    List<Stmt> stmts = parser.parse().first; 

    Interpreter interpreter = new Interpreter();
    Environment env = new Environment();
    LoxObject res = LoxNil.NIL;
    
    try {
      for (Stmt stmt: stmts) {
        res = interpreter.evaluateStmt(stmt, env);
      }
    } catch (InterpreterException e) {
      assertEquals(e.message, target);
      return;
    }
    assertEquals("An exception was caught", "No exception was caught");
  }

  static void assertStdoutIs(String source, String target) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    List<Stmt> stmts = parser.parse().first; 
    
    PrintStream originalStream = System.out;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    Interpreter interpreter = new Interpreter();
    interpreter.evaluate(stmts);
    System.setOut(originalStream);

    assertEquals(out.toString(), target);
  }
}
