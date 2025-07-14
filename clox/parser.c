#include "parser.h"
#include "error.h"
#include "scanner.h"

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

void initParser(Parser *parser, Scanner *scanner) {
  parser->hasError = false;
  parser->scanner = scanner;
}
