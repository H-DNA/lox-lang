#include "vm.h"
#include "chunk.h"
#include "compiler.h"
#include "debug.h"
#include "error.h"
#include "object/string.h"
#include "table.h"
#include "value.h"
#include <stdarg.h>
#include <stdio.h>

static void runtimeError(VirtualMachine *vm, const char *format, ...) {
  int line = vm->chunk.lines[vm->ip];
  fprintf(stderr, "[line %d] ", line);
  va_list args;
  va_start(args, format);
  vfprintf(stderr, format, args);
  va_end(args);
  fputs("\n", stderr);
}

void initVM(VirtualMachine *vm) {
  initChunk(&vm->chunk);
  vm->ip = 0;
  vm->stackTop = 0;
  vm->objects = NULL;
  initTable(&vm->strings);
}

void freeVM(VirtualMachine *vm) {
  freeChunk(&vm->chunk);
  freeTable(&vm->strings);
  freeObjects(vm->objects);
}

static InterpretResult run(VirtualMachine *vm) {
#define READ_BYTE() (vm->chunk.code[vm->ip++])
#define READ_2BYTES()                                                          \
  (vm->ip += 2, (vm->chunk.code[vm->ip - 2] << 8) + vm->chunk.code[vm->ip - 1])
#define READ_CONSTANT(id) (vm->chunk.constants.values[(id)])
  while (true) {
#ifdef DEBUG_TRACE_EXECUTION
    printf("          ");
    for (int i = 0; i < vm->stackTop; ++i) {
      printf("[ ");
      printValue(vm->stack[i]);
      printf(" ]");
    }
    printf("\n");
    disassembleInstruction(&vm->chunk, vm->ip);
#endif
    uint8_t instruction = READ_BYTE();
    switch (instruction) {
    case OP_RETURN:
      return INTERPRET_OK;
    case OP_PRINT:
      printValue(pop(vm));
      printf("\n");
      break;
    case OP_POP:
      pop(vm);
      break;
    case OP_CONSTANT: {
      Value constant = READ_CONSTANT(READ_BYTE());
      push(vm, constant);
      break;
    }
    case OP_CONSTANT_LONG: {
      Value constant = READ_CONSTANT(READ_2BYTES());
      push(vm, constant);
      break;
    }
    case OP_NIL: {
      push(vm, makeNil());
      break;
    }
    case OP_TRUE: {
      push(vm, makeBoolean(true));
      break;
    }
    case OP_FALSE: {
      push(vm, makeBoolean(false));
      break;
    }
    case OP_NEGATE: {
      Value operand = pop(vm);
      if (!isNumber(operand)) {
        runtimeError(vm, "Operands must be two numbers");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(vm, makeNumber(-asNumber(pop(vm))));
      break;
    }
    case OP_NOT: {
      Value operand = pop(vm);
      push(vm, makeBoolean(isFalsy(operand)));
      break;
    }
    case OP_EQUAL: {
      Value first = pop(vm);
      Value second = pop(vm);
      push(vm, makeBoolean(areEqual(second, first)));
      break;
    }
    case OP_GREATER: {
      Value first = pop(vm);
      Value second = pop(vm);
      if (!isNumber(first) || !isNumber(second)) {
        runtimeError(vm, "Operands must be two numbers");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(vm, makeBoolean(asNumber(second) > asNumber(first)));
      break;
    }
    case OP_LESS: {
      Value first = pop(vm);
      Value second = pop(vm);
      if (!isNumber(first) || !isNumber(second)) {
        runtimeError(vm, "Operands must be two numbers");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(vm, makeBoolean(asNumber(second) < asNumber(first)));
      break;
    }
    case OP_AND: {
      Value first = pop(vm);
      Value second = pop(vm);
      push(vm, makeBoolean(!isFalsy(first) && !isFalsy(second)));
      break;
    }
    case OP_OR: {
      Value first = pop(vm);
      Value second = pop(vm);
      push(vm, makeBoolean(!isFalsy(first) || !isFalsy(second)));
      break;
    }
    case OP_ADD: {
      Value first = pop(vm);
      Value second = pop(vm);
      if (isNumber(first) && isNumber(second)) {
        push(vm, makeNumber(asNumber(first) + asNumber(second)));
      } else if (isString(first) && isString(second)) {
        push(vm, concatenateStrings(vm, second, first));
      } else {
        runtimeError(vm, "Operands must be two numbers or two strings");
        return INTERPRET_RUNTIME_ERROR;
      }
      break;
    }
    case OP_SUBTRACT: {
      Value first = pop(vm);
      Value second = pop(vm);
      if (!isNumber(first) || !isNumber(second)) {
        runtimeError(vm, "Operands must be two numbers");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(vm, makeNumber(asNumber(second) - asNumber(first)));
      break;
    }
    case OP_MULTIPLY: {
      Value first = pop(vm);
      Value second = pop(vm);
      if (!isNumber(first) || !isNumber(second)) {
        runtimeError(vm, "Operands must be two numbers");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(vm, makeNumber(asNumber(first) * asNumber(second)));
      break;
    }
    case OP_DIVIDE: {
      Value first = pop(vm);
      Value second = pop(vm);
      if (!isNumber(first) || !isNumber(second)) {
        runtimeError(vm, "Operands must be two numbers");
        return INTERPRET_RUNTIME_ERROR;
      }
      push(vm, makeNumber(asNumber(second) / asNumber(first)));
      break;
    }
    }
  }
#undef READ_BYTE
#undef READ_2BYTES
#undef READ_CONSTANT
}

InterpretResult interpret(VirtualMachine *vm, const char *source) {
  vm->stackTop = 0;
  vm->ip = 0;
  resetChunkCode(&vm->chunk);

  if (!compile(vm, source)) {
    return INTERPRET_COMPILE_ERROR;
  }

  InterpretResult result = run(vm);

  return result;
}

void push(VirtualMachine *vm, Value value) {
  vm->stack[vm->stackTop++] = value;
}

Value pop(VirtualMachine *vm) { return vm->stack[--vm->stackTop]; }
