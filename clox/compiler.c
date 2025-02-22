#include "compiler.h"
#include "scanner.h"
#include "vm.h"

void compile(VirtualMachine *vm, const char *source) {
  Scanner scanner;
  initScanner(&scanner, source);
}
