package dgroomes.memory_addresses;

public class Runner {

    public static void main(String[] args) {
        System.out.println("(Not yet fully implemented)");
        System.out.println("Let's call native code from Java! Here we go...");
        int number = dgroomes.memory_addresses.bindings.hello_h.hello();
        System.out.printf("The C function returned %d.%n", number);
    }
}
