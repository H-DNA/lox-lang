#include "vm.h"
#include "chunk.h"
#include "debug.h"
#include <stdio.h>

void initVM(VirtualMachine *vm) {
  initChunk(&vm->chunk);
  vm->ip = 0;
  vm->stackTop = 0;
}

void freeVM(VirtualMachine *vm) { freeChunk(&vm->chunk); }

InterpretResult interpret(VirtualMachine *vm) {
#define READ_BYTE() (vm->chunk.code[vm->ip++])
#define READ_2BYTES()                                                          \
  (vm->ip += 2, (vm->chunk.code[vm->ip - 2] << 8) + vm->chunk.code[vm->ip - 1])
#define READ_CONSTANT(id) (vm->chunk.constants.values[(id)])
  while (true) {
#ifdef DEBUG_TRACE_EXECUTION
    disassembleInstruction(&vm->chunk, vm->ip);
#endif
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
