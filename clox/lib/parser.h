#ifndef clox_parser_h
#define clox_parser_h

#include "scanner.h"
#include "vm.h"

typedef struct {
  Token current;
  bool hasError;
  Scanner *scanner;
  Chunk *chunk;
} Parser;

void initParser(Parser *parser, Scanner *scanner, VirtualMachine *vm);

void parse(Parser *parser);

#endif
