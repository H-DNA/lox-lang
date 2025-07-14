#include "../lib/scanner.h"
#include <CUnit/CUnit.h>

static void test_punctuation() {
  Scanner scanner;
  initScanner(&scanner, ".,{}()");
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
  CU_ASSERT_TRUE(token.type == TOKEN_EOF);
  CU_ASSERT_TRUE(token.line == 0);
  CU_ASSERT_TRUE(token.start == 6);
  CU_ASSERT_TRUE(token.end == 6);
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
}
