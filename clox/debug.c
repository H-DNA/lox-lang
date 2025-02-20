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

  if (offset > 0 && chunk->lines[offset] == chunk->lines[offset - 1]) {
    printf("   | ");
  } else {
    printf("%4d ", chunk->lines[offset]);
  }

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
  case OP_CONSTANT_LONG: {
    uint8_t constant_first = chunk->code[offset + 1];
    uint8_t constant_second = chunk->code[offset + 2];
    unsigned int constant =
        (constant_first << 8) + constant_second;
    printf("%-16s %4d ", "OP_CONSTANT_LONG", constant);
    printValue(chunk->constants.values[constant]);
    printf("\n");
    return offset + 3;
  }
  default:
    printf("Unknown opcode %d\n", instruction);
    return offset + 1;
  }
}
