#include "value.h"
#include <stdio.h>
#include <stdlib.h>

void initValueArray(ValueArray *array) {
  array->count = 0;
  array->capacity = 8;
  array->values = (Value *)malloc(array->capacity * sizeof(Value));
  if (array->values == NULL) {
    fprintf(stderr,
            "Failed to malloc memory in initValueArray with capacity 8");
    exit(1);
  }
}

void writeValueArray(ValueArray *array, Value value) {
  if (array->count == array->capacity) {
    int new_capacity = array->capacity * 2;
    array->capacity = new_capacity;
    array->values = realloc(array->values, new_capacity);
    if (array->values == NULL) {
      fprintf(stderr, "Failed to realloc memory in writeChunk with capacity %d",
              (int)new_capacity);
      exit(1);
    }
  }
  array->values[array->count++] = value;
}

void freeValueArray(ValueArray *array) { free(array->values); }
