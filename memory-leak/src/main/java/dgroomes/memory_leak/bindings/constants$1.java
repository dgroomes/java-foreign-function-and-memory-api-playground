// Generated by jextract

package dgroomes.memory_leak.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
final class constants$1 {

    // Suppresses default constructor, ensuring non-instantiability.
    private constants$1() {}
    static final FunctionDescriptor const$0 = FunctionDescriptor.ofVoid(
        RuntimeHelper.POINTER
    );
    static final MethodHandle const$1 = RuntimeHelper.downcallHandle(
        "free_file_data",
        constants$1.const$0
    );
}

