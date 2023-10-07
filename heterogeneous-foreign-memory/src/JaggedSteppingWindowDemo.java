import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

/**
 * This is a single-file Java program that demonstrates a "jagged stepping read/write window" over a {@link MemorySegment}
 * of heterogeneous data.
 * <p>
 * This program can be built and run with the `java` command using Java 21. Do so like this:
 *
 * <pre>
 *     java --enable-preview --source 21 JaggedSteppingWindowDemo.java
 * </pre>
 * <p>
 * The program output is the following:
 * <pre>
 *     Allocated 28 bytes of memory.
 *     Writing entry ProgrammingLanguage[id=1, name=C] at offset 0
 *     Writing entry ProgrammingLanguage[id=2, name=Java] at offset 8
 *     Writing entry ProgrammingLanguage[id=3, name=Go] at offset 19
 *     ID: 1, Name: C
 *     ID: 2, Name: Java
 *     ID: 3, Name: Go
 * </pre>
 * <p>
 * Let's model a conceptual "table" of data. The table is called "programming_languages" and there are two
 * columns: id and name. In SQL, we could express this table with a DDL statement like the following (Postgres syntax):
 *
 * <pre>
 *     CREATE TABLE programming_languages (
 *         id SERIAL PRIMARY KEY,
 *         name TEXT UNIQUE
 *     );
 * </pre>
 * <p>
 * If we were to model this data in-memory using a Java record class, we might express it like this:
 *
 * <pre>
 *     record ProgrammingLanguage(int id, String name) {}
 * </pre>
 * <p>
 * and then we could have a {@link ArrayList} of those objects like this:
 *
 * <pre>
 *     List<ProgrammingLanguage> programmingLanguages = new ArrayList<>();
 *     programmingLanguages.add(new ProgrammingLanguage(1, "C"));
 *     programmingLanguages.add(new ProgrammingLanguage(2, "Java"));
 *     programmingLanguages.add(new ProgrammingLanguage(2, "Go"));
 * </pre>
 * <p>
 * In summary, SQL tables and Java classes make it easy to express the conceptual shape of our data, even for
 * variable-width data like strings. This is great! But, if we have a use-case for fast collection scans over large
 * collections, or we are memory constrained, then we suffer from cache misses due to our data being scattered on the
 * heap, and we suffer from the memory overhead of object headers. For this, we can reach for the <a href="https://openjdk.org/jeps/442">Java Foreign Function & Memory API.</a>.
 * <p>
 * The FFM API offers us strong control over the physical data layout, and this gives us the gift of performance. But the
 * API doesn't quite offer the same expressive convenience. But still, how expressive can we get with the FFM API? What
 * options do we have? Can {@link VarHandle}, {@link MethodHandle}, or {@link MemoryLayout} create a nice abstraction
 * that is both expressive and performant?
 * <p>
 * Of particular challenge is that the data is variable-width. The FFM API offers an awesome API called {@link java.lang.foreign.SequenceLayout}
 * for expressing collections of data where the collection itself is of course variable width (e.g. few or many elements)
 * but the data elements themselves must be fixed width (like ints or struct-like things like 2D coordinates). I don't
 * see a way to use {@link java.lang.foreign.SequenceLayout} to express and interact with a single {@link MemorySegment}
 * that contains the "programming language" elements.
 * <p>
 * The best I can come up with is to model a "programming language" struct {@link MemoryLayout} which models only the ID
 * and the length of the programming language name. Then I write the programming language string as "loose bytes".
 * For each programming language, I repeat this process to build up the whole data set:
 *
 * <pre>
 *     1. Write the programming language ID (int) and nameLength (short) to a buffer {@link MemorySegment}. (Big/little endian handled automatically!)
 *     2. Copy the buffer {@link MemorySegment} to the larger {@link MemorySegment} with {@link MemorySegment#copy(MemorySegment, ValueLayout, long, Object, int, int)}.
 *     3. Step an "offset" counter forward by the size of the struct.
 *     4. Write the programming language name to the larger {@link MemorySegment} using {@link MemorySegment#setUtf8String(long, String)}.
 *     5. Step the "offset" counter forward by the size of the name + 1 for the size of the null terminator.
 * </pre>
 * <p>
 * The offset increments in a "jagged" way. It's not a simple linear increment. The "buffer" {@MemorySegment} segment is
 * like a convenient ruler/guide/window.
 */
public class JaggedSteppingWindowDemo {

    private final Arena arena;

    public JaggedSteppingWindowDemo(Arena arena) {
        this.arena = arena;
    }

    record ProgrammingLanguage(int id, String name) {}

    private static final List<ProgrammingLanguage> PROGRAMMING_LANGUAGES = List.of(
            new ProgrammingLanguage(1, "C"),
            new ProgrammingLanguage(2, "Java"),
            new ProgrammingLanguage(3, "Go"));

    public static void main(String[] args) {
        try (Arena arena = Arena.ofConfined()) {
            new JaggedSteppingWindowDemo(arena).run();
        }
    }

    public void run() {

        // This is a convenient abstraction that models the "programming language" struct. It only models the ID and the
        // length of the programming language name and not the actual name itself.
        StructLayout struct;
        {
            struct = MemoryLayout.structLayout(
                            ValueLayout.JAVA_INT.withName("id"),
                            ValueLayout.JAVA_SHORT.withName("nameLength"))
                    .withName("programmingLanguages");
        }

        // Var handles
        VarHandle idHandle = struct.varHandle(MemoryLayout.PathElement.groupElement("id"));
        VarHandle nameLengthHandle = struct.varHandle(MemoryLayout.PathElement.groupElement("nameLength"));

        // Compute how many bytes we need to allocate to put all the IDs, byte lengths, and names in the memory
        // segment.
        long totalBytesNeeded = PROGRAMMING_LANGUAGES.stream().mapToLong(it -> {
            var nBytes = it.name.getBytes(StandardCharsets.UTF_8).length;

            // For extra realism, let's do this data check that we would have to do in the real world.
            if (nBytes > Short.MAX_VALUE)
                throw new RuntimeException("The string is too long to express its length as a short.");

            return struct.byteSize() + nBytes + 1; // The "+ 1" is for the null terminator
        }).sum();

        // Allocate the memory segments.
        MemorySegment collection = arena.allocate(totalBytesNeeded);
        MemorySegment buffer = arena.allocate(struct);
        out.printf("Allocated %d bytes of memory.%n", totalBytesNeeded);

        // Write the data. Unfortunately, we need to manually keep track of the offset. I can't find a way to abstract
        // the raw offset tracking behind FFM APIs. I hope I can be proved wrong.
        //
        // The farthest I got was trying to define my own java.lang.foreign.MemoryLayout.PathElement but that interface
        // is sealed.
        long offset = 0;
        for (var it : PROGRAMMING_LANGUAGES) {
            out.printf("Writing entry %s at offset %d%n", it, offset);
            int nameBytesLength = it.name.getBytes(StandardCharsets.UTF_8).length;

            // Write to the struct buffer
            {
                idHandle.set(buffer, it.id);
                nameLengthHandle.set(buffer, (short) nameBytesLength);
            }

            // Copy the struct buffer to the larger collection
            {
                MemorySegment.copy(buffer, 0, collection, offset, struct.byteSize());
                offset += struct.byteSize();
            }

            // Write the string
            {
                collection.setUtf8String(offset, it.name); // I can learn a lot more if I dive deeper into setUtf8String.
                offset += nameBytesLength + 1; // +1 for the null terminator
            }
        }

        // Read the data back out.
        //
        // We read from the beginning of the overall memory segment to the end.
        offset = 0;
        while (offset < totalBytesNeeded) {
            // Read the data into the struct buffer
            MemorySegment.copy(collection, offset, buffer, 0, struct.byteSize());

            var id = (int) idHandle.get(buffer);
            short nameLength = (short) nameLengthHandle.get(buffer);
            offset += struct.byteSize();


            String name = collection.getUtf8String(offset);
            offset += nameLength + 1; // +1 for the null terminator

            out.println("ID: " + id + ", Name: " + name);
        }
    }

    /**
     * As needed for debugging, print the whole memory segment as formatted bytes.
     */
    private static void printAsHex(MemorySegment collection) {
        ByteBuffer byteBuffer = collection.asByteBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        out.println("bytes: " + bytes.length);
        // Print out the bytes in a human-readable way.
        for (byte b : bytes) {
            out.printf("%02X ", b);
        }
        out.println();
    }
}
