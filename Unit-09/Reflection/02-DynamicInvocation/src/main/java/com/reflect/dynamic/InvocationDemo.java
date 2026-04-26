package com.reflect.dynamic;

import java.lang.reflect.*;

/**
 * Dynamic invocation — calling methods, reading/writing fields, and creating
 * instances entirely at runtime using only string identifiers.
 *
 * <p>This is the mechanism behind:
 * JUnit ({@code @Test} method discovery), Spring ({@code @Autowired} injection),
 * Jackson (field-by-field JSON mapping), and every IoC container.
 *
 * <p><strong>setAccessible(true)</strong> — bypasses Java's access control for
 * this specific reflective access.  Requires that the calling code is in the
 * same module or the field/method is in an open package.  In the unnamed module
 * (ordinary classpath code) it works without restriction.
 *
 * <p>Cost: reflection is slower than direct calls because it bypasses JIT
 * inlining.  For hot paths, cache the Method/Field object; repeated lookup
 * via getMethod() is the real bottleneck, not invoke() itself.
 */
public class InvocationDemo {

    public static void showInstanceCreation() throws Exception {
        System.out.println("-- newInstance() via Constructor --");

        // No-arg constructor.
        Constructor<Target> noArg = Target.class.getDeclaredConstructor();
        Target t1 = noArg.newInstance();
        System.out.println("  no-arg: " + t1);

        // Constructor with arguments — must specify the exact parameter types.
        Constructor<Target> withArg = Target.class.getDeclaredConstructor(String.class);
        Target t2 = withArg.newInstance("custom-value");
        System.out.println("  with arg: " + t2);
    }

    public static void showMethodInvocation() throws Exception {
        Target target = new Target();
        System.out.println("\n-- Method.invoke() --");

        // Public method — getMethod() searches declared + inherited public methods.
        Method pub = Target.class.getMethod("publicMethod", String.class);
        String result = (String) pub.invoke(target, "hello");
        System.out.println("  publicMethod: " + result);

        // Overload resolution: select the int overload explicitly.
        Method processInt = Target.class.getMethod("process", int.class);
        System.out.println("  process(int): " + processInt.invoke(target, 42));

        // Private method — getDeclaredMethod(), then setAccessible.
        Method priv = Target.class.getDeclaredMethod("privateMethod");
        priv.setAccessible(true);
        System.out.println("  privateMethod: " + priv.invoke(target));

        // Private method with argument.
        Method inc = Target.class.getDeclaredMethod("increment", int.class);
        inc.setAccessible(true);
        inc.invoke(target, 5);
        inc.invoke(target, 10);
        System.out.println("  counter after two increments: " + target.getCounter());

        // invoke() on a null target → calls a static method.
        Method valueOf = Integer.class.getMethod("valueOf", int.class);
        System.out.println("  Integer.valueOf(99) via invoke: " + valueOf.invoke(null, 99));
    }

    public static void showFieldAccess() throws Exception {
        Target target = new Target();
        System.out.println("\n-- Field get/set --");

        // Public field.
        Field pub = Target.class.getField("publicField");
        System.out.println("  publicField (before): " + pub.get(target));
        pub.set(target, "overwritten");
        System.out.println("  publicField (after):  " + pub.get(target));

        // Private field.
        Field priv = Target.class.getDeclaredField("privateField");
        priv.setAccessible(true);
        System.out.println("  privateField (before): " + priv.get(target));
        priv.set(target, "exposed");
        System.out.println("  privateField (after):  " + priv.get(target));

        // Static field via Field with null instance.
        Field counterField = Target.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        counterField.setInt(target, 100);    // typed setter avoids boxing
        System.out.println("  counter set to 100: " + target.getCounter());
    }
}
