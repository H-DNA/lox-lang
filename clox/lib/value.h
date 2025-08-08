#ifndef clox_value_h
#define clox_value_h

#include "common.h"

typedef enum {
  VAL_BOOL,
  VAL_NUMBER,
  VAL_NIL,
  VAL_OBJ,
} ValueType;

typedef struct Obj Obj;

typedef struct {
  ValueType type;
  union {
    double number;
    bool boolean;
    Obj *obj;
  };
} Value;

bool isObject(Value value);
bool isNumber(Value value);
bool isBoolean(Value value);
bool isNil(Value value);

Obj *asObject(Value value);
double asNumber(Value value);
bool asBoolean(Value value);

Value makeObject(Obj *obj);
Value makeNumber(double number);
Value makeBoolean(bool boolean);
Value makeNil();

bool isFalsy(Value value);
bool areEqual(Value, Value);

typedef struct {
  int capacity;
  int count;
  Value *values;
} ValueArray;

void initValueArray(ValueArray *array);
void writeValueArray(ValueArray *array, Value value);
void freeValueArray(ValueArray *array);

#endif
