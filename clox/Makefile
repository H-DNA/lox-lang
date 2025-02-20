.PHONY = build clean

CFLAGS = -Wall
CC = gcc

SOURCES := $(shell find . -name '*.c' -o -name '*.h')

build: $(SOURCES)
	$(CC) $(CFLAGS) -o out *.c

clean:
	rm *.o out
