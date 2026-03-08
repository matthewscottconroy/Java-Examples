package com.examples.hello;

import javax.swing.JOptionPane;

/**
 * Entry point for the HelloJPackage project.
 *
 * <p>This application shows a pop-up dialog — appropriate for a native desktop
 * app bundled with {@code jpackage}.  After packaging, the user double-clicks
 * an icon rather than running a terminal command.
 */
public class Main {

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(
                null,
                "Hello, jpackage!\n\nThis app was bundled as a native executable\nusing the jpackage tool included with the JDK.",
                "Hello",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
