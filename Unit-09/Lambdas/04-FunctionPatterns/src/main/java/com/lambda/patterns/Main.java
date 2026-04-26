package com.lambda.patterns;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Function Patterns ===");
        FunctionPatterns fp = new FunctionPatterns();
        fp.showPartialApplication();
        fp.showCurrying();
        fp.showMemoization();
        fp.showPipeline();
    }
}
