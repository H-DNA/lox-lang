#include "parser.h"
#include "error.h"

static void advance(Scanner *scanner, Parser *parser) {
  parser->previous = parser->current;

  for (;;) {
    parser->current = scanToken(scanner);
    if (parser->current.type != TOKEN_INVALID &&
        parser->current.type != TOKEN_UNCLOSED_STRING)
      break;

    reportErrorToken(scanner, parser->current);
    parser->hasError = true;
  }
}

void initParser(Parser *parser) { parser->hasError = false; }
