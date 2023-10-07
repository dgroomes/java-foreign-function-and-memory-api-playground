# memory-addresses

NOT YET FULLY IMPLEMENTED

An intermediate FFM example that uses the `MemoryAddress` class to describe the result of a sort operation from C code.


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
      build/install/memory-addresses/bin/memory-addresses
      ```
    * It should look something like this:
      ```text
      $ build/install/memory-addresses/bin/memory-addresses
      (Not yet fully implemented)
      Let's call native code from Java! Here we go...
      The C function returned 123.
      ```


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Scaffold
* [x] DONE (but how do we deallocate the memory allocated by C? We're not in an 'Arena'. Doesn't this leak?) Implement a C function that returns a string. How to get the string into the Java program? This is where
  MemoryAddress comes in.
* [ ] Implement a C function that sorts an array of strings, in a way that works with the FFM API.
