#include "readfile.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

struct file_data* read_file(char* name, long max_size) {
    struct file_data* f = (struct file_data*) malloc(sizeof(struct file_data));
    if (f == NULL) {
        return NULL;  // Memory allocation failed
    }

    // Initialize file attributes
    f->name = strdup(name);
    f->lines = 0;
    f->bytes = 0;

    // Open file
    FILE* file = fopen(name, "r");
    if (file == NULL) {
        free(f);
        return NULL;  // File open failed
    }

    // Find file size
    fseek(file, 0, SEEK_END);
    long size = ftell(file);
    fseek(file, 0, SEEK_SET);

    // Cap file size to max_size
    if (size > max_size) {
        size = max_size;
    }

    // Allocate memory for file content
    f->content = (char*) malloc(size + 1);
    if (f->content == NULL) {
        fclose(file);
        free(f);
        return NULL;  // Memory allocation failed
    }

    // Read file content into memory
    fread(f->content, 1, size, file);
    f->content[size] = '\0';  // Null-terminate the string
    f->bytes = size;

    // Count the number of lines
    if (size == 0) {
        // This is the "empty file" case.
        f->lines = 0;
    } else {
        // A non-empty file has at least one line by definition.
        f->lines = 1;
        for (int i = 0; i < size; i++) {
            if (f->content[i] == '\n') f->lines++;
        }
    }

    fclose(file);
    return f;
}

int main(int argc, char *argv[]) {
    // Check if at least one argument is passed.
    char *filename;
    if (argc > 1) {
        // Print the first argument.
        filename = argv[1];
        printf("The first argument is: %s\n", filename);
    } else {
        // Inform the user when no arguments are passed.
        printf("No arguments passed.\n");
        return 1;
    }

    struct file_data* file_data = read_file(filename, 1000000);

    printf("File name: %s\n", file_data->name);
    printf("File size: %ld bytes\n", file_data->bytes);
    printf("Number of lines: %d\n", file_data->lines);
}
