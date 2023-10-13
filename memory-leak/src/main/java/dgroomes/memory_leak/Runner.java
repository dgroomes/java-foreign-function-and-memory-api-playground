package dgroomes.memory_leak;

import dgroomes.memory_leak.bindings.file_data;
import dgroomes.memory_leak.bindings.readfile_h;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class Runner {

    private static final long SAMPLE_CONTENT_LIMIT = 1_024 * 1_024; // 1 MiB

    public static void main(String[] args) {
        String directory = "/Users/dave/repos/personal/java-foreign-function-and-memory-api-playground/memory-leak";
        System.out.printf("Computing (partly in Java, partly in C) the size/lines of all regular files in the directory '%s' ...%n", directory);

        class FileSummary {
            int lines;
            long bytes;
        }

        var fileSummary = new FileSummary();

        try (Arena arena = Arena.ofConfined()) {
            Files.walkFileTree(Path.of(directory), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    if (!attrs.isRegularFile()) return FileVisitResult.CONTINUE;

                    var pathName = path.toAbsolutePath().normalize().toString();

                    MemorySegment fileNameArg = arena.allocateUtf8String(pathName);

                    // Invoke the C function
                    MemorySegment fileData = readfile_h.read_file(fileNameArg, SAMPLE_CONTENT_LIMIT);

                    // Check for success. The function returns a null pointer on failure.
                    if (fileData.equals(MemorySegment.NULL)) {
                        System.out.printf("Something went wrong while reading the file '%s'%n", pathName);
                        return FileVisitResult.CONTINUE;
                    }

                    // Extract the data from the C struct
                    fileSummary.lines += file_data.lines$get(fileData);
                    fileSummary.bytes += file_data.bytes$get(fileData);

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Something went wrong while walking the file tree", e);
        }

        System.out.printf("Found %,d lines and %,d bytes in the files in the directory '%s'%n", fileSummary.lines, fileSummary.bytes, directory);
    }
}
