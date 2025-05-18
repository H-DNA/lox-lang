#ifndef clox_compiler_h
#define clox_compiler_h

#include "scanner.h"
#include "vm.h"

bool compile(VirtualMachine *vm, const char *source);

#endif
