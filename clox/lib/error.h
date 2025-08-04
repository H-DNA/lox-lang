#ifndef clox_error_h
#define clox_error_h

#include "scanner.h"
#include "vm.h"

void reportError(const char *message, unsigned int line);

void reportErrorToken(Scanner *scanner, Token token);

void reportRuntimeError(VirtualMachine* vm, const char *format, ...);

#endif
