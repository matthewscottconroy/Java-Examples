package com.lambda.capture;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Capture and Composition ===");

        CaptureDemo capture = new CaptureDemo();
        capture.showEffectivelyFinal();
        capture.showInstanceCapture();
        capture.showClosurePitfall();
        capture.showLambdaLifetime();

        System.out.println();
        CompositionDemo comp = new CompositionDemo();
        comp.showFunctionComposition();
        comp.showPredicateComposition();
        comp.showConsumerChaining();
        comp.showUnaryOperatorComposition();
    }
}
