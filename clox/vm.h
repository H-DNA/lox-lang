#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"

typedef struct {
  Chunk chunk;
  unsigned int ip;
} VirtualMachine;

typedef enum {
  INTERPRET_OK,
  INTERPRET_COMPILE_ERROR,
  INTERPRET_RUNTIME_ERROR,
} InterpretResult;

void initVM(VirtualMachine *vm);
void freeVM(VirtualMachine *vm);
InterpretResult interpret(VirtualMachine *vm);

#endif
