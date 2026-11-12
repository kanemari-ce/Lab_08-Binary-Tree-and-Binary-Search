import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dependency-free test runner for lab use (no JUnit required).
 *
 * Discovers the public no-arg "test*" methods of the given classes, runs each
 * one on a fresh instance of the test class, and reports PASS/FAIL per test.
 * Any throwable escaping a test (including an UnsupportedOperationException
 * thrown by an unimplemented TODO stub) is recorded as a failure of that test
 * only; the rest of the suite keeps running.
 *
 * Usage:  java LabTestRunner <TestClass1> [TestClass2 ...]
 * Exit:   0 = all tests passed, 1 = at least one failure, 2 = usage error.
 */
public final class LabTestRunner {

    /** Thrown by the assert* helpers when an expectation is not met. */
    public static final class TestFailure extends RuntimeException {
        public TestFailure(String message) {
            super(message);
        }
    }

    private LabTestRunner() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new TestFailure(message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        boolean equal = expected == null ? actual == null : expected.equals(actual);
        if (!equal) {
            throw new TestFailure(message + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertNull(Object actual, String message) {
        if (actual != null) {
            throw new TestFailure(message + ": expected null but was <" + actual + ">");
        }
    }

    public static void assertNotNull(Object actual, String message) {
        if (actual == null) {
            throw new TestFailure(message + ": expected a value but was null");
        }
    }

    public static void assertThrows(Class<? extends Throwable> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable thrown) {
            if (!expectedType.isInstance(thrown)) {
                throw new TestFailure(message + ": expected " + expectedType.getSimpleName()
                        + " but caught " + thrown.getClass().getSimpleName() + ": " + thrown.getMessage());
            }
            return;
        }
        throw new TestFailure(message + ": expected " + expectedType.getSimpleName() + " but nothing was thrown");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java LabTestRunner <TestClass1> [TestClass2 ...]");
            System.exit(2);
        }
        boolean allGreen = true;
        for (String className : args) {
            try {
                if (run(Class.forName(className)) != 0) {
                    allGreen = false;
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Test class not found: " + className);
                allGreen = false;
            }
        }
        System.out.println(allGreen ? "ALL GREEN" : "FAILURES PRESENT");
        System.exit(allGreen ? 0 : 1);
    }

    private static int run(Class<?> testClass) {
        System.out.println("== " + testClass.getSimpleName() + " ==");
        List<Method> tests = new ArrayList<>();
        for (Method method : testClass.getDeclaredMethods()) {
            boolean isTest = method.getName().startsWith("test")
                    && method.getParameterCount() == 0
                    && method.getReturnType() == void.class
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers());
            if (isTest) {
                tests.add(method);
            }
        }
        tests.sort(Comparator.comparing(Method::getName));
        if (tests.isEmpty()) {
            System.out.println("  no test* methods found");
            return 1;
        }
        int passed = 0;
        int failed = 0;
        for (Method test : tests) {
            String name = test.getName();
            long startNanos = System.nanoTime();
            try {
                test.invoke(testClass.getDeclaredConstructor().newInstance());
                System.out.printf("  [PASS] %-46s (%.1f ms)%n", name,
                        (System.nanoTime() - startNanos) / 1.0e6);
                passed++;
            } catch (InvocationTargetException wrapper) {
                Throwable cause = wrapper.getCause();
                String reason;
                if (cause instanceof TestFailure) {
                    reason = cause.getMessage();
                } else {
                    reason = "threw " + (cause == null ? wrapper : cause).getClass().getSimpleName()
                            + (cause != null && cause.getMessage() != null ? ": " + cause.getMessage() : "");
                }
                System.out.printf("  [FAIL] %-46s (%.1f ms)%n         -> %s%n", name,
                        (System.nanoTime() - startNanos) / 1.0e6, reason);
                failed++;
            } catch (ReflectiveOperationException e) {
                System.out.println("  [FAIL] " + name + " -> could not run: " + e);
                failed++;
            }
        }
        System.out.println("  " + passed + "/" + tests.size() + " passed");
        return failed == 0 ? 0 : 1;
    }
}
