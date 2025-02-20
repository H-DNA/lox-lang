#include "vm.h"
#include "chunk.h"

void initVM(VirtualMachine *vm) {
  initChunk(vm->chunk);
  vm->ip = 0;
}

void freeVM(VirtualMachine *vm) { freeChunk(vm->chunk); }
