#include "value.h"
#include "object.h"
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
    int newCapacity = array->capacity * 2;
    array->capacity = newCapacity;
    array->values = realloc(array->values, newCapacity * sizeof(Value));
    if (array->values == NULL) {
      fprintf(stderr, "Failed to realloc memory in writeChunk with capacity %d",
              (int)newCapacity);
      exit(1);
    }
  }
  array->values[array->count++] = value;
}

void freeValueArray(ValueArray *array) { free(array->values); }

bool isObject(Value value) { return value.type == VAL_OBJ; }
bool isNumber(Value value) { return value.type == VAL_NUMBER; }
bool isBoolean(Value value) { return value.type == VAL_BOOL; }
bool isNil(Value value) { return value.type == VAL_NIL; }

Obj *asObject(Value value) { return value.obj; }
double asNumber(Value value) { return value.number; }
bool asBoolean(Value value) { return value.boolean; }

Value makeObject(Obj *obj) {
  Value value = {.type = VAL_BOOL, .obj = obj};
  return value;
}
Value makeNumber(double number) {
  Value value = {.type = VAL_NUMBER, .number = number};
  return value;
}
Value makeBoolean(bool boolean) {
  Value value = {.type = VAL_BOOL, .boolean = boolean};
  return value;
}
Value makeNil() {
  Value value = {.type = VAL_NIL};
  return value;
}

bool isFalsy(Value value) {
  return isNil(value) || (isBoolean(value) && !asBoolean(value));
}

bool areEqual(Value first, Value second) {
  if (first.type != second.type)
    return false;
  switch (first.type) {
  case VAL_NIL:
    return true;
  case VAL_BOOL:
    return second.boolean == first.boolean;
  case VAL_NUMBER:
    return second.number == first.number;
  case VAL_OBJ:
    return areObjectsEqual(first, second);
  default:
    printf("Unreachable in areEqual");
    exit(1);
  }
}
