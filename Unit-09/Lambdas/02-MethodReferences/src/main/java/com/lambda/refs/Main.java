package com.lambda.refs;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Method References — all four kinds ===");
        MethodRefDemo.showStatic();
        MethodRefDemo.showBound();
        MethodRefDemo.showUnbound();
        MethodRefDemo.showConstructor();
    }
}
