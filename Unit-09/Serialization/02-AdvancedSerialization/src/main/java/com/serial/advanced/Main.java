package com.serial.advanced;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Custom writeObject / readObject ===");
        CustomSerial.demonstrate();

        System.out.println("\n=== Externalizable ===");
        ExternalizableDemo.demonstrate();

        System.out.println("\n=== writeReplace and readResolve ===");
        WriteReplaceDemo.showSingletonResolve();
        WriteReplaceDemo.showWriteReplace();

        System.out.println("\n=== Inheritance: subclass of non-Serializable parent ===");
        InheritanceDemo.demonstrate();
    }
}
