package dgroomes.memory_leak;

import dgroomes.memory_leak.bindings.file_data;
import dgroomes.memory_leak.bindings.readfile_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class Runner {

    private static final long MAX_FILE_SIZE = 1_024 * 1_024; // 1 MiB

    public static void main(String[] args) {
        System.out.println("(Not yet fully implemented)");
        System.out.println("Let's call native code from Java! Here we go...");
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment arg = arena.allocateUtf8String("/Users/dave/repos/personal/java-foreign-function-and-memory-api-playground/memory-leak/README.md");
            MemorySegment fileData = readfile_h.read_file(arg, MAX_FILE_SIZE);
            int lines = file_data.lines$get(fileData);
            long bytes = file_data.bytes$get(fileData);
            String name = file_data.name$get(fileData).getUtf8String(0);
            System.out.printf("\tfile=%s%n\tlines=%,d%n\tbytes=%,d%n", name, lines, bytes);
        }
    }
}
