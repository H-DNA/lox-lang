.PHONY = build clean

CFLAGS = -Wall
CC = gcc

LIB_SOURCES := $(shell find ./lib -name '*.c' -o -name '*.h')

build: $(LIB_SOURCES) src/clox.c
	$(CC) $(CFLAGS) -o out lib/*.c src/clox.c

clean:
	rm *.o out
