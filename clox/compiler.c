#include "compiler.h"
#include "scanner.h"
#include "vm.h"
#include <stdio.h>

void compile(VirtualMachine *vm, const char *source) {
  Scanner scanner;
  initScanner(&scanner, source);

  int line = -1;
  for (;;) {
    Token token = scanToken(&scanner);
    if (token.line != line) {
      printf("%4d ", token.line);
      line = token.line;
    } else {
      printf("   | ");
    }
    printf("%2d '%.*s'\n", token.type, token.end - token.start,
           scanner.source + token.start);

    if (token.type == TOKEN_EOF)
      break;
  }
}
