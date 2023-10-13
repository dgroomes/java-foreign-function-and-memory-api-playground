# memory-leak

An example "memory leak" programming mistake when calling a C function from a Java program via the Foreign Function and Memory API.


## Overview

The FFM API is great because it makes "calling C code from Java easier". But, it can't make C itself any easies. As a
developer, you still bear the responsibility of managing memory allocated by C code even when you are operating from the
comfort of a memory-managed runtime like the JVM. This project demonstrates a style of memory leak that you might make
when calling C code from Java.

Note: This project is not a "hello world"-style project. You should explore the foundational concepts of the FFM API,
`jextract`, and Gradle by some other means.


## Instructions

Follow these instructions to build and run the program.

1. Use Java 21
2. Use a matching `jextract` distribution
3. Compile the C library
    * ```shell
      clang -dynamiclib -o readfile.dylib readfile.c
      ```
4. Extract the Java bindings
    * ```shell
      jextract --source \
       --output src/main/java \
       --target-package dgroomes.memory_leak.bindings \
       -I "$PWD" \
       -l "$PWD/readfile.dylib" \
      readfile.h
      ```
5. Compile the Java program distribution
    * ```shell
      ./gradlew installDist
      ```
6. Run the Java program
    * Make sure you are using Java 21.
    * ```shell
      build/install/memory-leak/bin/memory-leak
      ```
    * It should look something like this:
      ```text
      $ build/install/memory-leak/bin/memory-leak
      This program will compute the size/lines of all regular files in a given directory. But it does it in a cool way because it calls C library using the Foreign Function and Memory API!
      Enter a directory (or 'exit'):
      ```
    * Enter a directory to scan. Try `build`, `.` (current dir), `..` (parent dir), etc.
    * Check how much memory is used by the process using the following command.
    * ```shell
      ps aux | grep 'java' | grep 'memory_leak' | awk '{print $6/1024 " MiB"}'
      ```
    * For me, it was 65MiB after I scanned a few directories. We can't tell if this is just normal JVM memory usage
      or if we have a memory leak.
    * Scan some more and check the memory usage again.
    * For me, it was 117MiB. There's a memory leak! Check the code and plug the leak.


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Scaffold
* [x] DONE (but how do we deallocate the memory allocated by C? We're not in an 'Arena'. Doesn't this leak?) Implement a C function that returns a string. How to get the string into the Java program? This is where
  MemoryAddress comes in.
* [x] DONE Figure out how to clean up the memory allocated by C. There should be some good examples in <https://github.com/openjdk/jextract/blob/master/samples>.
   * Answer: just like normal systems programming, you have to free the memory yourself. I was imagining (in my naive experience),
     that "hey, the FFM API exposes an Arena, it gets clean up automatically by the try-with-resources block, and process
     memory is freed by the OS after the process is done" but there is no such magic. In theory, C libraries could use
     some fictional alternative memory allocator that has a "free all" function. And in fact, some C libraries do support
     alternative memory allocators beside `malloc`, like `jemalloc`, but that's just not a thing. So, what you have to do
     is call back into the C library which needs to expose a function that frees the memory. So, integrating to foreign
     functions absolute exposes you to classic C memory management problems. But, that's just trade off.
* [x] DONE Turn this into a 'memory-leak' subproject which shows an example memory leak. The idea is that when calling a C
  library, you still need to explicitly manage the memory allocated by the C functions. FFM API does not save you from
  this burden. Specifically, I'll implement a commandline program that reads a line-count for a file using C code. But
  the function keeps the read content in-memory, and it's up to the caller to free it. You should be able to see, in
  Activity Manager, that the process is using more and more memory. As an aside, does the JVM have any idea how much
  off-heap memory is used? I would guess not, but maybe it calls into OS functions to get total memory used?
  * DONE Rename
  * DONE Implement a C function to read a file and return a struct containing the contents (pointer of course) and line count
  * DONE Call the C function from Java
  * DONE From Java, read an entire directory
  * DONE Create a `free` helper function for the file_data struct
  * DONE Allow user to enter a directory path as a commandline argument. This is the 'read' command.
  * DONE Showcase the memory issue.
  * DONE Include the free the invocation to prove the correct way to manage the memory. 
  * NOT POSSIBLE (answer: no it can't see memory allocated from third party code like what I'm doing) Can a Java program see how much memory (including non-JVM/native) memory is used?
* [ ] Defect. My program is not counting the same bytes as `dust`. Not sure why yet.
