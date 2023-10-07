package dgroomes;

import dgroomes.bindings.lucky_number_h;

public class Runner {

  public static void main(String[] args) {
    System.out.println("Let's call native code from Java! Here we go...");
    int luckyNumber = lucky_number_h.luckyNumber();
    System.out.printf("Your lucky number is %d.%n", luckyNumber);
  }
}
