#include "../lib/scanner.h"
#include <CUnit/CUnit.h>

static void test_keywords() {
  Scanner scanner;
  initScanner(&scanner, "true false nil");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_TRUE);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 0);
  CU_ASSERT_TRUE(token.end == 4);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_FALSE);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 5);
  CU_ASSERT_TRUE(token.end == 10);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_NIL);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 11);
  CU_ASSERT_TRUE(token.end == 14);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 14);
  CU_ASSERT_TRUE(token.end == 14);
}


static void test_identifier() {
  Scanner scanner;
  initScanner(&scanner, "a_ _b _ abc");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_IDENTIFIER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 0);
  CU_ASSERT_TRUE(token.end == 2);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_IDENTIFIER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 3);
  CU_ASSERT_TRUE(token.end == 5);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_IDENTIFIER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 6);
  CU_ASSERT_TRUE(token.end == 7);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_IDENTIFIER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 8);
  CU_ASSERT_TRUE(token.end == 11);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 11);
  CU_ASSERT_TRUE(token.end == 11);
}

static void test_newline() {
  Scanner scanner;
  initScanner(&scanner, "\na\n\nb\n");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_IDENTIFIER);
  CU_ASSERT_TRUE(token.line == 1);
  CU_ASSERT_TRUE(token.start == 1);
  CU_ASSERT_TRUE(token.end == 2);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_IDENTIFIER);
  CU_ASSERT_TRUE(token.line == 3);
  CU_ASSERT_TRUE(token.start == 4);
  CU_ASSERT_TRUE(token.end == 5);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 4);
  CU_ASSERT_TRUE(token.start == 6);
  CU_ASSERT_TRUE(token.end == 6);
}

static void test_number() {
  Scanner scanner;
  initScanner(&scanner, "-1.2 .3");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_MINUS);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 0);
  CU_ASSERT_TRUE(token.end == 1);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_NUMBER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 1);
  CU_ASSERT_TRUE(token.end == 4);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_DOT);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 5);
  CU_ASSERT_TRUE(token.end == 6);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_NUMBER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 6);
  CU_ASSERT_TRUE(token.end == 7);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 7);
  CU_ASSERT_TRUE(token.end == 7);
}

static void test_string() {
  Scanner scanner;
  initScanner(&scanner, "\"this is a string\"\"abc");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_STRING);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 0);
  CU_ASSERT_TRUE(token.end == 18);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_UNCLOSED_STRING);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 18);
  CU_ASSERT_TRUE(token.end == 22);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 22);
  CU_ASSERT_TRUE(token.end == 22);
}

static void test_operator() {
  Scanner scanner;
  initScanner(&scanner, "+-*/!<> ==!=>=<==");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_PLUS);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 0);
  CU_ASSERT_TRUE(token.end == 1);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_MINUS);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 1);
  CU_ASSERT_TRUE(token.end == 2);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_STAR);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 2);
  CU_ASSERT_TRUE(token.end == 3);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_SLASH);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 3);
  CU_ASSERT_TRUE(token.end == 4);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_BANG);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 4);
  CU_ASSERT_TRUE(token.end == 5);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_LESS);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 5);
  CU_ASSERT_TRUE(token.end == 6);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_GREATER);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 6);
  CU_ASSERT_TRUE(token.end == 7);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EQUAL_EQUAL);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 8);
  CU_ASSERT_TRUE(token.end == 10);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_BANG_EQUAL);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 10);
  CU_ASSERT_TRUE(token.end == 12);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_GREATER_EQUAL);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 12);
  CU_ASSERT_TRUE(token.end == 14);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_LESS_EQUAL);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 14);
  CU_ASSERT_TRUE(token.end == 16);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EQUAL);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 16);
  CU_ASSERT_TRUE(token.end == 17);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 17);
  CU_ASSERT_TRUE(token.end == 17);
}

static void test_punctuation() {
  Scanner scanner;
  initScanner(&scanner, ".,{}();");
  Token token;

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_DOT);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 0);
  CU_ASSERT_TRUE(token.end == 1);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_COMMA);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 1);
  CU_ASSERT_TRUE(token.end == 2);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_LEFT_BRACE);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 2);
  CU_ASSERT_TRUE(token.end == 3);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_RIGHT_BRACE);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 3);
  CU_ASSERT_TRUE(token.end == 4);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_LEFT_PAREN);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 4);
  CU_ASSERT_TRUE(token.end == 5);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_RIGHT_PAREN);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 5);
  CU_ASSERT_TRUE(token.end == 6);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_SEMICOLON);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 6);
  CU_ASSERT_TRUE(token.end == 7);

  token = scanToken(&scanner);
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 7);
  CU_ASSERT_TRUE(token.end == 7);
}

static void run_scanner_suite() {
  CU_pSuite suite = CU_add_suite("Scanner suite", NULL, NULL);
  CU_add_test(suite, "Lex punctuation", test_punctuation);
  CU_add_test(suite, "Lex operators", test_operator);
  CU_add_test(suite, "Lex strings", test_string);
  CU_add_test(suite, "Lex numbers", test_number);
  CU_add_test(suite, "Lex identifiers", test_identifier);
  CU_add_test(suite, "Lex newlines", test_newline);
  CU_add_test(suite, "Lex keywords", test_keywords);
}
