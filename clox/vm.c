#include "vm.h"
#include "chunk.h"

void initVM(VirtualMachine *vm) { initChunk(vm->chunk); }

void freeVM(VirtualMachine *vm) { freeChunk(vm->chunk); }
