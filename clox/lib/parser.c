#include "parser.h"
#include "chunk.h"
#include "error.h"
#include "scanner.h"
#include "vm.h"
#include <stdio.h>
#include <stdlib.h>

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

static void number(Parser *parser);
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

void parse(Parser *parser) { expression(parser); }

static void grouping(Parser *parser) {
  consume(parser, TOKEN_LEFT_PAREN, "Expect opening '('");
  expression(parser);
  consume(parser, TOKEN_RIGHT_PAREN, "Expect closing ')'");
}

static void expression(Parser *parser) { expression_bp(parser, 0); }

static void expression_bp(Parser *parser, uint bp) {
  switch (parser->current.type) {
  case TOKEN_MINUS:
    advance(parser);
    expression_bp(parser, prefix_bp(parser->current.type));
    emit_prefix(parser, parser->current.type);
    break;
  case TOKEN_LEFT_PAREN:
    grouping(parser);
    break;
  case TOKEN_NUMBER:
    number(parser);
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
      if (left_infix_bp(parser->current.type) < bp)
        return;
      advance(parser);
      expression_bp(parser, right_infix_bp(parser->current.type));
      emit_infix(parser, parser->current.type);
      break;
    default:
      return;
    }
  }
}

static int prefix_bp(TokenType type) {
  switch (type) {
  case TOKEN_MINUS:
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
    return 1;
  case TOKEN_STAR:
  case TOKEN_SLASH:
    return 3;
  default:
    printf("Unreachable in left_infix_bp");
    exit(1);
  }
}

static int right_infix_bp(TokenType type) {
  switch (type) {
  case TOKEN_PLUS:
  case TOKEN_MINUS:
    return 2;
  case TOKEN_STAR:
  case TOKEN_SLASH:
    return 4;
  default:
    printf("Unreachable in right_infix_bp");
    exit(1);
  }
}

static void emit_infix(Parser *parser, TokenType type) {
  switch (type) {
  case TOKEN_PLUS:
    writeChunk(parser->chunk, OP_ADD, parser->current.line);
  case TOKEN_MINUS:
    writeChunk(parser->chunk, OP_SUBTRACT, parser->current.line);
  case TOKEN_STAR:
    writeChunk(parser->chunk, OP_MULTIPLY, parser->current.line);
  case TOKEN_SLASH:
    writeChunk(parser->chunk, OP_DIVIDE, parser->current.line);
  default:
    printf("Unreachable in emit_infix");
    exit(1);
  }
}

static void emit_prefix(Parser *parser, TokenType type) {
  switch (type) {
  case TOKEN_MINUS:
    writeChunk(parser->chunk, OP_NEGATE, parser->current.line);
  default:
    printf("Unreachable in emit_prefix");
    exit(1);
  }
}

static void number(Parser *parser) {
  double value = strtod(parser->current.start + parser->scanner->source, NULL);
  writeConstant(parser->chunk, value, parser->current.line);
  advance(parser);
}
