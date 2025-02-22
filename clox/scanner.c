#include "scanner.h"

void initScanner(Scanner* scanner, const char* source) {
  scanner->source = source;
  scanner->current = 0;
  scanner->line = 0;
}
