#include "./string.h"
#include "../object.h"
#include "../value.h"
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

bool isString(Value value) {
  return isObject(value) && asObject(value)->type == OBJ_STRING;
}

ObjString *asString(Value value) { return (ObjString *)asObject(value); }

char *getCString(Value value) { return ((ObjString *)asObject(value))->chars; }
int getStringLength(Value value) {
  return ((ObjString *)asObject(value))->length;
}

static uint32_t hashString(const char *str, int length) {
  uint32_t hash = 2166136261u;
  for (int i = 0; i < length; i++) {
    hash ^= (uint8_t)str[i];
    hash *= 16777619;
  }
  return hash;
}

Value makeString(VirtualMachine *vm, const char *string, int length) {
  uint32_t hash = hashString(string, length);
  ObjString *interned = tableFindString(&vm->strings, string, length, hash);
  if (interned != NULL) {
    Value value = {.type = VAL_OBJ, .obj = (Obj *)interned};
    return value;
  }

  ObjString *obj =
      (ObjString *)allocateObject(vm, sizeof(ObjString), OBJ_STRING);
  char *rawValue = malloc(length + 1);
  memcpy(rawValue, string, length);
  rawValue[length] = '\0';
  obj->chars = rawValue;
  obj->length = length;
  obj->hash = hash;

  tableSet(&vm->strings, obj, makeNil());

  Value value = {.type = VAL_OBJ, .obj = (Obj *)obj};
  return value;
}

Value concatenateStrings(VirtualMachine *vm, Value v1, Value v2) {
  ObjString *obj1 = asString(v1);
  ObjString *obj2 = asString(v2);
  int length = obj1->length + obj2->length;
  char *res = malloc(length);
  memcpy(res, obj1->chars, obj1->length);
  memcpy(res + obj1->length, obj2->chars, obj2->length);
  Value value = makeString(vm, res, length);
  free(res);
  return value;
}

void printString(Value value) {
  ObjString *obj = asString(value);
  printf("%s", obj->chars);
}

void freeString(ObjString *obj) {
  free(obj->chars);
  free(obj);
}
