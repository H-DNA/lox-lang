#include "chunk.h"
#include "value.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

void initChunk(Chunk *chunk) {
  chunk->count = 0;
  chunk->capacity = 8;

  chunk->code = (uint8_t *)malloc(chunk->capacity * sizeof(uint8_t));
  chunk->lines = (unsigned int *)malloc(chunk->capacity * sizeof(unsigned int));
  if (chunk->code == NULL || chunk->lines == NULL) {
    fprintf(stderr, "Failed to malloc memory in initChunk with capacity 8");
    exit(1);
  }

  initValueArray(&chunk->constants);
}

void writeChunk(Chunk *chunk, uint8_t byte, unsigned int line) {
  if (chunk->count == chunk->capacity) {
    int new_capacity = chunk->capacity * 2;
    chunk->capacity = new_capacity;
    chunk->code = realloc(chunk->code, new_capacity);
    chunk->lines = realloc(chunk->lines, new_capacity);
    if (chunk->code == NULL || chunk->lines == NULL) {
      fprintf(stderr, "Failed to realloc memory in writeChunk with capacity %d",
              (int)new_capacity);
      exit(1);
    }
  }
  chunk->code[chunk->count] = byte;
  chunk->lines[chunk->count] = line;
  ++chunk->count;
}

void freeChunk(Chunk *chunk) {
  free(chunk->code);
  free(chunk->lines);
  freeValueArray(&chunk->constants);
}

int addConstant(Chunk *chunk, Value value) {
  writeValueArray(&chunk->constants, value);
  return chunk->constants.count - 1;
}

void printValue(Value value) { printf("%g", value); }
