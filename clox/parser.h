#ifndef clox_parser_h
#define clox_parser_h

#include "scanner.h"
#include "vm.h"

typedef struct {
  Token current;
  Token previous;
  bool hasError;
} Parser;

void initParser(Parser *parser);

#endif
