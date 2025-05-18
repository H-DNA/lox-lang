#include "compiler.h"
#include "parser.h"
#include "scanner.h"
#include "vm.h"

bool compile(VirtualMachine *vm, const char *source) {
  Scanner scanner;
  initScanner(&scanner, source);
  Parser parser;
  initParser(&parser);

  return !parser.hasError;
}
