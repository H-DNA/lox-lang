DIR := com/lox
BUILD_DIR := build

SOURCES := $(wildcard $(DIR)/*.java)
CLASSES := $(addprefix $(BUILD_DIR)/, $(SOURCES:.java=.class))

JAVA_OPTIONS := -Werror

default: $(SOURCES)
	@ mkdir -p $(BUILD_DIR)
	@ javac -d $(BUILD_DIR) $(SOURCES)


.PHONY: default
