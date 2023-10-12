#ifndef READFILE_H
#define READFILE_H

struct file_data {
    int lines;
    long bytes;
    char* name;
    char* content;
};

/**
 * Read a text file from disk up to a maximum size. This gathers the file content and metadata like the number of lines
 * and size in bytes.
 *
 * Note that the function allocates memory for the file_data and its content. Therefore, it's the caller's
 * responsibility to free these later.
 *
 * @param name Name of the file to read
 * @param max_size Maximum size in bytes to read from the file
 * @return A pointer to a file_data struct, or NULL if an error occurs.
 */
struct file_data* read_file(char* name, long max_size);

#endif
