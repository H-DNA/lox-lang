#include "vm.h"
#include "chunk.h"
#include <stdio.h>

void initVM(VirtualMachine *vm) {
  initChunk(&vm->chunk);
  vm->ip = 0;
}

void freeVM(VirtualMachine *vm) { freeChunk(&vm->chunk); }

InterpretResult interpret(VirtualMachine *vm) {
#define READ_BYTE() (vm->chunk.code[vm->ip++])
#define READ_2BYTES()                                                          \
  (vm->ip += 2, (vm->chunk.code[vm->ip - 1] << 8) + vm->chunk.code[vm->ip])
#define READ_CONSTANT(id) (vm->chunk.constants.values[(id)])
  while (true) {
    uint8_t instruction = READ_BYTE();
    switch (instruction) {
    case OP_RETURN:
      return INTERPRET_OK;
    case OP_CONSTANT: {
      Value constant = READ_CONSTANT(READ_BYTE());
      printValue(constant);
      printf("\n");
      break;
    }
    case OP_CONSTANT_LONG: {
      Value constant = READ_CONSTANT(READ_2BYTES());
      printValue(constant);
      printf("\n");
      break;
    }
    }
  }
#undef READ_BYTE
#undef READ_2BYTES
#undef READ_CONSTANT
}
