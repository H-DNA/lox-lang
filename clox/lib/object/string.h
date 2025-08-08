#ifndef clox_object_string_h
#define clox_object_string_h

#include "../object.h"

struct ObjString {
  Obj obj;
  int length;
  char *chars;
};

bool isString(Value value);

ObjString *asString(Value value);

char *getCString(Value value);
int getStringLength(Value value);

Value makeString(char *string, int length);

void printString(Value value);

bool areStringsEqual(Value v1, Value v2);

#endif
