#include "chunk.h"
#include <stdint.h>
#include <stdlib.h>

void initChunk(Chunk *chunk) {
  chunk->count = 0;
  chunk->capacity = 8;
  chunk->code = (uint8_t *)malloc(8 * sizeof(uint8_t));
}

void writeChunk(Chunk *chunk, uint8_t byte) {
  if (chunk->count == chunk->capacity) {
    int new_capacity = chunk->capacity * 2;
    chunk->capacity = new_capacity;
    chunk->code = realloc(chunk->code, new_capacity);
  }
  chunk->code[chunk->count++] = byte;
}

void freeChunk(Chunk* chunk) {
  free(chunk->code);
}
