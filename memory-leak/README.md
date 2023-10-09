# memory-leak

NOT YET FULLY IMPLEMENTED

An example "memory leak" programming mistake when calling a C function from a Java program via the Foreign Function and Memory API.


## Overview

The FFM API becomes especially interesting when you can use it do "substantial work" in C code that you could not do
in Java code at the same level of performance. This project aims to show a working example of that kind of use case.
I'm doubtful that it will actually demonstrate "better performance", but the architecture, concepts and code paths in
this project are what is interesting. Performance benchmarking is its own science.

This project is not a "hello world"-style project. You should explore the foundational concepts of the FFM API,
`jextract`, and Gradle by some other means.


## Instructions

Follow these instructions to build and run the program.

1. Use Java 21
2. Use a matching `jextract` distribution
3. Compile the C library
    * ```shell
      clang -dynamiclib -o hello.dylib hello.c
      ```
4. Extract the Java bindings
    * ```shell
      jextract --source \
       --output src/main/java \
       --target-package dgroomes.memory_addresses.bindings \
       -I "$PWD" \
       -l "$PWD/hello.dylib" \
      hello.h
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
      (Not yet fully implemented)
      Let's call native code from Java! Here we go...
      The C function returned 123.
      ```


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
* [ ] IN PROGRESS Turn this into a 'memory-leak' subproject which shows an example memory leak. The idea is that when calling a C
  library, you still need to explicitly manage the memory allocated by the C functions. FFM API does not save you from
  this burden. Specifically, I'll implement a commandline program that reads a line-count for a file using C code. But
  the function keeps the read content in-memory and it's up to the caller to free it. You should be able to see, in
  Activity Manager, that the process is using more and more memory. As an aside, does the JVM have any idea how much
  off-heap memory is used? I would guess not, but maybe it calls into OS functions to get total memory used?
  * DONE Rename
  * Implement a C function to read a file and return a struct containing the contents (pointer of course) and line count
  * Call the C function from Java
  * From Java, read an entire directory
  * Allow user to enter a directory path as a commandline argument. This is the 'read' command.
  * Offer a 'read-safe' command which actually frees the memory
  * (stretch) Can a Java program see how much memory (including non-JVM) memory is used?
