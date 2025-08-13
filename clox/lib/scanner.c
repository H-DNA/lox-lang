#include "scanner.h"
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

void initScanner(Scanner *scanner, const char *source) {
  scanner->source = source;
  scanner->current = 0;
  scanner->line = 0;
}

static Token makeToken(TokenType type, unsigned int start, unsigned int end,
                       unsigned int line) {
  Token token;
  token.type = type;
  token.start = start;
  token.end = end;
  token.line = line;
  return token;
}

static bool isAtEnd(Scanner *scanner) {
  int start = scanner->current;

  return scanner->source[start] == '\0';
}

static char peek(Scanner *scanner) { return scanner->source[scanner->current]; }

static char peekNext(Scanner *scanner) {
  return scanner->source[scanner->current + 1];
}

static char advance(Scanner *scanner) {
  char c = peek(scanner);
  scanner->current++;
  if (c == '\n')
    scanner->line++;
  return c;
}

static bool match(Scanner *scanner, char expected) {
  if (isAtEnd(scanner))
    return false;
  if (peek(scanner) != expected)
    return false;
  advance(scanner);
  return true;
}

static void skipWhitespace(Scanner *scanner) {
  for (;;) {
    char c = peek(scanner);
    switch (c) {
    case ' ':
    case '\r':
    case '\t':
    case '\n':
      advance(scanner);
      break;
    case '/':
      if (peekNext(scanner) == '/') {
        while (peek(scanner) != '\n' && !isAtEnd(scanner))
          advance(scanner);
      } else {
        return;
      }
      break;
    default:
      return;
    }
  }
}

Token string(Scanner *scanner) {
  int start = scanner->current;
  int line = scanner->line;

  advance(scanner);
  while (peek(scanner) != '"' && !isAtEnd(scanner)) {
    advance(scanner);
  }

  if (isAtEnd(scanner))
    return makeToken(TOKEN_UNCLOSED_STRING, start, scanner->current, line);

  advance(scanner);
  return makeToken(TOKEN_STRING, start, scanner->current, line);
}

static bool isDigit(char c) { return c >= '0' && c <= '9'; }

static Token number(Scanner *scanner) {
  int start = scanner->current;
  advance(scanner);
  while (isDigit(peek(scanner)))
    advance(scanner);

  if (peek(scanner) == '.' && isDigit(peekNext(scanner))) {
    advance(scanner);

    while (isDigit(peek(scanner)))
      advance(scanner);
  }

  return makeToken(TOKEN_NUMBER, start, scanner->current, scanner->line);
}

static bool isAlpha(char c) {
  return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
}

static TokenType identifierType(Scanner *scanner, int start, int end) {
  int length = end - start;
  if (length == 4 && strncmp(scanner->source + start, "true", length) == 0) {
    return TOKEN_TRUE;
  } else if (length == 5 &&
             strncmp(scanner->source + start, "false", length) == 0) {
    return TOKEN_FALSE;
  } else if (length == 3 &&
             strncmp(scanner->source + start, "nil", length) == 0) {
    return TOKEN_NIL;
  } else if (length == 3 &&
             strncmp(scanner->source + start, "and", length) == 0) {
    return TOKEN_AND;
  } else if (length == 2 &&
             strncmp(scanner->source + start, "or", length) == 0) {
    return TOKEN_OR;
  } else if (length == 5 &&
             strncmp(scanner->source + start, "print", length) == 0) {
    return TOKEN_PRINT;
  } else {
    return TOKEN_IDENTIFIER;
  }
}

static Token identifier(Scanner *scanner) {
  int start = scanner->current;
  advance(scanner);
  while (isAlpha(peek(scanner)) || isDigit(peek(scanner)))
    advance(scanner);
  return makeToken(identifierType(scanner, start, scanner->current), start,
                   scanner->current, scanner->line);
}

Token scanToken(Scanner *scanner) {
  skipWhitespace(scanner);
  int start = scanner->current;

  if (isAtEnd(scanner))
    return makeToken(TOKEN_EOF, start, scanner->current, scanner->line);

  char c = peek(scanner);

  if (isDigit(c))
    return number(scanner);
  if (isAlpha(c))
    return identifier(scanner);

  switch (c) {
  case '(':
    advance(scanner);
    return makeToken(TOKEN_LEFT_PAREN, start, scanner->current, scanner->line);
  case ')':
    advance(scanner);
    return makeToken(TOKEN_RIGHT_PAREN, start, scanner->current, scanner->line);
  case '{':
    advance(scanner);
    return makeToken(TOKEN_LEFT_BRACE, start, scanner->current, scanner->line);
  case '}':
    advance(scanner);
    return makeToken(TOKEN_RIGHT_BRACE, start, scanner->current, scanner->line);
  case ';':
    advance(scanner);
    return makeToken(TOKEN_SEMICOLON, start, scanner->current, scanner->line);
  case ',':
    advance(scanner);
    return makeToken(TOKEN_COMMA, start, scanner->current, scanner->line);
  case '.':
    advance(scanner);
    return makeToken(TOKEN_DOT, start, scanner->current, scanner->line);
  case '-':
    advance(scanner);
    return makeToken(TOKEN_MINUS, start, scanner->current, scanner->line);
  case '+':
    advance(scanner);
    return makeToken(TOKEN_PLUS, start, scanner->current, scanner->line);
  case '/':
    advance(scanner);
    return makeToken(TOKEN_SLASH, start, scanner->current, scanner->line);
  case '*':
    advance(scanner);
    return makeToken(TOKEN_STAR, start, scanner->current, scanner->line);
  case '!': {
    advance(scanner);
    const TokenType type = match(scanner, '=') ? TOKEN_BANG_EQUAL : TOKEN_BANG;
    return makeToken(type, start, scanner->current, scanner->line);
  }
  case '=': {
    advance(scanner);
    const TokenType type =
        match(scanner, '=') ? TOKEN_EQUAL_EQUAL : TOKEN_EQUAL;
    return makeToken(type, start, scanner->current, scanner->line);
  }
  case '<': {
    advance(scanner);
    const TokenType type = match(scanner, '=') ? TOKEN_LESS_EQUAL : TOKEN_LESS;
    return makeToken(type, start, scanner->current, scanner->line);
  }
  case '>': {
    advance(scanner);
    const TokenType type =
        match(scanner, '=') ? TOKEN_GREATER_EQUAL : TOKEN_GREATER;
    return makeToken(type, start, scanner->current, scanner->line);
  }
  case '"':
    return string(scanner);
  }

  advance(scanner);
  return makeToken(TOKEN_INVALID, start, scanner->current, scanner->line);
}
