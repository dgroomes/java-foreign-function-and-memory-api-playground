package dgroomes.foreign_memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
//import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.List;

/**
 * See the README for more information.
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        log.info("Let's explore the Java foreign memory API!");
        new App().run();
    }

    public void run() {
        // An arena is like a managed container of off-heap memory. It will be automatically freed after the try block
        // ends thanks to the 'try-with-resources' statement.
        try (Arena arena = Arena.ofConfined()) {
            List<ClassScanner.ClassInfo> classInfoList = ClassScanner.scanClasses().subList(20, 30); // Limit to a small subset for sanity/demo
            int elementCount = classInfoList.size();

            // Define the memory layout of the "class info" data structure. This is fixed width. We don't actually
            // store the class fields and methods here because those are variable. We have to figure that out later.
            SequenceLayout classInfoSequenceLayout;
            {
                StructLayout classInfoStructLayout = MemoryLayout.structLayout(
                        MemoryLayout.sequenceLayout(16, ValueLayout.JAVA_BYTE).withName("name"), // maybe this should be an address? Strugging. Can't get a varhandle to a sequence.
                        ValueLayout.JAVA_SHORT.withName("numberOfFields"),
                        ValueLayout.JAVA_SHORT.withName("numberOfMethods")).withName("classInfoStructLayout");

                classInfoSequenceLayout = MemoryLayout.sequenceLayout(elementCount, classInfoStructLayout).withName("classInfoSequenceLayout");
            }

            // Get a VarHandle for the fields.
//            MethodHandle nameHandle;
            VarHandle numberOfFieldsHandle;
            VarHandle numberOfMethodsHandle;
            {
                // You can't get a VarHandle on a sequence, like a sequence of BYTE value layouts like I'm trying to do
                // with the following commented out line. I get the following error:
                //
                //   java.lang.IllegalArgumentException: Path does not select a value layout
                //
                // I think I need a MethodHandle that can produce the byte offset for the first byte in the sequence.
                // And then client code can maybe use some "copy" method of the FFM API to copy in the bytes? I'll re-use
                // some local byte array to not do any allocations?
//                nameHandle = classInfoSequenceLayout.varHandle(PathElement.sequenceElement(), PathElement.sequenceElement() PathElement.groupElement("name"));
                // TODO This needs to be sequence-within-sequence path... I think? Idk the variable width stuff is hard.
                // I need an understanding of the fixed width stuff first.
//                nameHandle = classInfoSequenceLayout.sliceHandle(
//                        PathElement.sequenceElement(),
//                        PathElement.groupElement("name"));

                numberOfFieldsHandle = classInfoSequenceLayout.varHandle(PathElement.sequenceElement(), PathElement.groupElement("numberOfFields"));
                numberOfMethodsHandle = classInfoSequenceLayout.varHandle(PathElement.sequenceElement(), PathElement.groupElement("numberOfMethods"));
            }

            // Populate the "class info" structs.
            MemorySegment segment;
            {
                segment = arena.allocate(classInfoSequenceLayout);
                for (int i = 0; i < classInfoList.size(); i++) {
                    ClassScanner.ClassInfo classInfo = classInfoList.get(i);
//                    String className = classInfo.className();
//                    // Note: the class name is truncated to 15 bytes because the 16th bit is reserved for the null
//                    // terminator (I think).
//                    String classNameTruncated = StringUtil.getLimitedByteString(className, 15);
//                    MemorySegment classNameSegment;
//                    try {
//                        classNameSegment = (MemorySegment) nameHandle.invoke(segment, i);
//                    } catch (Throwable e) {
//                        throw new RuntimeException("Failed to invoke the method handle for the classname. This is almost definitely because I don't understand the API yet", e);
//                    }
//                    classNameSegment.setUtf8String(0, classNameTruncated);

                    numberOfFieldsHandle.set(segment, (long) i, (short) classInfo.fieldNames().size());
                    numberOfMethodsHandle.set(segment, (long) i, (short) classInfo.methodNames().size());
                }
            }

            // Read a sample of class infos from the memory segment.
            log.info("Sample data: ");
            for (int i = 0; i < elementCount; i++) {
//                MemorySegment classNameSegment;
//                try {
//                    classNameSegment = (MemorySegment) nameHandle.invoke(segment, i);
//                } catch (Throwable e) {
//                    throw new RuntimeException("Failed to invoke the method handle for the classname. This is almost definitely because I don't understand the API yet", e);
//                }
//                var name = classNameSegment.getUtf8String(i);
                var numberOfFields = (short) numberOfFieldsHandle.get(segment, i);
                var numberOfMethods = (short) numberOfMethodsHandle.get(segment, i);
//                log.info("  name: {}, numberOfFields: {}, numberOfMethods: {}", name, numberOfFields, numberOfMethods);
                log.info("  numberOfFields: {}, numberOfMethods: {}", numberOfFields, numberOfMethods);
            }
        }
    }
}



