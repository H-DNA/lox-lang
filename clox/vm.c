#include "vm.h"
#include "chunk.h"

void initVM(VirtualMachine *vm) {
  initChunk(vm->chunk);
  vm->ip = 0;
}

void freeVM(VirtualMachine *vm) { freeChunk(vm->chunk); }

InterpretResult interpret(VirtualMachine *vm) {
  while (true) {
    uint8_t instruction = vm->ip++;
    switch (instruction) {
      case OP_RETURN:
        return INTERPRET_OK;
    }
  }
}
