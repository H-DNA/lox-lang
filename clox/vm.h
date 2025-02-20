#ifndef clox_vm_h
#define clox_vm_h

#include "chunk.h"

#define STACK_MAX 256

typedef struct {
  Chunk chunk;
  unsigned int ip;
  Value stack[STACK_MAX];
  unsigned int stackTop;
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
