#include "chunk.h"
#include "common.h"
#include "debug.h"
#include "vm.h"

int main(int argc, const char *argv[]) {
  VirtualMachine vm;
  initVM(&vm);
  for (int i = 0; i < 300; ++i) {
  //  writeConstant(vm.chunk, i, 0);
  }
  writeChunk(&vm.chunk, OP_RETURN, 0);
  // interpret(&vm);
  freeVM(&vm);
  return 0;
}
