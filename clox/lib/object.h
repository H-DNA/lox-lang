#ifndef clox_object_h
#define clox_object_h

#include "value.h"

typedef enum {
  OBJ_STRING,
} ObjType;

struct Obj {
  ObjType type;
  Obj *next;
};

typedef struct ObjString ObjString;

ObjType objectType(Value value);

void printObject(Value value);

bool areObjectsEqual(Value, Value);

Obj *allocateObject(size_t size, ObjType type);

#endif
