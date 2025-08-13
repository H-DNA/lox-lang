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
  case OP_DEFINE_GLOBAL: {
    uint8_t constant = chunk->code[offset + 1];
    printf("%-16s %4d ", "OP_DEFINE_GLOBAL", constant);
    printValue(chunk->constants.values[constant]);
    printf("\n");
    return offset + 2;
  }
  case OP_GET_GLOBAL: {
    uint8_t constant = chunk->code[offset + 1];
    printf("%-16s %4d ", "OP_GET_GLOBAL", constant);
    printValue(chunk->constants.values[constant]);
    printf("\n");
    return offset + 2;
  }
  case OP_CONSTANT_LONG: {
    uint8_t constant_first = chunk->code[offset + 1];
    uint8_t constant_second = chunk->code[offset + 2];
    unsigned int constant = (constant_first << 8) + constant_second;
    printf("%-16s %4d ", "OP_CONSTANT_LONG", constant);
    printValue(chunk->constants.values[constant]);
    printf("\n");
    return offset + 3;
  }
  case OP_NEGATE: {
    printf("%-16s\n", "OP_NEGATE");
    return offset + 1;
  }
  case OP_NOT: {
    printf("%-16s\n", "OP_NOT");
    return offset + 1;
  }
  case OP_ADD: {
    printf("%-16s\n", "OP_ADD");
    return offset + 1;
  }
  case OP_SUBTRACT: {
    printf("%-16s\n", "OP_SUBTRACT");
    return offset + 1;
  }
  case OP_MULTIPLY: {
    printf("%-16s\n", "OP_MULTIPLY");
    return offset + 1;
  }
  case OP_DIVIDE: {
    printf("%-16s\n", "OP_DIVIDE");
    return offset + 1;
  }
  case OP_EQUAL: {
    printf("%-16s\n", "OP_EQUAL");
    return offset + 1;
  }
  case OP_GREATER: {
    printf("%-16s\n", "OP_GREATER");
    return offset + 1;
  }
  case OP_LESS: {
    printf("%-16s\n", "OP_LESS");
    return offset + 1;
  }
  case OP_AND: {
    printf("%-16s\n", "OP_AND");
    return offset + 1;
  }
  case OP_OR: {
    printf("%-16s\n", "OP_OR");
    return offset + 1;
  }
  case OP_PRINT: {
    printf("%-16s\n", "OP_PRINT");
    return offset + 1;
  }
  case OP_TRUE: {
    printf("%-16s\n", "OP_TRUE");
    return offset + 1;
  }
  case OP_FALSE: {
    printf("%-16s\n", "OP_FALSE");
    return offset + 1;
  }
  case OP_NIL: {
    printf("%-16s\n", "OP_NIL");
    return offset + 1;
  }
  case OP_POP: {
    printf("%-16s\n", "OP_POP");
    return offset + 1;
  }
  default:
    printf("Unknown opcode %d\n", instruction);
    return offset + 1;
  }
}
