// Generated by jextract

package dgroomes.memory_leak.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
public class readfile_h  {

    public static final OfByte C_CHAR = JAVA_BYTE;
    public static final OfShort C_SHORT = JAVA_SHORT;
    public static final OfInt C_INT = JAVA_INT;
    public static final OfLong C_LONG = JAVA_LONG;
    public static final OfLong C_LONG_LONG = JAVA_LONG;
    public static final OfFloat C_FLOAT = JAVA_FLOAT;
    public static final OfDouble C_DOUBLE = JAVA_DOUBLE;
    public static final AddressLayout C_POINTER = RuntimeHelper.POINTER;
    public static MethodHandle read_file$MH() {
        return RuntimeHelper.requireNonNull(constants$0.const$6,"read_file");
    }
    /**
     * {@snippet :
     * struct file_data* read_file(char* name, long max_size);
     * }
     */
    public static MemorySegment read_file(MemorySegment name, long max_size) {
        var mh$ = read_file$MH();
        try {
            return (java.lang.foreign.MemorySegment)mh$.invokeExact(name, max_size);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
    public static MethodHandle free_file_data$MH() {
        return RuntimeHelper.requireNonNull(constants$1.const$1,"free_file_data");
    }
    /**
     * {@snippet :
     * void free_file_data(struct file_data* f);
     * }
     */
    public static void free_file_data(MemorySegment f) {
        var mh$ = free_file_data$MH();
        try {
            mh$.invokeExact(f);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}


