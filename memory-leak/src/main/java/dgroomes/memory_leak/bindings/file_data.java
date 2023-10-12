// Generated by jextract

package dgroomes.memory_leak.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
/**
 * {@snippet :
 * struct file_data {
 *     int lines;
 *     long bytes;
 *     char* name;
 *     char* content;
 * };
 * }
 */
public class file_data {

    public static MemoryLayout $LAYOUT() {
        return constants$0.const$0;
    }
    public static VarHandle lines$VH() {
        return constants$0.const$1;
    }
    /**
     * Getter for field:
     * {@snippet :
     * int lines;
     * }
     */
    public static int lines$get(MemorySegment seg) {
        return (int)constants$0.const$1.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * int lines;
     * }
     */
    public static void lines$set(MemorySegment seg, int x) {
        constants$0.const$1.set(seg, x);
    }
    public static int lines$get(MemorySegment seg, long index) {
        return (int)constants$0.const$1.get(seg.asSlice(index*sizeof()));
    }
    public static void lines$set(MemorySegment seg, long index, int x) {
        constants$0.const$1.set(seg.asSlice(index*sizeof()), x);
    }
    public static VarHandle bytes$VH() {
        return constants$0.const$2;
    }
    /**
     * Getter for field:
     * {@snippet :
     * long bytes;
     * }
     */
    public static long bytes$get(MemorySegment seg) {
        return (long)constants$0.const$2.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * long bytes;
     * }
     */
    public static void bytes$set(MemorySegment seg, long x) {
        constants$0.const$2.set(seg, x);
    }
    public static long bytes$get(MemorySegment seg, long index) {
        return (long)constants$0.const$2.get(seg.asSlice(index*sizeof()));
    }
    public static void bytes$set(MemorySegment seg, long index, long x) {
        constants$0.const$2.set(seg.asSlice(index*sizeof()), x);
    }
    public static VarHandle name$VH() {
        return constants$0.const$3;
    }
    /**
     * Getter for field:
     * {@snippet :
     * char* name;
     * }
     */
    public static MemorySegment name$get(MemorySegment seg) {
        return (java.lang.foreign.MemorySegment)constants$0.const$3.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * char* name;
     * }
     */
    public static void name$set(MemorySegment seg, MemorySegment x) {
        constants$0.const$3.set(seg, x);
    }
    public static MemorySegment name$get(MemorySegment seg, long index) {
        return (java.lang.foreign.MemorySegment)constants$0.const$3.get(seg.asSlice(index*sizeof()));
    }
    public static void name$set(MemorySegment seg, long index, MemorySegment x) {
        constants$0.const$3.set(seg.asSlice(index*sizeof()), x);
    }
    public static VarHandle content$VH() {
        return constants$0.const$4;
    }
    /**
     * Getter for field:
     * {@snippet :
     * char* content;
     * }
     */
    public static MemorySegment content$get(MemorySegment seg) {
        return (java.lang.foreign.MemorySegment)constants$0.const$4.get(seg);
    }
    /**
     * Setter for field:
     * {@snippet :
     * char* content;
     * }
     */
    public static void content$set(MemorySegment seg, MemorySegment x) {
        constants$0.const$4.set(seg, x);
    }
    public static MemorySegment content$get(MemorySegment seg, long index) {
        return (java.lang.foreign.MemorySegment)constants$0.const$4.get(seg.asSlice(index*sizeof()));
    }
    public static void content$set(MemorySegment seg, long index, MemorySegment x) {
        constants$0.const$4.set(seg.asSlice(index*sizeof()), x);
    }
    public static long sizeof() { return $LAYOUT().byteSize(); }
    public static MemorySegment allocate(SegmentAllocator allocator) { return allocator.allocate($LAYOUT()); }
    public static MemorySegment allocateArray(long len, SegmentAllocator allocator) {
        return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }
    public static MemorySegment ofAddress(MemorySegment addr, Arena arena) { return RuntimeHelper.asArray(addr, $LAYOUT(), 1, arena); }
}

