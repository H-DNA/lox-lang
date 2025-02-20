#include "chunk.h"
#include "common.h"
#include "debug.h"

int main(int argc, const char *argv[]) {
  Chunk chunk;
  initChunk(&chunk);
  int index = addConstant(&chunk, 1.2);
  writeChunk(&chunk, OP_CONSTANT, 0);
  writeChunk(&chunk, index, 0);
  writeChunk(&chunk, OP_RETURN, 0);
  disassembleChunk(&chunk, "test chunk");
  freeChunk(&chunk);
  return 0;
}
