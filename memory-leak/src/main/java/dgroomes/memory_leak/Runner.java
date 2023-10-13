package dgroomes.memory_leak;

import dgroomes.memory_leak.bindings.file_data;
import dgroomes.memory_leak.bindings.readfile_h;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.lang.System.out;

/**
 * See the README for more information.
 */
public class Runner {

    private static final long SAMPLE_CONTENT_LIMIT = 1_024 * 1_024; // 1 MiB
    private final Arena arena;
    private final BufferedReader reader;

    public Runner(Arena arena, BufferedReader reader) {
        this.arena = arena;
        this.reader = reader;
    }

    static class FileSummary {
        int lines;
        long bytes;
    }

    public static void main(String[] args) throws IOException {
        out.printf("This program will compute the size/lines of all regular files in a given directory. But it does it in a cool way because it calls a C library using the Foreign Function and Memory API!%n");

        /*
        Note: the Arena class is super helpful in freeing foreign memory that's allocated by the Java program, but it is
        completely unaware of the memory allocated by the foreign C function.
        */
        try (Arena arena = Arena.ofConfined()) {
            try (var reader = new BufferedReader(new InputStreamReader(System.in))) {
                var runner = new Runner(arena, reader);
                runner.run();
            }
        }
    }

    public void run() throws IOException {
        while (true) {
            out.print("Enter a directory (or 'exit'): ");

            // Read and validate the directory
            String command = reader.readLine().trim();

            if ("exit".equalsIgnoreCase(command)) {
                out.println("Exiting...");
                break;
            }

            var dir = new File(command);

            if (!dir.exists() || !dir.isDirectory()) {
                out.println("Invalid directory. Try again.");
                continue;
            }

            var fileSummary = new FileSummary();
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (!attrs.isRegularFile()) return FileVisitResult.CONTINUE;

                    var pathName = path.toAbsolutePath().normalize().toString();

                    MemorySegment fileNameArg = arena.allocateUtf8String(pathName);

                    // Invoke the C function
                    MemorySegment fileData = readfile_h.read_file(fileNameArg, SAMPLE_CONTENT_LIMIT);

                    // Check for success. The function returns a null pointer on failure.
                    if (fileData.equals(MemorySegment.NULL)) {
                        out.printf("Something went wrong while reading the file '%s'%n", pathName);
                        return FileVisitResult.CONTINUE;
                    }

                    // Extract the data from the C struct
                    fileSummary.lines += file_data.lines$get(fileData);
                    fileSummary.bytes += file_data.bytes$get(fileData);

                    // Now that we've extracted the data we need from the C struct, we need to free the memory related
                    // to the struct. But, this line is purposely commented out to demonstrate the memory leak.
                    // readfile_h.free_file_data(fileData);

                    return FileVisitResult.CONTINUE;
                }
            });

            out.printf("Found %,d lines and %,d bytes in the files in the directory '%s'%n", fileSummary.lines, fileSummary.bytes, dir);

            // Normally, you would just let the JVM do garbage collection on its own, but we need to factor out the
            // variability of JVM memory usage so that it's more clear that the C function is leaking memory.
            System.gc();
        }
    }
}
