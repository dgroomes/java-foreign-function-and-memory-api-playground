// Generated by jextract

package dgroomes.memory_leak.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$0 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$0() {}
    static final StructLayout const$0 = MemoryLayout.structLayout(
        JAVA_INT.withName("lines"),
        MemoryLayout.paddingLayout(4),
        JAVA_LONG.withName("bytes"),
        RuntimeHelper.POINTER.withName("name"),
        RuntimeHelper.POINTER.withName("content")
    ).withName("file_data");
    static final VarHandle const$1 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("lines"));
    static final VarHandle const$2 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("bytes"));
    static final VarHandle const$3 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("name"));
    static final VarHandle const$4 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("content"));
    static final FunctionDescriptor const$5 = FunctionDescriptor.of(RuntimeHelper.POINTER,
        RuntimeHelper.POINTER,
        JAVA_LONG
    );
    static final MethodHandle const$6 = RuntimeHelper.downcallHandle(
        "read_file",
        constants$0.const$5
    );
}


