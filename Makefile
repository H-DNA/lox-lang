JUNIT_JAR := vendors/junit-platform-console-standalone-1.11.3.jar
DIR := com/lox
BUILD_DIR := build
TEST_DIR := test

SOURCES := $(wildcard $(DIR)/**/*.java) $(wildcard $(DIR)/*.java)
TESTS := $(wildcard $(TEST_DIR)/*.java)
CLASSES := $(addprefix $(BUILD_DIR)/, $(SOURCES:.java=.class))

JAVA_OPTIONS := -Werror

default: $(SOURCES)
	@ mkdir -p $(BUILD_DIR)
	@ javac -d $(BUILD_DIR) $(SOURCES)

test: $(SOURCES) $(JUNIT_JAR) $(TESTS)
	@ mkdir -p $(BUILD_DIR)
	@ javac -d $(BUILD_DIR) $(SOURCES) $(TESTS) -cp $(JUNIT_JAR)
	@ java -jar $(JUNIT_JAR) -cp $(BUILD_DIR) --select-class com.lox.ScannerTest

.PHONY: default test
