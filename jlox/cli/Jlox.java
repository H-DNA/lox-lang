import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.lox.Environment;
import com.lox.Interpreter;
import com.lox.InterpreterException;
import com.lox.Parser;
import com.lox.ParserException;
import com.lox.Scanner;
import com.lox.ScannerException;
import com.lox.ast.Stmt;
import com.lox.ast.Token;
import com.lox.object.LoxNil;
import com.lox.object.LoxObject;
import com.lox.utils.Pair;

public class Jlox {
  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(2);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  public static void runFile(String path) throws IOException {
    Interpreter interpreter;
    try {
      interpreter = new Interpreter();
    } catch (Exception e) {
      throw new Error("Failed to construct interpreter");
    }
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(interpreter, new Environment(Interpreter.globals), new String(bytes, Charset.defaultCharset()));
  }

  public static void runPrompt() throws IOException {
    Interpreter interpreter;
    Environment env = new Environment(Interpreter.globals);
    try {
      interpreter = new Interpreter();
    } catch (Exception e) {
      throw new Error("Failed to construct interpreter");
    }

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    while (true) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      final LoxObject res = run(interpreter, env, line);
      if (!(res instanceof LoxNil)) {
        System.out.println(res.toString());
      }
    }
  }

  private static LoxObject run(Interpreter interpreter, Environment env, String source) {
    final Scanner scanner = new Scanner(source);
    final Pair<List<Token>, List<ScannerException>> scannerRes = scanner.tokenize();
    if (scannerRes.second.size() > 0) {
      for (ScannerException e: scannerRes.second) {
        reportError(e, source);
      }
      return LoxNil.NIL;
    }

    final Parser parser = new Parser(scannerRes.first);
    final Pair<List<Stmt>, List<ParserException>> parserRes = parser.parse();
    if (parserRes.second.size() > 0) {
      for (ParserException e: parserRes.second) {
        reportError(e, source);
      }
      return LoxNil.NIL;
    }

    LoxObject res = LoxNil.NIL;

    try {
      for (Stmt stmt: parserRes.first) {
        res = interpreter.evaluateStmt(stmt, env);
      }
      return res;
    } catch (InterpreterException e) {
      reportError(e, source);
      return LoxNil.NIL;
    }
  }

  private static void reportError(ScannerException e, String source) {
    final Pair<Long, Long> startPos = getLineAndCol(e.startOffset, source);
    final Pair<Long, Long> endPos = getLineAndCol(e.endOffset, source);
    System.err.println(String.format("[Error] (line %s column %s) %s", startPos.first, startPos.second, e.message));
  }

  private static void reportError(ParserException e, String source) {
    final Pair<Long, Long> startPos = getLineAndCol(e.startOffset, source);
    final Pair<Long, Long> endPos = getLineAndCol(e.endOffset, source);
    System.err.println(String.format("[Error] (line %s column %s) %s", startPos.first, startPos.second, e.message));
  }

  private static void reportError(InterpreterException e, String source) {
    System.err.println(e.message);
  }

  private static Pair<Long, Long> getLineAndCol(int offset, String source) {
    final String beforeSource = source.substring(0, offset);
    final long line = beforeSource.chars()
                                  .filter(ch -> ch == '\n')
                                  .count() + 1;
    final long col = beforeSource.length() - beforeSource.lastIndexOf('\n');
    return new Pair<>(line, col);
  }
}
