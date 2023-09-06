import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This is a single-file Java program for exploring how to use MemoryLayout objects on a heterogeneous segment of
 * memory. Is this possible?
 * <p>
 * This program can be built and run with the `java` command using Java 21. Do so like this:
 *
 * <pre>
 *     java --enable-preview --source 21 OffsetArithmeticForMemoryLayoutsDemo.java
 * </pre>
 *
 * The program output is the following:
 * <pre>
 *     Allocated 32 bytes of memory.
 *     Writing entry 0 at offset 0
 *     Writing entry 1 at offset 8
 *     Writing entry 2 at offset 19
 *     bytes: 32
 *     ID: 0, Name: C
 *     ID: 1, Name: Java
 *     ID: 2, Name: Python
 * </pre>
 */
public class OffsetArithmeticForMemoryLayoutsDemo {


    private static final List<String> PROGRAMMING_LANGUAGE_NAMES = List.of(
            "C",
            "Java",
            "Python");
    private final Arena arena;

    public OffsetArithmeticForMemoryLayoutsDemo(Arena arena) {
        this.arena = arena;
    }

    public static void main(String[] args) throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            new OffsetArithmeticForMemoryLayoutsDemo(arena).run();
        }
    }

    public void run() {
        // Let's model a simple table of data. The table is called "programming_languages" and there are two
        // columns: id and name. In SQL, we could express this conceptual model in a one-to-one way using a column
        // of type "int" and a column of type "text". In the FFM API, we have stronger constraints. We can express
        // a memory layout element of type int, but we can't express a memory layout element of type text because
        // the names are variable width. So, let's express the length of the name as a memory layout element of type
        // short, and then use that to do some offset arithmetic to get to the next entry.
        var programmingLanguagesStruct = MemoryLayout.structLayout(
                ValueLayout.JAVA_INT.withName("id"),
                ValueLayout.JAVA_SHORT.withName("stringLength")).withName("programmingLanguages");

        // Get them as UTF-8 bytes
        List<byte[]> programmingLanguageNamesAsBytes = PROGRAMMING_LANGUAGE_NAMES.stream()
                .map(s -> s.getBytes(StandardCharsets.UTF_8)).toList();

        // Compute how many bytes we need to allocate to put all the IDs, byte lengths, and names in the memory
        // segment.
        long totalBytesNeeded = 0;
        for (byte[] programmingLanguageNamesAsByte : programmingLanguageNamesAsBytes) {
            totalBytesNeeded += ValueLayout.JAVA_INT.byteSize();
            totalBytesNeeded += ValueLayout.JAVA_SHORT.byteSize();

            var bytesLength = programmingLanguageNamesAsByte.length;
            if (bytesLength > Short.MAX_VALUE)
                throw new RuntimeException("The string is too long to fit in the memory segment!");
            totalBytesNeeded += bytesLength + 1; // +1 for the null terminator
        }

        // Allocate the memory segment. Because we have a heterogeneous memory layout, we can only align on the lowest
        // common denominator, which is a byte. I think this is the same as expressing no alignment at all.
//        MemorySegment overallSegment = arena.allocate(totalBytesNeeded, ValueLayout.JAVA_BYTE.byteAlignment());
        MemorySegment overallSegment = arena.allocate(totalBytesNeeded, 1);
        System.out.println("Allocated " + totalBytesNeeded + " bytes of memory.");

        // Write the data. We need to manually keep track of the offset. (Although I think maybe there is a way to
        // get some more leverage out of segments or method handles for this).
        long offset = 0;
        for (int i = 0; i < programmingLanguageNamesAsBytes.size(); i++) {
            System.out.println("Writing entry " + i + " at offset " + offset);

            // This is silly, but I'm just trying to get something working. There is a better way to do this.
            // Also this is super fragile because I'm hard coding the endianness.
            overallSegment.set(ValueLayout.JAVA_BYTE, offset++, (byte) i);
            overallSegment.set(ValueLayout.JAVA_BYTE, offset++, (byte) (i >> 8));
            overallSegment.set(ValueLayout.JAVA_BYTE, offset++, (byte) (i >> 16));
            overallSegment.set(ValueLayout.JAVA_BYTE, offset++, (byte) (i >> 24));

            byte[] bytes = programmingLanguageNamesAsBytes.get(i);
            overallSegment.set(ValueLayout.JAVA_BYTE, offset++, (byte) bytes.length);
            overallSegment.set(ValueLayout.JAVA_BYTE, offset++, (byte) (bytes.length >> 8));

            String str = new String(bytes);  // this is silly to turn it into a string just to turn it back into bytes
            overallSegment.setUtf8String(offset, str);
            offset += bytes.length + 1; // +1 for the null terminator
        }

        // Read the whole memory segment as bytes
        ByteBuffer byteBuffer = overallSegment.asByteBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        System.out.println("bytes: " + bytes.length);
        // Print out the bytes in a human-readable way.
//        for (byte b : bytes) {
//            System.out.printf("%02X ", b);
//        }
//        System.out.println();

        // Read the data back out.
        //
        // We read from the beginning of the overall memory segment to the end.
        offset = 0;
        while (offset < totalBytesNeeded) {
            // Read the ID (int) one byte at a time.
            int id = 0;
            id += overallSegment.get(ValueLayout.JAVA_BYTE, offset++);
            id += overallSegment.get(ValueLayout.JAVA_BYTE, offset++) << 8;
            id += overallSegment.get(ValueLayout.JAVA_BYTE, offset++) << 16;
            id += overallSegment.get(ValueLayout.JAVA_BYTE, offset++) << 24;

            // Read the string length (short) one byte at a time.
            short stringLength = 0;
            stringLength += overallSegment.get(ValueLayout.JAVA_BYTE, offset++);
            stringLength += (short) (overallSegment.get(ValueLayout.JAVA_BYTE, offset++) << 8);

            String name = overallSegment.getUtf8String(offset);
            offset += stringLength + 1; // +1 for the null terminator

            System.out.println("ID: " + id + ", Name: " + name);
        }
    }
}
