#include "chunk.h"
#include "common.h"
#include "debug.h"

int main(int argc, const char *argv[]) {
  Chunk chunk;
  initChunk(&chunk);
  int index = addConstant(&chunk, 1.2);
  writeChunk(&chunk, OP_CONSTANT);
  writeChunk(&chunk, index);
  writeChunk(&chunk, OP_RETURN);
  disassembleChunk(&chunk, "test chunk");
  freeChunk(&chunk);
  return 0;
}
