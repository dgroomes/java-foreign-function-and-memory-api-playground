import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;
import java.util.List;

/**
 * This is a single-file Java program for exploring reading and writing strings using the Java foreign memory API.
 * It can be built and run with the `java` command using Java 21. Do so like this:
 *
 * <pre>
 *     java --enable-preview --source 21 StringOnlyDemo.java
 * </pre>
 */
public class StringOnlyDemo {

    public static void main(String[] args) throws Throwable {
        System.out.println("Let's explore how to read and write strings using the Java foreign memory API!");
        new StringOnlyDemo().run();
    }

    public void run() throws Throwable {
        try (Arena arena = Arena.ofConfined()) {
            List<String> fiveCharacterAsciiMessages = List.of(
                    "hello",
                    "there",
                    "world");
            int elementCount = fiveCharacterAsciiMessages.size();

            // The ASCII messages are 5 bytes, but we need 6 bytes the for the null terminator.
            var messageLayout = MemoryLayout.sequenceLayout(6, ValueLayout.JAVA_BYTE).withName("message");
            SequenceLayout messagesLayout = MemoryLayout.sequenceLayout(elementCount, messageLayout).withName("messagesSequenceLayout");

            MethodHandle messageSequenceHandler = messagesLayout.sliceHandle(
                    PathElement.sequenceElement());

            System.out.println("Let's create a memory segment and write to it...");
            MemorySegment segment = arena.allocate(messagesLayout);
            for (int i = 0; i < fiveCharacterAsciiMessages.size(); i++) {
                var message = fiveCharacterAsciiMessages.get(i);
                System.out.printf("Writing message: %s%n", message);
                MemorySegment messageSegment = (MemorySegment) messageSequenceHandler.invoke(segment, i);
                messageSegment.setUtf8String(0, message);
            }

            System.out.println("Let's read the data back from the memory segment...");
            for (int i = 0; i < elementCount; i++) {
                MemorySegment messageSegment;
                messageSegment = (MemorySegment) messageSequenceHandler.invoke(segment, i);
                var message = messageSegment.getUtf8String(0);
                System.out.println(message);
            }
        }
    }
}
