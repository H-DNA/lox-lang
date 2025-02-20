#include "debug.h"
#include "chunk.h"
#include <stdint.h>
#include <stdio.h>

void disassembleChunk(Chunk *chunk, const char *name) {
  printf("== %s ==\n", name);

  for (int offset = 0; offset < chunk->count;) {
    offset = disassembleInstruction(chunk, offset);
  }
}

int disassembleInstruction(Chunk *chunk, int offset) {
  printf("%04d ", offset);

  uint8_t instruction = chunk->code[offset];
  switch (instruction) {
  case OP_RETURN:
    printf("%-16s\n", "OP_RETURN");
    return offset + 1;
  case OP_CONSTANT: {
    uint8_t constant = chunk->code[offset + 1];
    printf("%-16s %4d ", "OP_CONSTANT", constant);
    printValue(chunk->constants.values[constant]);
    printf("\n");
    return offset + 2;
  }
  default:
    printf("Unknown opcode %d\n", instruction);
    return offset + 1;
  }
}
