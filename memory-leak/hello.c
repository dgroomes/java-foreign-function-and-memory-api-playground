#include "hello.h"
#include <stdlib.h>
#include <string.h>

char* hello() {
    char* str = (char*) malloc(6 * sizeof(char)); // 5 characters for "hello" + 1 for '\0'
    if (str == NULL) {
        return NULL;  // Memory allocation failed
    }
    strcpy(str, "hello");
    return str;
}
