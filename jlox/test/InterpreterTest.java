package com.lox;

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
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("\"true\";"), "true");
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("false;"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("true;"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("0;"), 0.0);
  }

  @Test
  public void testUnary() throws Throwable {
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("-1;"), -1.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("!1;"), false);

    assertEquals(InterpreterTestUtils.assertLastStmtEquals("-   1;"), -1.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("!   1;"), false);

    assertEquals(InterpreterTestUtils.assertLastStmtEquals("!\"\";"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("! -1;"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("!0;"), false);

    assertEquals(InterpreterTestUtils.assertLastStmtEquals("!!1;"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("--1;"), 1.0);
  }

  @Test
  public void testGrouping() throws Throwable {
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(1);"), 1.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(\"abc\");"), "abc");
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(true);"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(false);"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(1 + 2);"), 3.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(! 2);"), false);
  }

  @Test
  public void testBinary() throws Throwable {
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 + 2;"), 3.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 + (2);"), 3.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 + (2 + 3);"), 6.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 + 2 * 3;"), 7.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 * 2 + 3;"), 5.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("(1 + 2) * 3;"), 9.0);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 - 2 == 3;"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("1 - 2 * 4 == -7;"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("2 / 1 == 2;"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("2 / 1 >= 1;"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("2 / 1 > 1;"), true);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("2 / 1 <= 1;"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("2 / 1 < 1;"), false);
    assertEquals(InterpreterTestUtils.assertLastStmtEquals("2 / 1 != 1;"), true);
  }
}

class InterpreterTestUtils {
  static Object assertLastStmtEquals(String source) throws Throwable {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.tokenize().first;
    Parser parser = new Parser(tokens);
    List<Stmt> stmts = parser.parse().first; 

    Interpreter interpreter = new Interpreter();
    LoxObject res = new LoxNil();
    for (Stmt stmt: stmts) {
      res = interpreter.evaluateStmt(stmt);
    }
    return res.value();
  }
}
