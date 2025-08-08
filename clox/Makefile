.PHONY = build clean

CFLAGS = -Wall
CC = gcc

LIB_SOURCES := $(shell find ./lib -name '*.c' -o -name '*.h')
TEST_SOURCES := $(shell find ./test -name '*.c' -o -name '*.h')

.PHONY: build clean test

build: $(LIB_SOURCES) src/clox.c
	$(CC) $(CFLAGS) -o out $(LIB_SOURCES) src/clox.c

test: $(LIB_SOURCES) $(TEST_SOURCES)
	$(CC) $(CFLAGS) -o out $(LIB_SOURCES) $(TEST_SOURCES) -lcunit

clean:
	rm *.o out
