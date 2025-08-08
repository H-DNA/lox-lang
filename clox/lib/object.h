#ifndef clox_object_h
#define clox_object_h

#include "value.h"

typedef enum {
  OBJ_STRING,
} ObjType;

struct Obj {
  ObjType type;
};

typedef struct ObjString ObjString;

ObjType objectType(Value value);

void printObject(Value value);

bool areObjectsEqual(Value, Value);

#endif
