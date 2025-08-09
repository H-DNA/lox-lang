#include "object.h"
#include "object/string.h"
#include "value.h"
#include <stdio.h>
#include <stdlib.h>

ObjType objectType(Value value) { return asObject(value)->type; }

void printObject(Value value) {
  switch (asObject(value)->type) {
  case OBJ_STRING:
    printString(value);
    break;
  default:
    printf("Unreachable in printObject");
    exit(1);
  }
}

bool areObjectsEqual(Value v1, Value v2) {
  Obj *obj1 = asObject(v1);
  Obj *obj2 = asObject(v2);

  if (obj1->type != obj2->type) {
    return false;
  }

  switch (obj1->type) {
  case OBJ_STRING:
    return areStringsEqual(v1, v2);
  default:
    return obj1 == obj2;
  }
}

Obj *allocateObject(size_t size, ObjType type) {
  Obj *object = (Obj *)malloc(size);
  object->type = type;
  return object;
}
