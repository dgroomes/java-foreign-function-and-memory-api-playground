// Generated by jextract

package dgroomes.bindings;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.lang.foreign.*;
import static java.lang.foreign.ValueLayout.*;
public class lucky_number_h  {

    public static final OfByte C_CHAR = JAVA_BYTE;
    public static final OfShort C_SHORT = JAVA_SHORT;
    public static final OfInt C_INT = JAVA_INT;
    public static final OfLong C_LONG = JAVA_LONG;
    public static final OfLong C_LONG_LONG = JAVA_LONG;
    public static final OfFloat C_FLOAT = JAVA_FLOAT;
    public static final OfDouble C_DOUBLE = JAVA_DOUBLE;
    public static final AddressLayout C_POINTER = RuntimeHelper.POINTER;
    public static MethodHandle luckyNumber$MH() {
        return RuntimeHelper.requireNonNull(constants$0.const$1,"luckyNumber");
    }
    /**
     * {@snippet :
     * int luckyNumber(,...);
     * }
     */
    public static int luckyNumber(Object... x0) {
        var mh$ = luckyNumber$MH();
        try {
            return (int)mh$.invokeExact(x0);
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}


