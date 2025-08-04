#include "../lib/common.h"
#include "../lib/vm.h"
#include <stdio.h>
#include <stdlib.h>

static void repl();
static void runFile(const char *pathname);
static char *readFile(const char *pathname);

int main(int argc, const char *argv[]) {
  if (argc == 1) {
    repl();
  } else if (argc == 2) {
    runFile(argv[1]);
  } else {
    fprintf(stderr, "Usage: clox [path]\n");
    exit(2);
  }
  return 0;
}

static void repl() {
  VirtualMachine vm;
  initVM(&vm);

  char line[1024];
  while (true) {
    printf("> ");

    if (!fgets(line, sizeof(line), stdin)) {
      printf("\n");
      break;
    }
    interpret(&vm, line);
  }
  freeVM(&vm);
}

static void runFile(const char *pathname) {
  VirtualMachine vm;
  initVM(&vm);

  char *source = readFile(pathname);
  InterpretResult result = interpret(&vm, source);
  free(source);

  freeVM(&vm);

  if (result != INTERPRET_OK) {
    exit(1);
  }
}

static char *readFile(const char *pathname) {
  FILE *file = fopen(pathname, "rb");
  if (!file) {
    fprintf(stderr, "Failed to open file %s\n", pathname);
    exit(1);
  }

  fseek(file, 0L, SEEK_END);
  const size_t fileSize = ftell(file);
  rewind(file);

  char *buffer = (char *)malloc(fileSize + 1);
  if (!buffer) {
    fprintf(stderr, "Failed to malloc memory for file content %s\n", pathname);
    exit(1);
  }

  const size_t bytesRead = fread(buffer, sizeof(char), fileSize, file);
  if (bytesRead != fileSize) {
    fprintf(stderr, "Failed to read file content %s\n", pathname);
    exit(1);
  }
  buffer[bytesRead] = '\0';

  fclose(file);
  return buffer;
}
