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
import com.lox.object.LoxNil;
import com.lox.object.LoxObject;
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
  }

  @Test
  public void testGrouping() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("(1);", 1.0);
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
  public void testVarDecl() throws Throwable {
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; x;", 3.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 3; var x = 4; x;", 4.0);
    InterpreterTestUtils.assertLastStmtEquals("var x = 1 + 2; x;", 3.0);
    InterpreterTestUtils.assertErrorMessageIs("var x = y; x;", "Undefined variable 'y'");
    InterpreterTestUtils.assertErrorMessageIs("var x = y + 1; x;", "Undefined variable 'y'");
    InterpreterTestUtils.assertErrorMessageIs("var x = x; x;", "Undefined variable 'x'");
    InterpreterTestUtils.assertLastStmtEquals("var y = 1 + 2; var x = y * 2; x;", 6.0);
  }

  @Test
  public void testPrintStmt() throws Throwable {
    InterpreterTestUtils.assertStdoutIs("var x = 3; print x;", "3.0\n");
    InterpreterTestUtils.assertStdoutIs("var x = \"3.02\"; print x;", "3.02\n");
    InterpreterTestUtils.assertStdoutIs("print 1 + 2 + 3;", "6.0\n");
    InterpreterTestUtils.assertStdoutIs("var x = 10; var y = x * 2; print y + 1 + 2 + 3;", "26.0\n");
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
  public void testTypeMismatch() throws Throwable {
    InterpreterTestUtils.assertErrorMessageIs("1 + \"3\"", "Unsupported operator '+' on number and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" + 3", "Unsupported operator '+' on string and number");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" + \"3\"", "Unsupported operator '+' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" * \"3\"", "Unsupported operator '*' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" - \"3\"", "Unsupported operator '-' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" / \"3\"", "Unsupported operator '/' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" > \"3\"", "Unsupported operator '>' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" >= \"3\"", "Unsupported operator '>=' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" <= \"3\"", "Unsupported operator '<=' on string and string");
    InterpreterTestUtils.assertErrorMessageIs("\"1\" < \"3\"", "Unsupported operator '<' on string and string");
  }
}

class InterpreterTestUtils {
  static void assertLastStmtEquals(String source, Object target) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    List<Stmt> stmts = parser.parse().first; 

    Interpreter interpreter = new Interpreter();
    LoxObject res = new LoxNil();
    for (Stmt stmt: stmts) {
      res = interpreter.evaluateStmt(stmt);
    }
    assertEquals(res.value(), target);
  }

  static void assertErrorMessageIs(String source, String target) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    List<Stmt> stmts = parser.parse().first; 

    Interpreter interpreter = new Interpreter();
    LoxObject res = new LoxNil();
    
    try {
      for (Stmt stmt: stmts) {
        res = interpreter.evaluateStmt(stmt);
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
