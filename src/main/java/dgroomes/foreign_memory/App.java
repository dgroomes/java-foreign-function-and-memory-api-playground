package dgroomes.foreign_memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * See the README for more information.
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    public static final int SAMPLE_READ_COUNT = 30;

    public static void main(String[] args) throws Throwable {
        log.info("Let's explore the Java foreign memory API!");
        new App().run();
    }

    public void run() throws Throwable {
        // An arena is like a managed container of off-heap memory. It will be automatically freed after the try block
        // ends thanks to the 'try-with-resources' statement.
        try (Arena arena = Arena.ofConfined()) {

            // Fetch the source domain data.
            List<ClassScanner.ClassInfo> classInfoList = ClassScanner.scanClasses();
            int elementCount = classInfoList.size();

            // Define the domain data structures in terms of MemoryLayout objects. MemoryLayout is a core component of
            // the Foreign Memory API.
            StructLayout structLayout = MemoryLayout.structLayout(
                    ValueLayout.JAVA_SHORT.withName("classNameLength"),
                    ValueLayout.JAVA_SHORT.withName("numberOfFields"),
                    ValueLayout.JAVA_SHORT.withName("numberOfMethods")).withName("classInfoStructLayout");

            // Compute how many bytes we need to allocate to put all the class names, byte lengths, and counts in the
            // memory segment.
            long totalBytesNeeded = classInfoList.stream().mapToLong(it -> {
                var classNameBytes = it.className().getBytes(StandardCharsets.UTF_8).length;

                // For extra realism, let's do this data check that we would have to do in the real world.
                if (classNameBytes > Short.MAX_VALUE)
                    throw new RuntimeException("The string is too long to express its length as a short.");

                return structLayout.byteSize() + classNameBytes + 1; // The "+ 1" is for the null terminator
            }).sum();

            // Get "accessor" objects for the data.
            VarHandle classNameLengthHandle = structLayout.varHandle(PathElement.groupElement("classNameLength"));
            VarHandle numberOfFieldsHandle = structLayout.varHandle(PathElement.groupElement("numberOfFields"));
            VarHandle numberOfMethodsHandle = structLayout.varHandle(PathElement.groupElement("numberOfMethods"));

            // Allocate and write the memory segments.
            log.info("Let's write the data to a memory segment...");
            MemorySegment overallSegment;
            MemorySegment structBuffer;
            {
                overallSegment = arena.allocate(totalBytesNeeded);
                log.info("Allocated %,d bytes of memory.".formatted(totalBytesNeeded));
                structBuffer = arena.allocate(structLayout);

                long offset = 0;
                for (ClassScanner.ClassInfo classInfo : classInfoList) {
                    int classNameBytesLength = classInfo.className().getBytes(StandardCharsets.UTF_8).length;

                    // Write to the struct buffer
                    {
                        classNameLengthHandle.set(structBuffer, (short) classNameBytesLength);
                        numberOfFieldsHandle.set(structBuffer, (short) classInfo.fieldNames().size());
                        numberOfMethodsHandle.set(structBuffer, (short) classInfo.methodNames().size());
                    }

                    // Copy the struct buffer (fixed width data) to the larger memory segment
                    {
                        MemorySegment.copy(structBuffer, 0, overallSegment, offset, structLayout.byteSize());
                        offset += structLayout.byteSize();
                    }

                    // Write the class name (variable width data) to the larger memory segment
                    {
                        overallSegment.setUtf8String(offset, classInfo.className());
                        offset += classNameBytesLength + 1; // +1 for the null terminator
                    }
                }
            }
            log.info("Done. Wrote %,d elements to the memory segment".formatted(elementCount));
            log.info("");

            log.info("Let's read a sample of the data back out of the memory segment...");
            {
                long offset = 0;
                for (int elementsRead = 0; elementsRead < SAMPLE_READ_COUNT && offset < totalBytesNeeded; elementsRead++) {

                    // Read the data into the struct buffer (isn't this needlessly expensive? maybe not because it's so
                    // small, but I don't know.)
                    MemorySegment.copy(overallSegment, offset, structBuffer, 0, structLayout.byteSize());

                    // Read the struct (fixed width) data
                    var classNameLength = (short) classNameLengthHandle.get(structBuffer);
                    var numberOfFields = (short) numberOfFieldsHandle.get(structBuffer);
                    var numberOfMethods = (short) numberOfMethodsHandle.get(structBuffer);
                    offset += structLayout.byteSize();

                    // Read the class name (variable width) data
                    String className = overallSegment.getUtf8String(offset);
                    offset += classNameLength + 1; // +1 for the null terminator

                    log.info("  className: {}, numberOfFields: {}, numberOfMethods: {}", className, numberOfFields, numberOfMethods);
                }
            }
        }
    }
}



