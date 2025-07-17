#include "parser.h"
#include "chunk.h"
#include "error.h"
#include "scanner.h"
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

static void number(Parser *parser) {
  double value = strtod(parser->current.start + parser->scanner->source, NULL);
  writeConstant(&parser->chunk, value, parser->current.line);
  advance(parser);
}

static void expression(Parser *parser) {}

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
