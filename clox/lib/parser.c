#include "parser.h"
#include "chunk.h"
#include "error.h"
#include "object/string.h"
#include "scanner.h"
#include "value.h"
#include "vm.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void advance(Parser *parser) {
  for (;;) {
    parser->current = scanToken(parser->scanner);
    if (parser->current.type != TOKEN_INVALID &&
        parser->current.type != TOKEN_UNCLOSED_STRING)
      break;

    reportErrorToken(parser->scanner, parser->current);
    parser->hasError = true;
  }
}

static void consume(Parser *parser, TokenType type, const char *message) {
  if (parser->current.type == type) {
    advance(parser);
    return;
  }

  reportError(message, parser->current.line);
  parser->hasError = true;
}

static bool match(Parser *parser, TokenType type) {
  if (parser->current.type != type) {
    return false;
  }
  advance(parser);
  return true;
}

static void string(Parser *parser);
static void number(Parser *parser);
static void boolean(Parser *parser);
static void nil(Parser *parser);
static int prefixBp(TokenType type);
static void synchronizeExpression(Parser *parser);
static int leftInfixBp(TokenType type);
static int rightInfixBp(TokenType type);
static void emitInfix(Parser *parser, TokenType type);
static void emitPrefix(Parser *parser, TokenType type);
static void expressionBp(Parser *parser, uint bp);
static void expression(Parser *parser);
static void grouping(Parser *parser);
static void declaration(Parser *parser);
static void statement(Parser *parser);
static void printStatement(Parser *parser);
static void expressionStatement(Parser *parser);

void initParser(Parser *parser, Scanner *scanner, VirtualMachine *vm) {
  parser->hasError = false;
  parser->scanner = scanner;
  parser->vm = vm;
}

void parse(Parser *parser) {
  advance(parser);
  while (!match(parser, TOKEN_EOF)) {
    declaration(parser);
  }
  writeChunk(&parser->vm->chunk, OP_RETURN, parser->current.line);
}

static void declaration(Parser *parser) {
  statement(parser);
}

static void statement(Parser *parser) {
  switch (parser->current.type) {
  case TOKEN_PRINT:
    printStatement(parser);
    break;
  default:
    expressionStatement(parser);
  }
}

static void printStatement(Parser *parser) {
  consume(parser, TOKEN_PRINT, "Expect the 'print' keyword");
  expression(parser);
  consume(parser, TOKEN_SEMICOLON, "Expect the ending semicolon ';'");
  writeChunk(&parser->vm->chunk, OP_PRINT, parser->current.line);
}

static void expressionStatement(Parser *parser) {
  expression(parser);
  consume(parser, TOKEN_SEMICOLON, "Expect the ending semicolon ';'");
  writeChunk(&parser->vm->chunk, OP_POP, parser->current.line);
}

static void grouping(Parser *parser) {
  consume(parser, TOKEN_LEFT_PAREN, "Expect opening '('");
  expression(parser);
  consume(parser, TOKEN_RIGHT_PAREN, "Expect closing ')'");
}

static void expression(Parser *parser) { expressionBp(parser, 0); }

static void expressionBp(Parser *parser, uint bp) {
  TokenType operatorType;
  switch (parser->current.type) {
  case TOKEN_MINUS:
  case TOKEN_BANG:
    operatorType = parser->current.type;
    advance(parser);
    expressionBp(parser, prefixBp(operatorType));
    emitPrefix(parser, operatorType);
    break;
  case TOKEN_LEFT_PAREN:
    grouping(parser);
    break;
  case TOKEN_NUMBER:
    number(parser);
    break;
  case TOKEN_TRUE:
  case TOKEN_FALSE:
    boolean(parser);
    break;
  case TOKEN_STRING:
    string(parser);
    break;
  case TOKEN_NIL:
    nil(parser);
    break;
  default:
    reportError("Invalid operand", parser->current.line);
    synchronizeExpression(parser);
    parser->hasError = true;
    return;
  }

  while (true) {
    switch (parser->current.type) {
    case TOKEN_MINUS:
    case TOKEN_PLUS:
    case TOKEN_STAR:
    case TOKEN_SLASH:
    case TOKEN_BANG_EQUAL:
    case TOKEN_EQUAL_EQUAL:
    case TOKEN_GREATER:
    case TOKEN_GREATER_EQUAL:
    case TOKEN_LESS:
    case TOKEN_LESS_EQUAL:
    case TOKEN_AND:
    case TOKEN_OR:
      if (leftInfixBp(parser->current.type) < bp)
        return;
      operatorType = parser->current.type;
      advance(parser);
      expressionBp(parser, rightInfixBp(operatorType));
      emitInfix(parser, operatorType);
      break;
    default:
      return;
    }
  }
}

static int prefixBp(TokenType type) {
  switch (type) {
  case TOKEN_MINUS:
  case TOKEN_BANG:
    return 20;
  default:
    printf("Unreachable in prefix_bp");
    exit(1);
  }
}

static void synchronizeExpression(Parser *parser) {
  while (parser->current.type != TOKEN_EOF &&
         parser->current.type != TOKEN_SEMICOLON) {
    advance(parser);
  }
}

static int leftInfixBp(TokenType type) {
  switch (type) {
  case TOKEN_PLUS:
  case TOKEN_MINUS:
    return 11;
  case TOKEN_STAR:
  case TOKEN_SLASH:
    return 13;
  case TOKEN_EQUAL_EQUAL:
  case TOKEN_GREATER:
  case TOKEN_GREATER_EQUAL:
  case TOKEN_LESS:
  case TOKEN_LESS_EQUAL:
  case TOKEN_BANG_EQUAL:
    return 9;
  case TOKEN_AND:
    return 7;
  case TOKEN_OR:
    return 5;
  default:
    printf("Unreachable in left_infix_bp");
    exit(1);
  }
}

static int rightInfixBp(TokenType type) {
  switch (type) {
  case TOKEN_PLUS:
  case TOKEN_MINUS:
    return 12;
  case TOKEN_STAR:
  case TOKEN_SLASH:
    return 14;
  case TOKEN_EQUAL_EQUAL:
  case TOKEN_GREATER:
  case TOKEN_GREATER_EQUAL:
  case TOKEN_LESS:
  case TOKEN_LESS_EQUAL:
  case TOKEN_BANG_EQUAL:
    return 10;
  case TOKEN_AND:
    return 8;
  case TOKEN_OR:
    return 6;
  default:
    printf("Unreachable in right_infix_bp");
    exit(1);
  }
}

static void emitInfix(Parser *parser, TokenType type) {
  switch (type) {
  case TOKEN_PLUS:
    writeChunk(&parser->vm->chunk, OP_ADD, parser->current.line);
    break;
  case TOKEN_MINUS:
    writeChunk(&parser->vm->chunk, OP_SUBTRACT, parser->current.line);
    break;
  case TOKEN_STAR:
    writeChunk(&parser->vm->chunk, OP_MULTIPLY, parser->current.line);
    break;
  case TOKEN_SLASH:
    writeChunk(&parser->vm->chunk, OP_DIVIDE, parser->current.line);
    break;
  case TOKEN_EQUAL_EQUAL:
    writeChunk(&parser->vm->chunk, OP_EQUAL, parser->current.line);
    break;
  case TOKEN_BANG_EQUAL:
    writeChunk(&parser->vm->chunk, OP_EQUAL, parser->current.line);
    writeChunk(&parser->vm->chunk, OP_NOT, parser->current.line);
    break;
  case TOKEN_GREATER:
    writeChunk(&parser->vm->chunk, OP_GREATER, parser->current.line);
    break;
  case TOKEN_GREATER_EQUAL:
    writeChunk(&parser->vm->chunk, OP_LESS, parser->current.line);
    writeChunk(&parser->vm->chunk, OP_NOT, parser->current.line);
    break;
  case TOKEN_LESS:
    writeChunk(&parser->vm->chunk, OP_LESS, parser->current.line);
    break;
  case TOKEN_LESS_EQUAL:
    writeChunk(&parser->vm->chunk, OP_GREATER, parser->current.line);
    writeChunk(&parser->vm->chunk, OP_NOT, parser->current.line);
    break;
  case TOKEN_AND:
    writeChunk(&parser->vm->chunk, OP_AND, parser->current.line);
    break;
  case TOKEN_OR:
    writeChunk(&parser->vm->chunk, OP_OR, parser->current.line);
    break;
  default:
    printf("Unreachable in emit_infix");
    exit(1);
  }
}

static void emitPrefix(Parser *parser, TokenType type) {
  switch (type) {
  case TOKEN_MINUS:
    writeChunk(&parser->vm->chunk, OP_NEGATE, parser->current.line);
    break;
  case TOKEN_BANG:
    writeChunk(&parser->vm->chunk, OP_NOT, parser->current.line);
    break;
  default:
    printf("Unreachable in emit_prefix");
    exit(1);
  }
}

static void string(Parser *parser) {
  int length = parser->current.end - parser->current.start - 2;
  writeConstant(&parser->vm->chunk,
                makeString(parser->vm,
                           parser->scanner->source + parser->current.start + 1,
                           length),
                parser->current.line);
  advance(parser);
}

static void number(Parser *parser) {
  double raw_value =
      strtod(parser->current.start + parser->scanner->source, NULL);
  writeConstant(&parser->vm->chunk, makeNumber(raw_value),
                parser->current.line);
  advance(parser);
}

static void boolean(Parser *parser) {
  bool raw_value = parser->current.type == TOKEN_TRUE;
  writeConstant(&parser->vm->chunk, makeBoolean(raw_value),
                parser->current.line);
  advance(parser);
}

static void nil(Parser *parser) {
  writeConstant(&parser->vm->chunk, makeNil(), parser->current.line);
  advance(parser);
}
