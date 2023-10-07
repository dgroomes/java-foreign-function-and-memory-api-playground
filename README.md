# java-foreign-function-and-memory-api-playground

📚 Learning and exploring "off-heap" memory segments using Java's foreign memory API.


## Overview

Java's [Foreign Function & Memory API](https://openjdk.org/jeps/442) is a new and powerful feature of the platform. It's
a preview feature in Java 21 and is expected to be finalized soon thereafter. I want to learn how to use it.


## Standalone subprojects

This repository illustrates different concepts, patterns and examples via standalone subprojects. Each subproject is
completely independent of the others and do not depend on the root project. This _standalone subproject constraint_
forces the subprojects to be complete and maximizes the reader's chances of successfully running, understanding, and
re-using the code.

The subprojects include:


### `heterogeneous-foreign-memory/`

Interact with heterogeneous data (variable width) stored in `MemorySegment`s using the Foreign Function & Memory API.

See the README in [heterogeneous-foreign-memory/](heterogeneous-foreign-memory/).

### `jextract/`

Call from Java code into C code using the `jextract` tool.

See the README in [jextract/](jextract/).


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Scaffold the subprojects shape (README, settings.gradle.kts, etc)
* [x] DONE Migrate <https://github.com/dgroomes/jdk-playground/tree/main/jextract> to a subproject here
* [ ] (I don't super want to do this because I've already explored this and get it, and don't have a ready-made program)
  Create a bare-bones memory layout example. Just x,y points or something, like they talk about often in the OpenJDK. 
* [x] DONE Extract jaggedsteppingwindow project (and I guess it's ok to bundle in StringOnlyDemo because it was a stepping
  stone and tightly related)
* [ ] Create an intermediate project that passes a complex type from C to Java, like an array of strings or something.
* [x] DONE Delete the original/main example. It's not effective at showing new concepts and the performance doesn't actually work.
  I'm going to explore applications of FFM API in a different project: <https://github.com/dgroomes/java-columnar-query-engine>.
* [ ] (stretch) upcalls from C to Java, but I don't have a personal interest in that right now.
* [ ] Upgrade Gradle


## Finished Wish List Items

* [x] DONE Scaffold.
* [x] DONE Write to foreign memory.
* [x] DONE Read from foreign memory.
* [x] SKIP (Partially implemented: I'm not doing glob matching but going to keep all the work) (UPDATE: maybe forget glob matching. I like showcasing the variable width stuff, but I don't want to
  lose the fixed width component so I really want to keep the struct of "number of methods" and "number of fields". Support
  a regex search maybe on class name AND support a "find classes with greater than N methods" or "find classes with greater than N fields")
  Do something more interesting. Can we implement a "glob matcher" program that scans a segment of foreign
  memory? We could read, let's say all classes on the classpath, extract their method and field names and store this as a
  corpus of data. Then, we can match on this data using globs like "stat*" which would match "static", "statistics", etc.
  This is a "search" use case, so it makes the program more convincing. I think this should work, because to
  match on simple globs, we don't need to new up a `String` object, we should be able to just compare the bytes directly.
  Newing up a `String` object would cause tons of allocations which would mostly defeat the purpose of using foreign
  memory. Also, this is more interesting because strings are variable length, and we would engage different parts of the
  API.
  * DONE Enumerate classes and their fields and methods.
  * DONE Serialize and write fixed-width "class info" data. Write a truncated class name (16 bytes) instead of the
    hash and write the number of fields and number of methods. This keeps us in the easier territory of "fixed width"
    instead of "variable width" data but gets us much deeper into the FFM API. UPDATE: I'm committing this is in a
    semi-working state for now. I'm able to write and read a string (the class name) but with a defect. The output
    is funny. See below.
  * ```text
    14:12:05.952 [main] INFO dgroomes.foreign_memory.App - Let's explore the Java foreign memory API!
    14:12:06.592 [main] INFO dgroomes.foreign_memory.App - Sample data:
    14:12:06.593 [main] INFO dgroomes.foreign_memory.App -   name: apple.laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.593 [main] INFO dgroomes.foreign_memory.App -   name: pple.laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.593 [main] INFO dgroomes.foreign_memory.App -   name: ple.laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.593 [main] INFO dgroomes.foreign_memory.App -   name: le.laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.593 [main] INFO dgroomes.foreign_memory.App -   name: e.laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.593 [main] INFO dgroomes.foreign_memory.App -   name: .laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.594 [main] INFO dgroomes.foreign_memory.App -   name: laf.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.594 [main] INFO dgroomes.foreign_memory.App -   name: af.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.594 [main] INFO dgroomes.foreign_memory.App -   name: f.JRSUI, numberOfFields: 0, numberOfMethods: 0
    14:12:06.594 [main] INFO dgroomes.foreign_memory.App -   name: .JRSUI, numberOfFields: 0, numberOfMethods: 0
    ```
  * DONE Read from the fixed-width "class info" data. This is where the MemoryLayout should be really helpful.
  * DONE (Do the "struct header + raw content" pairs which is demonstrated in `JaggedSteppingWindowDemo.java`.
    I'm satisfied with this and especially happy to not use multiple/disparate memory segments) Figure out a plan for variable sized chunks. The names related to a "class info" are variable. Some classes have many
    fields, and some have few. Some classes have long-winded field names and method names, and some have terse names.
    How do we physically lay out this data with the FFM API? I don't think we can express variable width data using the
    "structured data" offerings of the FFM API like MemoryLayout. All MemoryLayout objects are fixed width. I think the
    best bet might be model a "class info" *index* and then the actual class info data is stored in a long memory
    segment. So there's a "structured + raw" design. I'm curious to read more what the OpenJDK developers think about
    variable width stuff, I see that there are methods like `java.lang.foreign.MemorySegment.setUtf8String`.
    We need to express "shape" in a serial way. We need to do stuff like have some front matter that says
    "here's the number of fields" and then for each field, "here's the length of the field" etc.
    * (`JaggedSteppingWindowDemo.java` shows my best attempt, which works) I'm a little hung up on this. I have an idea for doing a "two-segment" approach where the first segment is basically
      three offsets into the second segment. The first offset is the offset of the class name, the second offset
      is the offset of the first field name, and the third offset is the offset of the first method name. The field
      names and method names are delimited by the null terminator or some illegal character. That should work fine. But
      what I really want is an unadulterated segment of my data. This should be the absolute best for cache hits, and
      also it's just non-arbitrary. It is what it is. Can we express that? Let's boil it down to a single-file example
      that defines a contiguous memory segment, writes a struct (type 'X') at the start, writes a string that, and then
      writes another struct (type 'X') after that. This can't be expressed as a sequence because the string is
      variable-width. Can we still get the advantage of the MemoryLayout for struct 'X' and var handles? I think we need
      to do manual offset arithmetic. Hopefully that's the only trade-off. UPDATE: I have some working code in `JaggedSteppingWindowDemo.java`
      but it does low-level work in a way that we are not interested in maintaining. The effect of the code in terms of
      physical memory usage is what we want, but the source code is not. Can we apply the MemoryLayout abstraction to
      this code? UPDATE: No we need to do manual offset arithmetic. Not the end of the world.
  * DONE Write the full class names to the memory segment since we solved the variable-width problem.
  * Glob match on foreign memory.
* [x] DONE Move this to its own repository. `jdk-playground` isn't the right place because FFM is a library and runtime feature.
