#ifndef clox_scanner_h
#define clox_scanner_h

typedef struct {
  const char *source;
  unsigned int current;
  unsigned int line;
} Scanner;

void initScanner(Scanner* scanner, const char *source);

#endif
