#include "vm.h"
#include "chunk.h"

void initVM(VM *vm) { initChunk(vm->chunk); }

void freeVM(VM *vm) { freeChunk(vm->chunk); }
