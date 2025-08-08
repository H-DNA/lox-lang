#include "scanner.h"
#include "vm.h"
#include <stdarg.h>
#include <stdio.h>

void reportError(const char *message, unsigned int line) {
  fprintf(stderr, "[line %u] Error: %s\n", line + 1, message);
}

void reportErrorToken(Scanner *scanner, Token token) {
  char message[1024];
  if (token.type == TOKEN_INVALID) {
    sprintf(message, "Invalid character: %*s", token.end - token.start,
            scanner->source + token.start);
    reportError(message, token.line);
  } else if (token.type == TOKEN_UNCLOSED_STRING) {
    // TODO: make it safe against overflow attack
    sprintf(message, "Unclosed string: %*s", token.end - token.start,
            scanner->source + token.start);
    reportError(message, token.line);
  }
}
