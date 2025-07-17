#include "parser.h"
#include "chunk.h"
#include "error.h"
#include "scanner.h"
#include <stdio.h>
#include <stdlib.h>

static void advance(Parser *parser) {
  parser->previous = parser->current;

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

static void number(Parser *parser) {
  double value = strtod(parser->previous.start + parser->scanner->source, NULL);
  writeConstant(&parser->chunk, value, parser->previous.line);
}

static void expression(Parser *parser) {

}

static void unary(Parser *parser) {
  Token op = parser->previous;

  expression(parser);

  switch (op.type) {
  case TOKEN_MINUS:
    writeChunk(&parser->chunk, OP_NEGATE, op.line);
    break;
  default:
    printf("Unreachable in unary()");
    exit(1);
  }
}

static void grouping(Parser *parser) {
  consume(parser, TOKEN_LEFT_PAREN, "Expect opening '('");
  expression(parser);
  consume(parser, TOKEN_RIGHT_PAREN, "Expect closing ')'");
}

void initParser(Parser *parser, Scanner *scanner) {
  parser->hasError = false;
  parser->scanner = scanner;
  initChunk(&parser->chunk);
}
