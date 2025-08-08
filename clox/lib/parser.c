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

static void string(Parser *parser);
static void number(Parser *parser);
static void boolean(Parser *parser);
static void nil(Parser *parser);
static int prefix_bp(TokenType type);
static void synchronize_expression(Parser *parser);
static int left_infix_bp(TokenType type);
static int right_infix_bp(TokenType type);
static void emit_infix(Parser *parser, TokenType type);
static void emit_prefix(Parser *parser, TokenType type);
static void expression_bp(Parser *parser, uint bp);
static void expression(Parser *parser);
static void grouping(Parser *parser);

void initParser(Parser *parser, Scanner *scanner, VirtualMachine *vm) {
  parser->hasError = false;
  parser->scanner = scanner;
  parser->chunk = &vm->chunk;
}

void parse(Parser *parser) {
  advance(parser);
  expression(parser);
  writeChunk(parser->chunk, OP_RETURN, parser->current.line);
}

static void grouping(Parser *parser) {
  consume(parser, TOKEN_LEFT_PAREN, "Expect opening '('");
  expression(parser);
  consume(parser, TOKEN_RIGHT_PAREN, "Expect closing ')'");
}

static void expression(Parser *parser) { expression_bp(parser, 0); }

static void expression_bp(Parser *parser, uint bp) {
  TokenType operator_type;
  switch (parser->current.type) {
  case TOKEN_MINUS:
  case TOKEN_BANG:
    operator_type = parser->current.type;
    advance(parser);
    expression_bp(parser, prefix_bp(operator_type));
    emit_prefix(parser, operator_type);
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
    synchronize_expression(parser);
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
      if (left_infix_bp(parser->current.type) < bp)
        return;
      operator_type = parser->current.type;
      advance(parser);
      expression_bp(parser, right_infix_bp(operator_type));
      emit_infix(parser, operator_type);
      break;
    default:
      return;
    }
  }
}

static int prefix_bp(TokenType type) {
  switch (type) {
  case TOKEN_MINUS:
  case TOKEN_BANG:
    return 20;
  default:
    printf("Unreachable in prefix_bp");
    exit(1);
  }
}

static void synchronize_expression(Parser *parser) {
  while (parser->current.type != TOKEN_EOF &&
         parser->current.type != TOKEN_SEMICOLON) {
    advance(parser);
  }
  if (parser->current.type == TOKEN_SEMICOLON) {
    advance(parser);
  }
}

static int left_infix_bp(TokenType type) {
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

static int right_infix_bp(TokenType type) {
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

static void emit_infix(Parser *parser, TokenType type) {
  switch (type) {
  case TOKEN_PLUS:
    writeChunk(parser->chunk, OP_ADD, parser->current.line);
    break;
  case TOKEN_MINUS:
    writeChunk(parser->chunk, OP_SUBTRACT, parser->current.line);
    break;
  case TOKEN_STAR:
    writeChunk(parser->chunk, OP_MULTIPLY, parser->current.line);
    break;
  case TOKEN_SLASH:
    writeChunk(parser->chunk, OP_DIVIDE, parser->current.line);
    break;
  case TOKEN_EQUAL_EQUAL:
    writeChunk(parser->chunk, OP_EQUAL, parser->current.line);
    break;
  case TOKEN_BANG_EQUAL:
    writeChunk(parser->chunk, OP_EQUAL, parser->current.line);
    writeChunk(parser->chunk, OP_NOT, parser->current.line);
    break;
  case TOKEN_GREATER:
    writeChunk(parser->chunk, OP_GREATER, parser->current.line);
    break;
  case TOKEN_GREATER_EQUAL:
    writeChunk(parser->chunk, OP_LESS, parser->current.line);
    writeChunk(parser->chunk, OP_NOT, parser->current.line);
    break;
  case TOKEN_LESS:
    writeChunk(parser->chunk, OP_LESS, parser->current.line);
    break;
  case TOKEN_LESS_EQUAL:
    writeChunk(parser->chunk, OP_GREATER, parser->current.line);
    writeChunk(parser->chunk, OP_NOT, parser->current.line);
    break;
  case TOKEN_AND:
    writeChunk(parser->chunk, OP_AND, parser->current.line);
    break;
  case TOKEN_OR:
    writeChunk(parser->chunk, OP_OR, parser->current.line);
    break;
  default:
    printf("Unreachable in emit_infix");
    exit(1);
  }
}

static void emit_prefix(Parser *parser, TokenType type) {
  switch (type) {
  case TOKEN_MINUS:
    writeChunk(parser->chunk, OP_NEGATE, parser->current.line);
    break;
  case TOKEN_BANG:
    writeChunk(parser->chunk, OP_NOT, parser->current.line);
    break;
  default:
    printf("Unreachable in emit_prefix");
    exit(1);
  }
}

static void string(Parser *parser) {
  int length = parser->current.end - parser->current.start - 2;
  char *raw_value = malloc(length + 1);
  memcpy(raw_value, parser->scanner->source + parser->current.start + 1,
         length);
  raw_value[length] = '\0';
  writeConstant(parser->chunk, makeString(raw_value, length),
                parser->current.line);
  advance(parser);
}

static void number(Parser *parser) {
  double raw_value =
      strtod(parser->current.start + parser->scanner->source, NULL);
  writeConstant(parser->chunk, makeNumber(raw_value), parser->current.line);
  advance(parser);
}

static void boolean(Parser *parser) {
  bool raw_value = parser->current.type == TOKEN_TRUE;
  writeConstant(parser->chunk, makeBoolean(raw_value), parser->current.line);
  advance(parser);
}

static void nil(Parser *parser) {
  writeConstant(parser->chunk, makeNil(), parser->current.line);
  advance(parser);
}
