# heterogeneous-foreign-memory

Interact with heterogeneous data (variable width) stored in `MemorySegment`s using the Foreign Function & Memory API.


## Overview

A main component of the Java [Foreign Function & Memory API](https://openjdk.java.net/jeps/442) is structured access
to foreign memory. In other words, instead of "loosely accessing" bits of memory, the FFM API offers abstractions like
`MemoryLayout` to give a structured view of the memory, along with accessor methods, so that you can "get the X
component of Point(x,y)" so to speak. This is great. But, what do we do when the data is variable width? What do we do
when the data has strings? The demo programs in this subproject show the best I could come up with about how to use the
FFM API to access heterogeneous data (like strings).


## Instructions

1. Use Java 21
2. Run `StringOnlyDemo.java`
   * ```shell
     java --enable-preview --source 21 src/StringOnlyDemo.java
     ```
3. Run `JaggedSteppingWindowDemo.java`
   * ```shell
     java --enable-preview --source 21 src/JaggedSteppingWindowDemo.java
     ```
