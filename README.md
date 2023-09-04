# java-foreign-function-and-memory-api

📚 Learning and exploring "off-heap" memory segments using Java's foreign memory API.


## Overview

Java's [Foreign Function & Memory API](https://openjdk.org/jeps/442) is a new and powerful feature of the platform. It's
a preview feature in Java 21 and is expected to be finalized soon thereafter. I want to learn how to use it.

In this project, we'll build a simple program that writes data (what kind of data should we pick?) into foreign segments
of memory and then searches it and summarizes it. (Maybe just a byte count?)

Note: I moved this from my other repository: <https://github.com/dgroomes/jdk-playground/>. I'll delete this note soon.


## Instructions

Follow these instructions to build and run the program.

1. Use Java 17 but have Java 21 installed
   * Java 21 is in preview. It's too new for Gradle to support it. So, use Java 17 to run Gradle tasks but have Java 21
     installed so that Gradle can delegate to Java 21's `javac` and `java` commands for compiling and running our
     program.
2. Build the program distribution:
   * ```shell
     ./gradlew installDist
     ```
3. Switch to Java 21
4. Run the program:
   * ```shell
     build/install/foreign-memory/bin/foreign-memory
     ```


## Wish List

General clean-ups, TODOs and things I wish to implement for this project:

* [x] DONE Scaffold.
* [x] DONE Write to foreign memory.
* [x] DONE Read from foreign memory.
* [ ] IN PROGRESS Do something more interesting. Can we implement a "glob matcher" program that scans a segment of foreign
  memory? We could read, let's say all classes on the classpath, extract their method and field names and store this as a
  corpus of data. Then, we can match on this data using globs like "stat*" which would match "static", "statistics", etc. 
  This is a "search" use case, so it makes the program more convincing. I think this should work, because to
  match on simple globs, we don't need to new up a `String` object, we should be able to just compare the bytes directly.
  Newing up a `String` object would cause tons of allocations which would mostly defeat the purpose of using foreign
  memory. Also, this is more interesting because strings are variable length, and we would engage different parts of the
  API.
  * DONE Enumerate classes and their fields and methods.
  * IN PROGRESS Serialize and write fixed-width "class info" data. Write a truncated class name (16 bytes) instead of the
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
  * IN PROGRESS Read from the fixed-width "class info" data. This is where the MemoryLayout should be really helpful.
  * Figure out a plan for variable sized chunks. The names related to a "class info" are variable. Some classes have many
    fields, and some have few. Some classes have long-winded field names and method names, and some have terse names.
    How do we physically lay out this data with the FFM API? I don't think we can express variable width data using the
    "structured data" offerings of the FFM API like MemoryLayout. All MemoryLayout objects are fixed width. I think the
    best bet might be model a "class info" *index* and then the actual class info data is stored in a long memory
    segment. So there's a "structured + raw" design. I'm curious to read more what the OpenJDK developers think about
    variable width stuff, I see that there are methods like `java.lang.foreign.MemorySegment.setUtf8String`.
    We need to express "shape" in a serial way. We need to do stuff like have some front matter that says
    "here's the number of fields" and then for each field, "here's the length of the field" etc.
  * Glob match on foreign memory.
* [x] DONE Move this to its own repository. `jdk-playground` isn't the right place because FFM is a library and runtime feature.
