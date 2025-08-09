#ifndef clox_object_string_h
#define clox_object_string_h

#include "../object.h"
#include <stdint.h>

struct ObjString {
  Obj obj;
  int length;
  char *chars;
  uint32_t hash;
};

bool isString(Value value);

ObjString *asString(Value value);

char *getCString(Value value);
int getStringLength(Value value);

Value makeString(VirtualMachine *vm, char *string, int length);
Value concatenateStrings(VirtualMachine *vm, Value v1, Value v2);

void printString(Value value);

bool areStringsEqual(Value v1, Value v2);

void freeString(ObjString *obj);

#endif
