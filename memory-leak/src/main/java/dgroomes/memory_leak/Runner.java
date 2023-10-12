package dgroomes.memory_leak;

import dgroomes.memory_leak.bindings.hello_h;

import java.lang.foreign.MemorySegment;

public class Runner {

    public static void main(String[] args) {
        System.out.println("(Not yet fully implemented)");
        System.out.println("Let's call native code from Java! Here we go...");
        String message;
        {
            MemorySegment memorySegment = hello_h.hello();
            message = memorySegment.getUtf8String(0);
        }
        System.out.printf("The C function returned '%s'.%n", message);
    }
}
