package dgroomes.foreign_memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.foreign.*;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
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
            //
            // Specifically, we define a sequence of "class info" structs. Notice how we have the option of using
            // structs in Java (well by way of some indirection). This option is a great luxury! By necessity, the
            // index is fixed width. Later we will create a big memory segment to store the class fields and methods
            // because those are variable width. We have to figure that out later.
            SequenceLayout classInfoSequenceLayout;
            {
                StructLayout classInfoStructLayout = MemoryLayout.structLayout(
                        MemoryLayout.sequenceLayout(32, ValueLayout.JAVA_BYTE).withName("name"), // maybe this should be an address? Strugging. Can't get a varhandle to a sequence.
                        ValueLayout.JAVA_SHORT.withName("numberOfFields"),
                        ValueLayout.JAVA_SHORT.withName("numberOfMethods")).withName("classInfoStructLayout");

                classInfoSequenceLayout = MemoryLayout.sequenceLayout(elementCount, classInfoStructLayout).withName("classInfoSequenceLayout");
            }

            // Get "accessor" objects for the data. These are VarHandles and MethodHandles.
            MethodHandle nameHandle;
            VarHandle numberOfFieldsHandle;
            VarHandle numberOfMethodsHandle;
            {
                nameHandle = classInfoSequenceLayout.sliceHandle(
                        PathElement.sequenceElement(),
                        PathElement.groupElement("name"));

                numberOfFieldsHandle = classInfoSequenceLayout.varHandle(PathElement.sequenceElement(), PathElement.groupElement("numberOfFields"));
                numberOfMethodsHandle = classInfoSequenceLayout.varHandle(PathElement.sequenceElement(), PathElement.groupElement("numberOfMethods"));
            }

            // Write the data.
            log.info("Let's write the data to a memory segment...");
            MemorySegment segment;
            {
                segment = arena.allocate(classInfoSequenceLayout);
                for (int i = 0; i < classInfoList.size(); i++) {
                    var classInfo = classInfoList.get(i);

                    String classNameTruncated;
                    {
                        var className = classInfo.className();
                        // Note: the class name is truncated to 31 bytes because the 32th bit is reserved for the null
                        // terminator.
                        classNameTruncated = StringUtil.getLimitedByteString(className, 31);
                    }

                    var classNameSegment = (MemorySegment) nameHandle.invoke(segment, i);
                    classNameSegment.setUtf8String(0, classNameTruncated);

                    numberOfFieldsHandle.set(segment, (long) i, (short) classInfo.fieldNames().size());
                    numberOfMethodsHandle.set(segment, (long) i, (short) classInfo.methodNames().size());
                }
            }
            log.info("Done. Wrote {} elements to the memory segment", elementCount);
            log.info("");

            log.info("Let's read a sample of the data back out of the memory segment...");
            for (int baseIndex = 0, multipliedIndex = baseIndex;
                 baseIndex < SAMPLE_READ_COUNT && multipliedIndex < elementCount;
                 baseIndex++, multipliedIndex = baseIndex * 200) {

                var classNameSegment = (MemorySegment) nameHandle.invoke(segment, multipliedIndex);
                var name = classNameSegment.getUtf8String(0);
                var numberOfFields = (short) numberOfFieldsHandle.get(segment, multipliedIndex);
                var numberOfMethods = (short) numberOfMethodsHandle.get(segment, multipliedIndex);
                log.info("  name: {}, numberOfFields: {}, numberOfMethods: {}", name, numberOfFields, numberOfMethods);
            }
        }
    }
}



