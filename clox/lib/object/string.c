#include "./string.h"
#include "../object.h"
#include "../value.h"
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

// Taking ownership of string
Value makeString(char *string, int length) {
  ObjString *obj = malloc(sizeof(ObjString));
  obj->chars = string;
  obj->length = length;
  Value value = {.type = VAL_OBJ, .obj = (Obj *)obj};
  return value;
}

void printString(Value value) {
  ObjString *obj = asString(value);
  printf("%s", obj->chars);
}

bool areStringsEqual(Value v1, Value v2) {
  ObjString* s1 = asString(v1);
  ObjString* s2 = asString(v2);
  return s1->length == s2->length && memcmp(s1, s2, s1->length);
}
