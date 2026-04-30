package com.exam;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.exam.Difficulty.*;
import static com.exam.QuestionType.*;

/**
 * Built-in question pool, organised by type and annotated with difficulty.
 *
 * <p>Questions from external {@code .q} files (loaded via {@link QuestionLoader})
 * are merged in by {@link #all(Path)} at runtime.
 *
 * <p>Difficulty distribution per type (current bank):
 * <pre>
 *   WRITE  : 7 Easy · 8 Medium · 5 Hard  = 20
 *   DEBUG  : 5 Easy · 6 Medium · 4 Hard  = 15
 *   EXTEND : 2 Easy · 7 Medium · 6 Hard  = 15
 *   TRACE  : 5 Easy · 7 Medium · 3 Hard  = 15
 *   DESIGN : 2 Easy · 5 Medium · 7 Hard  = 14
 *                                  Total = 79
 * </pre>
 */
public class QuestionBank {

    /** Built-in bank only. */
    public static List<Question> all() {
        return all(null);
    }

    /**
     * Built-in bank merged with any {@code .q} files found under {@code externalDir}.
     * Pass {@code null} to skip external loading.
     */
    public static List<Question> all(Path externalDir) {
        List<Question> bank = new ArrayList<>();
        bank.addAll(writeQuestions());
        bank.addAll(debugQuestions());
        bank.addAll(extendQuestions());
        bank.addAll(traceQuestions());
        bank.addAll(designQuestions());
        if (externalDir != null) bank.addAll(QuestionLoader.loadFrom(externalDir));
        return bank;
    }

    // =========================================================================
    // WRITE (20)
    // =========================================================================

    private static List<Question> writeQuestions() {
        return List.of(

        // ---- EASY (7) -------------------------------------------------------

        new Question(WRITE, "Arrays", "01", EASY,
            "Write a static method void reverseInPlace(int[] arr) that reverses " +
            "the array in place without allocating a new array.",
            "",
            "Use two pointers lo=0, hi=arr.length-1. While lo < hi, swap arr[lo] " +
            "and arr[hi] then advance both. No extra space needed."),

        new Question(WRITE, "Strings", "02", EASY,
            "Write a static method int countVowels(String s) that returns the number " +
            "of vowel characters (a, e, i, o, u — case-insensitive) in s.",
            "",
            "Convert to lowercase, iterate chars, check \"aeiou\".indexOf(c) >= 0. " +
            "Or use s.toLowerCase().chars().filter(\"aeiou\"::indexOf … >= 0).count()."),

        new Question(WRITE, "Arrays", "01", EASY,
            "Write a static method int[] removeDuplicates(int[] arr) that returns " +
            "a new array containing only the first occurrence of each value in arr, " +
            "preserving original order. Do not use any collection class.",
            "",
            "Use a boolean[] seen sized to the value range, or an inner-loop O(n²) scan. " +
            "Collect unique values into a temporary array, then trim to the filled length."),

        new Question(WRITE, "Strings", "02", EASY,
            "Write a static method boolean isPalindrome(String s) that returns true " +
            "if s reads the same forwards and backwards, ignoring case and any character " +
            "that is not a letter or digit.",
            "",
            "Strip non-alphanumeric: s.replaceAll(\"[^a-zA-Z0-9]\", \"\").toLowerCase(). " +
            "Compare to its reverse: new StringBuilder(cleaned).reverse().toString()."),

        new Question(WRITE, "Inheritance", "05", EASY,
            "Write an abstract class Animal with a field String name and an abstract " +
            "method String sound(). Extend it with Dog (\"woof\"), Cat (\"meow\"), and " +
            "Duck (\"quack\"). In main, store all three in a List<Animal> and print " +
            "\"<name> says <sound>\" for each.",
            "",
            "Animal has constructor Animal(String name). Each subclass calls super(name) " +
            "and implements sound(). Main: List.of(new Dog(\"Rex\"), ...) then " +
            "animals.forEach(a -> System.out.println(a.name + \" says \" + a.sound()));"),

        new Question(WRITE, "Lambdas", "09", EASY,
            "Given a List<String> words, use a Comparator built from lambdas to sort " +
            "by word length ascending, then alphabetically for equal lengths. Print " +
            "each word on its own line. Do not write an explicit Comparator class.",
            "",
            "Comparator<String> cmp = Comparator.comparingInt(String::length)" +
            ".thenComparing(Comparator.naturalOrder()); words.sort(cmp); words.forEach(System.out::println);"),

        new Question(WRITE, "Error Detection", "10", EASY,
            "Implement a utility class Checksum with: " +
            "(a) static byte compute(byte[] data) — sum all bytes mod 256 then negate (one's complement); " +
            "(b) static boolean verify(byte[] data, byte cs) — true when sum of data bytes plus cs equals 0xFF.",
            "",
            "compute: int sum=0; for(byte b:data) sum+=(b&0xFF); return (byte)(~(sum&0xFF)). " +
            "verify: int sum=0; for(byte b:data) sum+=(b&0xFF); return (sum+(cs&0xFF))&0xFF==0xFF."),

        // ---- MEDIUM (8) -----------------------------------------------------

        new Question(WRITE, "Classes", "02", MEDIUM,
            "Design and implement a BankAccount class. It must store an owner name " +
            "and a balance. Provide deposit(double amount), withdraw(double amount) " +
            "(throw IllegalArgumentException if the amount exceeds the balance), " +
            "and a formatted toString() that shows owner and balance to two decimal places.",
            "",
            "Fields: String owner, double balance. Validate amount > 0 in both methods. " +
            "withdraw checks balance >= amount. toString: String.format(\"BankAccount[%s $%.2f]\", owner, balance)."),

        new Question(WRITE, "Collections", "02", MEDIUM,
            "Write a program that accepts a List<String> of words and uses a HashMap " +
            "to count how many times each word appears. Print the words sorted by " +
            "frequency (highest first); break ties alphabetically.",
            "",
            "Use Map.merge(word, 1, Integer::sum). Sort entrySet() with " +
            "Map.Entry.<String,Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey())."),

        new Question(WRITE, "Exceptions", "03", MEDIUM,
            "Write a custom checked exception InsufficientFundsException that carries " +
            "the requested amount and the available balance. Then write a Wallet class " +
            "with a spend(double amount) method that throws this exception when the " +
            "balance is too low.",
            "",
            "InsufficientFundsException extends Exception; constructor stores both fields " +
            "and calls super with a descriptive message. Wallet.spend checks balance < amount."),

        new Question(WRITE, "Interfaces", "05", MEDIUM,
            "Define an interface Shape with methods double area() and double perimeter(). " +
            "Implement it in Circle (radius field) and Rectangle (width, height fields). " +
            "Write a static helper totalArea(List<Shape> shapes) that sums all areas.",
            "",
            "Circle: area=πr², perimeter=2πr. Rectangle: area=w*h, perimeter=2*(w+h). " +
            "totalArea: shapes.stream().mapToDouble(Shape::area).sum()."),

        new Question(WRITE, "Generics", "09", MEDIUM,
            "Write a generic class Stack<T> backed by an ArrayList<T>. Provide " +
            "push(T item), T pop() (throw NoSuchElementException if empty), " +
            "T peek() (same exception), boolean isEmpty(), and int size().",
            "",
            "Store elements in ArrayList<T>. push → add to end. pop/peek → get index size()-1. " +
            "pop also removes. Throw new NoSuchElementException(\"Stack is empty\") when empty."),

        new Question(WRITE, "Generics", "09", MEDIUM,
            "Write a generic record Pair<A, B> with fields first and second. Add: " +
            "(a) a static factory method of(A, B); " +
            "(b) Pair<B, A> swap() that returns a new Pair with the fields reversed; " +
            "(c) override toString() to produce \"(first, second)\".",
            "",
            "record Pair<A,B>(A first, B second). of: return new Pair<>(a,b). " +
            "swap: return new Pair<>(second, first). toString: '('+first+\", \"+second+')'."),

        new Question(WRITE, "Algorithms", "10", MEDIUM,
            "Implement static int binarySearch(int[] sorted, int target) without using " +
            "Arrays.binarySearch. Return the index of target if found, or " +
            "-(insertion point + 1) if not found, matching the standard library contract.",
            "",
            "lo=0, hi=length-1. mid=(lo+hi)>>>1 (avoids overflow). " +
            "If sorted[mid]==target return mid. If less, lo=mid+1. If greater, hi=mid-1. " +
            "On exit return -(lo+1). Test with found, missing, and boundary values."),

        new Question(WRITE, "Collections", "02", MEDIUM,
            "Implement an LRU (Least Recently Used) cache with a fixed capacity using " +
            "LinkedHashMap. The cache maps String keys to String values. When the cache " +
            "is full, inserting a new entry evicts the least recently used one.",
            "",
            "Extend LinkedHashMap with accessOrder=true and override removeEldestEntry " +
            "to return size() > capacity. Provide put(String k, String v) and get(String k)."),

        // ---- HARD (5) -------------------------------------------------------

        new Question(WRITE, "Streams", "09", HARD,
            "Using only the Stream API (no for-loops), given a List<Integer> numbers: " +
            "(a) collect only even numbers into a new list, each doubled; " +
            "(b) compute the sum of the original list using IntStream; " +
            "(c) find the maximum value, printing \"none\" if the list is empty.",
            "",
            "(a) numbers.stream().filter(n->n%2==0).map(n->n*2).toList(). " +
            "(b) numbers.stream().mapToInt(Integer::intValue).sum(). " +
            "(c) numbers.stream().mapToInt(Integer::intValue).max().ifPresentOrElse(System.out::println, ()->System.out.println(\"none\"))."),

        new Question(WRITE, "Concurrency", "09", HARD,
            "Write a program that uses an ExecutorService with a fixed thread pool of " +
            "4 threads to compute the square of each integer from 1 to 20 in parallel. " +
            "Collect the Future<Integer> results and print them in submission order. " +
            "Ensure the pool is shut down in all cases.",
            "",
            "Submit Callable<Integer> lambdas, collect futures. Iterate futures calling " +
            "future.get() in submission order to guarantee ordering. shutdown() in finally. " +
            "awaitTermination for clean shutdown."),

        new Question(WRITE, "Design Patterns", "10", HARD,
            "Implement the Observer pattern. Create a NewsPublisher that maintains a " +
            "list of Subscriber objects and notifies all of them when publishArticle(String) " +
            "is called. Demonstrate with two subscribers that format the headline differently.",
            "",
            "@FunctionalInterface interface Subscriber { void onArticle(String headline); } " +
            "NewsPublisher holds List<Subscriber>, has addSubscriber, removeSubscriber, " +
            "publishArticle. Two lambda subscribers: print \"Breaking: \"+h and h.toUpperCase()."),

        new Question(WRITE, "Lambdas", "09", HARD,
            "Write a static utility method <A, B> Function<A, B> memoize(Function<A, B> fn) " +
            "that returns a new function backed by a HashMap cache. The first call for each " +
            "argument computes the result; subsequent calls return the cached value. " +
            "Demonstrate with an expensive Fibonacci computation.",
            "",
            "Map<A,B> cache=new HashMap<>(); return a -> cache.computeIfAbsent(a, fn). " +
            "For thread safety use ConcurrentHashMap. Demonstrate: Function<Integer,Long> " +
            "memoFib = memoize(n -> slowFib(n)); calling with the same n twice should " +
            "only compute once (verify with a call counter or print inside the function)."),

        new Question(WRITE, "Concurrency", "09", HARD,
            "Write a program that counts word frequencies across a list of strings in " +
            "parallel using ConcurrentHashMap. Launch one thread per string; each thread " +
            "splits its string on whitespace and calls map.merge(word, 1L, Long::sum). " +
            "After all threads finish, print the top-5 words by frequency.",
            "",
            "ConcurrentHashMap<String,Long> counts = new ConcurrentHashMap<>(); " +
            "ExecutorService pool=Executors.newFixedThreadPool(N). Submit one Runnable " +
            "per string. pool.shutdown(); pool.awaitTermination. Sort entries by value desc, limit 5.")
        );
    }

    // =========================================================================
    // DEBUG (15)
    // =========================================================================

    private static List<Question> debugQuestions() {
        return List.of(

        // ---- EASY (5) -------------------------------------------------------

        new Question(DEBUG, "Arrays", "01", EASY,
            "The method below is supposed to return the sum of all elements in the " +
            "array, but contains two bugs. Identify each bug and write the corrected method.",
            """
static int sumArray(int[] data) {
    int sum = 0;
    for (int i = 1; i <= data.length; i++) {
        sum += data[i];
    }
    return sum;
}""",
            "Bug 1: i=1 skips data[0]. Fix: i=0. " +
            "Bug 2: i<=data.length causes ArrayIndexOutOfBoundsException. Fix: i<data.length."),

        new Question(DEBUG, "Strings", "02", EASY,
            "The validation method below misbehaves for certain inputs. " +
            "Identify the bug and correct it.",
            """
static boolean isValidCode(String code) {
    if (code == null || code == "") {
        return false;
    }
    return code.length() == 6;
}""",
            "Bug: code==\"\" compares references, not content. A freshly constructed " +
            "String with value \"\" will not be == to the literal. Fix: code.isEmpty()."),

        new Question(DEBUG, "Loops", "01", EASY,
            "The method below is supposed to build a String of every other character " +
            "(indices 0, 2, 4, …) from the input, but it produces the wrong result. " +
            "Identify the bug and fix it.",
            """
static String everyOther(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
        sb.append(s.charAt(i));
        i++;
    }
    return sb.toString();
}""",
            "The loop already increments i in its update clause (i++), and then " +
            "increments it again inside the body — so every character is appended and " +
            "then two indices are skipped. Fix: remove the i++ inside the body (keep only " +
            "the update clause increment, or change the loop to i+=2 and remove the body i++)."),

        new Question(DEBUG, "Recursion", "10", EASY,
            "The recursive method below works for most inputs but fails for one " +
            "specific argument. Identify that argument, explain why it fails, and fix the method.",
            """
static int factorial(int n) {
    if (n == 1) return 1;
    return n * factorial(n - 1);
}""",
            "factorial(0) causes infinite recursion: 0 != 1 so the base case is never hit; " +
            "the call chain goes 0 → -1 → -2 → … until StackOverflowError. " +
            "Fix: if (n <= 1) return 1. Also add a guard: if (n < 0) throw new IllegalArgumentException()."),

        new Question(DEBUG, "Data Types", "01", EASY,
            "The program intends to compute the number of seconds in a century " +
            "but prints a wrong (possibly negative) number. Explain why and fix it.",
            """
int secondsInCentury = 60 * 60 * 24 * 365 * 100;
System.out.println("Seconds in a century: " + secondsInCentury);""",
            "All operands are int literals so the multiplication is performed in int " +
            "arithmetic and silently overflows before being stored. " +
            "Fix: force long arithmetic with a long literal: 60L * 60 * 24 * 365 * 100."),

        // ---- MEDIUM (6) -----------------------------------------------------

        new Question(DEBUG, "Collections", "02", MEDIUM,
            "The code below throws a runtime exception. Name the exception, " +
            "explain why it occurs, and rewrite the loop correctly.",
            """
List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
for (Integer n : numbers) {
    if (n % 2 == 0) {
        numbers.remove(n);
    }
}
System.out.println(numbers);""",
            "ConcurrentModificationException: the enhanced-for uses an iterator internally; " +
            "removing from the backing list while the iterator is live invalidates it. " +
            "Fix: numbers.removeIf(n -> n % 2 == 0); or iterate with an explicit Iterator and call it.remove()."),

        new Question(DEBUG, "Exceptions", "03", MEDIUM,
            "This method has a resource-leak bug: the reader may not be closed when " +
            "an exception is thrown. Identify the problem and rewrite using the " +
            "appropriate Java feature.",
            """
static String readFirstLine(String filename) throws IOException {
    BufferedReader reader =
        new BufferedReader(new FileReader(filename));
    String line = reader.readLine();
    reader.close();
    return line;
}""",
            "If readLine() throws, close() is never reached. Fix: use try-with-resources: " +
            "try (BufferedReader reader = new BufferedReader(new FileReader(filename))) { " +
            "return reader.readLine(); } — the JVM guarantees close() on any exit path."),

        new Question(DEBUG, "Classes", "02", MEDIUM,
            "The Counter class below behaves unexpectedly when multiple instances exist. " +
            "Identify the root cause, predict the printed output, and correct the class.",
            """
class Counter {
    static int count = 0;

    void increment() { count++; }
    int  getCount()  { return count; }
}

Counter a = new Counter();
Counter b = new Counter();
a.increment();
a.increment();
System.out.println("a=" + a.getCount());
System.out.println("b=" + b.getCount());""",
            "count is static — shared across all Counter instances. " +
            "Output: a=2, b=2 (b shows a's increments). " +
            "Fix: remove static so count is an instance field."),

        new Question(DEBUG, "Generics", "09", MEDIUM,
            "The code below compiles with a warning and throws an exception at runtime. " +
            "Identify the warning, explain the failure, and rewrite safely.",
            """
List list = new ArrayList();
list.add("hello");
list.add(42);
for (Object obj : list) {
    String s = (String) obj;
    System.out.println(s.toUpperCase());
}""",
            "Raw-type warning: List without a type argument disables generic checks. " +
            "ClassCastException at runtime when casting 42 (Integer) to String. " +
            "Fix: List<String> list = new ArrayList<>(); — the compiler rejects list.add(42)."),

        new Question(DEBUG, "Exceptions", "03", MEDIUM,
            "The code below always prints \"done\" even when an important exception " +
            "is silently swallowed. Identify the anti-pattern and rewrite correctly.",
            """
static int parseAndDouble(String s) {
    try {
        return Integer.parseInt(s) * 2;
    } catch (Exception e) {
        // ignore all errors
        return 0;
    }
}""",
            "Catching Exception (or worse, Throwable) hides every error, including " +
            "unexpected ones like NullPointerException or OutOfMemoryError. Returning 0 " +
            "silently gives callers bad data. Fix: catch only the expected NumberFormatException, " +
            "and either rethrow a domain exception or document the sentinel value in the javadoc."),

        new Question(DEBUG, "Inheritance", "05", MEDIUM,
            "The code below compiles but produces unexpected output because the " +
            "subclass does not properly override the parent method. Find and fix the bug.",
            """
class Animal {
    String describe() { return "Animal"; }
}
class Dog extends Animal {
    String describe(String mood) { return "Dog is " + mood; }
}

Animal d = new Dog();
System.out.println(d.describe());""",
            "Dog.describe(String) is an overload, not an override of describe(). " +
            "Animal.describe() is still inherited unchanged, so d.describe() prints \"Animal\". " +
            "Fix: override the no-arg version: @Override String describe() { return \"Dog\"; } " +
            "Adding @Override would have caught this at compile time."),

        // ---- HARD (4) -------------------------------------------------------

        new Question(DEBUG, "Concurrency", "09", HARD,
            "The code below has a race condition that causes the final count to be " +
            "less than 10 000 on most runs. Explain why and provide two different fixes.",
            """
class UnsafeCounter {
    int count = 0;
    void increment() { count++; }
}

UnsafeCounter c = new UnsafeCounter();
List<Thread> threads = new ArrayList<>();
for (int i = 0; i < 100; i++) {
    threads.add(new Thread(() -> {
        for (int j = 0; j < 100; j++) c.increment();
    }));
}
threads.forEach(Thread::start);
threads.forEach(t -> { try { t.join(); } catch (Exception e) {} });
System.out.println(c.count);""",
            "count++ is not atomic: it reads, increments, then writes. Two threads can " +
            "read the same value simultaneously, both increment it, and write the same " +
            "result — losing one increment. " +
            "Fix 1: replace int count with AtomicInteger count = new AtomicInteger(); " +
            "increment with count.incrementAndGet(); read with count.get(). " +
            "Fix 2: mark increment() synchronized."),

        new Question(DEBUG, "Generics", "09", HARD,
            "The method below fails to compile. Explain the exact error and rewrite " +
            "the method so it compiles and works correctly.",
            """
static void addNumbers(List<Number> list) {
    list.add(42);
    list.add(3.14);
}

// Caller:
List<Integer> ints = new ArrayList<>();
addNumbers(ints);   // compile error here""",
            "List<Integer> is not a subtype of List<Number> even though Integer extends Number " +
            "(generics are invariant). The caller gets a compile error. " +
            "Fix: change the parameter to List<? super Integer> if you only need to add Integers, " +
            "or to List<? super Number> to accept any list whose element type is a supertype of Number. " +
            "Alternatively, provide overloads or use wildcards according to the PECS rule " +
            "(Producer Extends, Consumer Super)."),

        new Question(DEBUG, "Recursion", "10", HARD,
            "The method below should return the nth Fibonacci number but runs in " +
            "exponential time and produces a StackOverflowError for large n. " +
            "Identify both problems and rewrite using memoization.",
            """
static long fib(int n) {
    return fib(n - 1) + fib(n - 2);
}""",
            "Bug 1: no base case — fib(0) and fib(1) recurse forever causing StackOverflowError. " +
            "Bug 2: exponential time O(2^n) due to repeated sub-problem recomputation. " +
            "Fix: add if (n <= 1) return n; base case. " +
            "Add memoization: Map<Integer,Long> memo = new HashMap<>(); " +
            "return memo.computeIfAbsent(n, k -> fib(k-1) + fib(k-2)); " +
            "This reduces to O(n) time and space."),

        new Question(DEBUG, "Streams", "09", HARD,
            "The stream pipeline below compiles but throws a runtime exception. " +
            "Identify the exception, explain why it occurs, and rewrite correctly.",
            """
List<String> words = Arrays.asList("hello", null, "world");
long count = words.stream()
    .filter(s -> s.length() > 3)
    .count();
System.out.println(count);""",
            "NullPointerException: s.length() is called on the null element because filter " +
            "does not skip nulls automatically. " +
            "Fix: add a null guard in the filter: .filter(s -> s != null && s.length() > 3) " +
            "or .filter(Objects::nonNull).filter(s -> s.length() > 3).")
        );
    }

    // =========================================================================
    // EXTEND (15)
    // =========================================================================

    private static List<Question> extendQuestions() {
        return List.of(

        // ---- EASY (2) -------------------------------------------------------

        new Question(EXTEND, "Arrays", "01", EASY,
            "The utility class below has a static print method. Extend it with: " +
            "(a) static int[] sorted(int[] arr) that returns a new sorted array without " +
            "modifying the original; " +
            "(b) static boolean contains(int[] arr, int target) using linear search.",
            """
class ArrayUtils {
    static void print(int[] arr) {
        System.out.println(Arrays.toString(arr));
    }
}""",
            "sorted: int[] copy = Arrays.copyOf(arr, arr.length); Arrays.sort(copy); return copy. " +
            "contains: for(int x : arr) if(x==target) return true; return false."),

        new Question(EXTEND, "Strings", "02", EASY,
            "The method below converts a single word to pig-latin (move the first " +
            "consonant cluster to the end and append \"ay\"; words starting with a vowel " +
            "just get \"yay\" appended). Extend it to: " +
            "(a) static String convertSentence(String sentence) that processes each word; " +
            "(b) static String reverse(String pigLatin) that reverses the transformation.",
            """
static String toPigLatin(String word) {
    String vowels = "aeiouAEIOU";
    if (vowels.indexOf(word.charAt(0)) >= 0) return word + "yay";
    int i = 0;
    while (i < word.length() && vowels.indexOf(word.charAt(i)) < 0) i++;
    return word.substring(i) + word.substring(0, i) + "ay";
}""",
            "convertSentence: split on whitespace, apply toPigLatin to each, rejoin with spaces. " +
            "reverse: if ends with \"yay\" return word without it; otherwise find \"ay\" suffix, " +
            "take prefix before ay, split at the original consonant cluster length and reassemble."),

        // ---- MEDIUM (7) -----------------------------------------------------

        new Question(EXTEND, "Classes", "02", MEDIUM,
            "The BankAccount class below supports deposit and withdraw. Extend it to: " +
            "(a) add a List<String> transaction history, appending a readable entry for every operation; " +
            "(b) add transfer(BankAccount target, double amount) that records on both accounts; " +
            "(c) add printHistory() that prints every entry numbered from 1.",
            """
class BankAccount {
    private final String owner;
    private double balance;

    BankAccount(String owner, double initial) {
        this.owner   = owner;
        this.balance = initial;
    }
    void deposit(double amount)  { balance += amount; }
    void withdraw(double amount) {
        if (amount > balance)
            throw new IllegalArgumentException("Insufficient funds");
        balance -= amount;
    }
    double getBalance() { return balance; }
}""",
            "Add List<String> history=new ArrayList<>(); append in deposit/withdraw/transfer. " +
            "transfer calls withdraw(amount) on this, deposit(amount) on target, logs both. " +
            "printHistory: IntStream.range(0,history.size()).forEach(i->print(i+1+\". \"+history.get(i)))."),

        new Question(EXTEND, "Collections", "02", MEDIUM,
            "The code below reads a list of Student records and prints all names. " +
            "Extend it to: (a) sort students by GPA descending; " +
            "(b) print only the top 3 with their GPAs; " +
            "(c) compute and print the class average GPA.",
            """
record Student(String name, double gpa) {}

List<Student> students = List.of(
    new Student("Alice", 3.8), new Student("Bob",   3.2),
    new Student("Carol", 3.9), new Student("Dave",  3.5),
    new Student("Eve",   3.7));
students.forEach(s -> System.out.println(s.name()));""",
            "(a) students.stream().sorted(Comparator.comparingDouble(Student::gpa).reversed()). " +
            "(b) .limit(3).forEach(s->printf(\"%s %.2f%n\",s.name(),s.gpa())). " +
            "(c) students.stream().mapToDouble(Student::gpa).average().orElse(0)."),

        new Question(EXTEND, "Interfaces", "05", MEDIUM,
            "The Shape interface below declares only area(). Extend the code to: " +
            "(a) add perimeter() to the interface; " +
            "(b) update Circle and Rectangle to implement it; " +
            "(c) add a default method describe() that prints area and perimeter formatted to 2dp.",
            """
interface Shape {
    double area();
}
class Circle implements Shape {
    final double r;
    Circle(double r) { this.r = r; }
    public double area() { return Math.PI * r * r; }
}
class Rectangle implements Shape {
    final double w, h;
    Rectangle(double w, double h) { this.w=w; this.h=h; }
    public double area() { return w * h; }
}""",
            "Add double perimeter(); to Shape. Circle: 2*Math.PI*r. Rectangle: 2*(w+h). " +
            "default void describe() { printf(\"area=%.2f perimeter=%.2f%n\", area(), perimeter()); }"),

        new Question(EXTEND, "Streams", "09", MEDIUM,
            "The stream pipeline below filters adults from a list of Person records. " +
            "Extend it to produce three additional results using only the Stream API: " +
            "(a) group people by department (Map<String, List<Person>>); " +
            "(b) average age per department (Map<String, Double>); " +
            "(c) the oldest person (Optional<Person>).",
            """
record Person(String name, int age, String dept) {}
List<Person> people = List.of(
    new Person("Alice", 30, "Engineering"),
    new Person("Bob",   22, "Marketing"),
    new Person("Carol", 35, "Engineering"),
    new Person("Dave",  28, "Marketing"),
    new Person("Eve",   19, "HR"));
List<Person> adults = people.stream().filter(p->p.age()>18).toList();""",
            "(a) Collectors.groupingBy(Person::dept). " +
            "(b) Collectors.groupingBy(Person::dept, Collectors.averagingInt(Person::age)). " +
            "(c) people.stream().max(Comparator.comparingInt(Person::age))."),

        new Question(EXTEND, "Serialization", "09", MEDIUM,
            "The TodoList class below works in memory but loses data when the program " +
            "exits. Extend it to add: " +
            "(a) void save(String filename) that serializes to disk; " +
            "(b) static TodoList load(String filename) that deserializes from disk; " +
            "(c) update main to save after adding items and load on the next run.",
            """
class TodoList implements Serializable {
    private final List<String> items = new ArrayList<>();
    void add(String item)    { items.add(item); }
    void remove(String item) { items.remove(item); }
    void print() { items.forEach(i -> System.out.println("- " + i)); }
}""",
            "save: try(ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(f))){ oos.writeObject(this); } " +
            "load: try(ObjectInputStream ois=new ObjectInputStream(new FileInputStream(f))){ return (TodoList)ois.readObject(); } " +
            "Add @Serial private static final long serialVersionUID=1L;"),

        new Question(EXTEND, "Algorithms", "10", MEDIUM,
            "The method below performs linear search in O(n). " +
            "Extend the program to: (a) add a binarySearch(int[] sorted, int target) in O(log n); " +
            "(b) write a test that generates 50 sorted arrays and verifies both methods return " +
            "the same found/not-found verdict for 10 random targets each.",
            """
static int linearSearch(int[] arr, int target) {
    for (int i = 0; i < arr.length; i++)
        if (arr[i] == target) return i;
    return -1;
}""",
            "binarySearch: lo=0,hi=length-1; mid=(lo+hi)>>>1; compare; return -(lo+1) on miss. " +
            "Test: for each (sorted array, target) pair assert (linearSearch>=0)==(binarySearch>=0). " +
            "Remember to Arrays.sort before binary search in the test."),

        new Question(EXTEND, "Collections", "02", MEDIUM,
            "The StudentGradeBook class below stores raw scores. Extend it to add: " +
            "(a) double average(String student) returning the mean of their scores; " +
            "(b) char letterGrade(String student) (A≥90, B≥80, C≥70, D≥60, F otherwise); " +
            "(c) void printReport() printing each student with their average and letter grade, " +
            "sorted by average descending.",
            """
class StudentGradeBook {
    private final Map<String, List<Double>> grades = new LinkedHashMap<>();

    void addScore(String student, double score) {
        grades.computeIfAbsent(student, k -> new ArrayList<>()).add(score);
    }
}""",
            "average: grades.get(s).stream().mapToDouble(Double::doubleValue).average().orElse(0). " +
            "letterGrade: cascade of if/else on average(s). " +
            "printReport: sort entrySet by average(e.getKey()) desc, print formatted lines."),

        // ---- HARD (6) -------------------------------------------------------

        new Question(EXTEND, "Exceptions", "03", HARD,
            "The parser method below throws RuntimeException on bad input. Extend it to: " +
            "(a) define a checked ParseException that stores the offending string; " +
            "(b) have parsePositiveInt throw ParseException instead; " +
            "(c) write a caller that retries up to 3 times from a list before returning -1.",
            """
static int parsePositiveInt(String s) {
    int value = Integer.parseInt(s);
    if (value <= 0) throw new RuntimeException("Not positive: " + s);
    return value;
}""",
            "class ParseException extends Exception { ParseException(String s){ super(\"Bad: \"+s); } } " +
            "Caller: for(String attempt : attempts){ try{ return parsePositiveInt(attempt); } " +
            "catch(ParseException e){ /* try next */ } } return -1;"),

        new Question(EXTEND, "Concurrency", "09", HARD,
            "The Counter class below is not thread-safe. Extend it to: " +
            "(a) make increment and getCount thread-safe using AtomicInteger; " +
            "(b) write a test that starts 100 threads each calling increment() 1000 times, " +
            "waits for all to finish, and asserts count == 100 000.",
            """
class Counter {
    private int count = 0;
    void increment() { count++; }
    int  getCount()  { return count; }
}""",
            "Replace int with AtomicInteger count=new AtomicInteger(); increment: count.incrementAndGet(); " +
            "getCount: count.get(). Test: ExecutorService pool=Executors.newFixedThreadPool(10); " +
            "submit 100 tasks each calling counter.increment() 1000 times; " +
            "pool.shutdown(); pool.awaitTermination; assert counter.getCount()==100_000."),

        new Question(EXTEND, "Design Patterns", "10", HARD,
            "The TextEditor class below can type and delete characters. Extend it to " +
            "support undo and redo using the Command pattern: " +
            "(a) define a Command interface with execute() and undo(); " +
            "(b) wrap each type/delete operation as a Command; " +
            "(c) add undo() and redo() methods backed by two Deques.",
            """
class TextEditor {
    private StringBuilder text = new StringBuilder();

    void type(String s)   { text.append(s); }
    void delete(int n)    { text.delete(text.length()-n, text.length()); }
    String getText()      { return text.toString(); }
}""",
            "interface Command { void execute(); void undo(); } " +
            "TypeCommand: execute appends s, undo deletes last s.length() chars. " +
            "DeleteCommand: stores deleted text, execute removes it, undo re-inserts. " +
            "Editor holds Deque<Command> undoStack, redoStack. " +
            "undo: pop from undoStack, call cmd.undo(), push to redoStack (and vice versa)."),

        new Question(EXTEND, "Streams", "09", HARD,
            "The number processor below computes only the sum. Extend it to compute " +
            "a full statistics summary using a single stream pass: count, sum, min, max, " +
            "mean, and standard deviation. Return the results in a record Stats.",
            """
static double sum(List<Double> numbers) {
    return numbers.stream().mapToDouble(Double::doubleValue).sum();
}""",
            "Use DoubleSummaryStatistics stats = numbers.stream().mapToDouble(Double::doubleValue).summaryStatistics(). " +
            "For stddev: compute variance = numbers.stream().mapToDouble(x->(x-mean)*(x-mean)).average().orElse(0); " +
            "stddev = Math.sqrt(variance). Return record Stats(long count, double sum, double min, double max, double mean, double stddev)."),

        new Question(EXTEND, "Concurrency", "09", HARD,
            "The simple HTTP fetcher below runs requests sequentially. Refactor it to " +
            "fetch all URLs concurrently using CompletableFuture, collect the results in " +
            "submission order, and add a timeout of 5 seconds per request.",
            """
static List<String> fetchAll(List<String> urls) throws Exception {
    List<String> results = new ArrayList<>();
    for (String url : urls) {
        results.add(fetch(url));  // blocks until response
    }
    return results;
}
// Assume: static String fetch(String url) throws Exception""",
            "List<CompletableFuture<String>> futures = urls.stream() " +
            ".map(url -> CompletableFuture.supplyAsync(() -> { try { return fetch(url); } catch(Exception e){ throw new RuntimeException(e); } })" +
            ".orTimeout(5, TimeUnit.SECONDS)) " +
            ".toList(); " +
            "return futures.stream().map(CompletableFuture::join).toList(); " +
            "Use CompletableFuture.allOf(...).join() if you need all to complete before collecting."),

        new Question(EXTEND, "Design Patterns", "10", HARD,
            "The FileWriter class below writes plain text. Extend it using the Decorator " +
            "pattern so that callers can optionally layer: " +
            "(a) BufferedFileWriter that buffers writes and flushes on close; " +
            "(b) CompressedFileWriter that GZIP-compresses the output; " +
            "(c) EncryptedFileWriter that AES-encrypts the output. " +
            "Decorators should be stackable in any order.",
            """
interface DataWriter extends AutoCloseable {
    void write(byte[] data) throws Exception;
}
class FileWriter implements DataWriter {
    private final OutputStream out;
    FileWriter(String path) throws Exception {
        this.out = new FileOutputStream(path);
    }
    public void write(byte[] data) throws Exception { out.write(data); }
    public void close() throws Exception { out.close(); }
}""",
            "Abstract base: class WriterDecorator implements DataWriter { protected final DataWriter inner; ... }. " +
            "BufferedFileWriter: accumulates bytes in a ByteArrayOutputStream, flushes to inner on close or when buffer exceeds threshold. " +
            "CompressedFileWriter: wraps inner in GZIPOutputStream. " +
            "EncryptedFileWriter: uses AES/CBC cipher, writes IV prefix then ciphertext. " +
            "Usage: new EncryptedFileWriter(new CompressedFileWriter(new FileWriter(\"out.bin\"))).")
        );
    }

    // =========================================================================
    // TRACE (15)
    // =========================================================================

    private static List<Question> traceQuestions() {
        return List.of(

        // ---- EASY (5) -------------------------------------------------------

        new Question(TRACE, "Loops", "01", EASY,
            "Write the exact output produced when the following code runs.",
            """
int product = 1;
for (int i = 2; i <= 5; i++) {
    product *= i;
    System.out.println(i + " -> " + product);
}""",
            "2 -> 2\n3 -> 6\n4 -> 24\n5 -> 120"),

        new Question(TRACE, "Strings", "02", EASY,
            "Write the exact output produced when the following code runs.",
            """
String s = "  Hello, World!  ";
System.out.println(s.trim().length());
System.out.println(s.trim().split(",").length);
System.out.println(s.trim().replace("World", "Java").toLowerCase());
System.out.println("hello".compareTo("world") < 0);""",
            "13\n2\nhello, java!\ntrue\n" +
            "(trim removes outer spaces giving 13 chars; split on comma gives 2 parts; " +
            "compareTo: 'h'(104) < 'w'(119) so negative, hence true.)"),

        new Question(TRACE, "Collections", "02", EASY,
            "Write the exact output produced when the following code runs. " +
            "Note which Map implementation is used.",
            """
Map<String, Integer> scores = new TreeMap<>();
scores.put("Charlie", 85);
scores.put("Alice",   92);
scores.put("Bob",     78);
for (var entry : scores.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}""",
            "Alice: 92\nBob: 78\nCharlie: 85\n" +
            "(TreeMap iterates in ascending natural key order — alphabetical for Strings.)"),

        new Question(TRACE, "Loops", "01", EASY,
            "Write the exact output produced when the following code runs.",
            """
int count = 0;
for (int i = 1; i <= 4; i++) {
    for (int j = 1; j <= i; j++) {
        count++;
    }
    System.out.println("i=" + i + " count=" + count);
}""",
            "i=1 count=1\ni=2 count=3\ni=3 count=6\ni=4 count=10"),

        new Question(TRACE, "Strings", "02", EASY,
            "Write the exact output produced when the following code runs.",
            """
String s = "programming";
System.out.println(s.charAt(0));
System.out.println(s.indexOf('g'));
System.out.println(s.lastIndexOf('g'));
System.out.println(s.substring(3, 7));
System.out.println(s.toUpperCase().startsWith("PRO"));""",
            "p\n3\n10\ngram\ntrue\n" +
            "(indexOf finds first 'g' at index 3; lastIndexOf finds last 'g' at index 10; " +
            "substring(3,7) = chars at indices 3,4,5,6 = \"gram\".)"),

        // ---- MEDIUM (7) -----------------------------------------------------

        new Question(TRACE, "Recursion", "10", MEDIUM,
            "Write the exact output produced when the following code runs.",
            """
static String reverse(String s) {
    if (s.isEmpty()) return "";
    return reverse(s.substring(1)) + s.charAt(0);
}
// In main:
System.out.println(reverse("Java"));
System.out.println(reverse("ab"));
System.out.println(reverse(""));""",
            "avaJ\nba\n(empty line)\n" +
            "(reverse(\"\") returns \"\" and println adds a newline, printing a blank line.)"),

        new Question(TRACE, "Polymorphism", "05", MEDIUM,
            "Write the exact output produced when the following code runs. " +
            "Pay close attention to which method is dispatched at runtime.",
            """
class Vehicle {
    String type() { return "Vehicle"; }
    void describe() { System.out.println("I am a " + type()); }
}
class Car extends Vehicle {
    @Override String type() { return "Car"; }
}
class Truck extends Vehicle {
    @Override String type() { return "Truck"; }
}
Vehicle v = new Car();
v.describe();
System.out.println(v instanceof Car);
System.out.println(v instanceof Truck);
System.out.println(v.type());""",
            "I am a Car\ntrue\nfalse\nCar\n" +
            "(describe() is defined in Vehicle but calls type(), which dispatches dynamically " +
            "to Car.type(). This is runtime polymorphism — the actual object's method wins.)"),

        new Question(TRACE, "Exceptions", "03", MEDIUM,
            "Write the exact output produced when the following code runs.",
            """
static int divide(int a, int b) {
    try {
        return a / b;
    } finally {
        System.out.println("finally in divide");
    }
}
try {
    System.out.println("result: " + divide(10, 2));
    System.out.println("result: " + divide(6, 0));
} catch (ArithmeticException e) {
    System.out.println("caught: " + e.getMessage());
}
System.out.println("done");""",
            "finally in divide\nresult: 5\nfinally in divide\ncaught: / by zero\ndone\n" +
            "(finally always runs — even when a / by zero propagates out of the inner try.)"),

        new Question(TRACE, "Classes", "02", MEDIUM,
            "Write the exact output produced when the following code runs.",
            """
class Ticket {
    private static int nextId = 1;
    private final int id;
    Ticket() { this.id = nextId++; }
    @Override public String toString() { return "Ticket#" + id; }
}
Ticket a = new Ticket();
Ticket b = new Ticket();
System.out.println(a);
System.out.println(b);
Ticket c = new Ticket();
System.out.println(c);
System.out.println(new Ticket());""",
            "Ticket#1\nTicket#2\nTicket#3\nTicket#4\n" +
            "(nextId is static — shared across all instances. Each constructor call post-increments it.)"),

        new Question(TRACE, "Interfaces", "05", MEDIUM,
            "Write the exact output produced when the following code runs.",
            """
interface Greeter {
    String name();
    default String greet() { return "Hello, " + name() + "!"; }
}
class Formal implements Greeter {
    public String name() { return "Dr. Smith"; }
    @Override public String greet() { return "Good day, " + name() + "."; }
}
class Casual implements Greeter {
    public String name() { return "Alex"; }
}
Greeter f = new Formal();
Greeter c = new Casual();
System.out.println(f.greet());
System.out.println(c.greet());
System.out.println(f.name().equals(c.name()));""",
            "Good day, Dr. Smith.\nHello, Alex!\nfalse\n" +
            "(Formal overrides greet(); Casual inherits the default. " +
            "equals() compares string content; \"Dr. Smith\" != \"Alex\".)"),

        new Question(TRACE, "Lambdas", "09", MEDIUM,
            "Write the exact output produced when the following code runs.",
            """
Function<String, String> trim    = String::trim;
Function<String, Integer> length = String::length;
Function<String, Integer> trimLen = trim.andThen(length);

System.out.println(trimLen.apply("  hello  "));
System.out.println(trimLen.apply("Java"));
System.out.println(trimLen.apply("   "));""",
            "5\n4\n0\n" +
            "(trim removes whitespace first: \"  hello  \"→\"hello\"(5), \"Java\"→\"Java\"(4), \"   \"→\"\"(0).)"),

        new Question(TRACE, "Inheritance", "05", MEDIUM,
            "Write the exact output produced when the following code runs.",
            """
class Shape {
    Shape(String color) { System.out.println("Shape: " + color); }
}
class Circle extends Shape {
    Circle(String color, double r) {
        super(color);
        System.out.println("Circle r=" + r);
    }
}
class ColoredCircle extends Circle {
    ColoredCircle(String color, double r) {
        super(color, r);
        System.out.println("ColoredCircle done");
    }
}
new ColoredCircle("red", 3.0);""",
            "Shape: red\nCircle r=3.0\nColoredCircle done\n" +
            "(Constructor chain runs top-down: Shape first, then Circle, then ColoredCircle.)"),

        // ---- HARD (3) -------------------------------------------------------

        new Question(TRACE, "Streams", "09", HARD,
            "Write the exact output produced when the following code runs. " +
            "List the lines in the order they appear.",
            """
List<String> words = List.of(
    "apple", "banana", "cherry", "avocado", "blueberry");
words.stream()
     .filter(w -> w.startsWith("a") || w.startsWith("b"))
     .map(String::toUpperCase)
     .sorted()
     .forEach(System.out::println);""",
            "APPLE\nAVOCADO\nBANANA\nBLUEBERRY\n" +
            "(cherry is filtered out; the remaining four are uppercased then sorted alphabetically.)"),

        new Question(TRACE, "Streams", "09", HARD,
            "Write the exact output produced when the following code runs.",
            """
List<String> words = List.of("cat", "car", "dog", "door", "cat", "deer");
Map<Character, Long> freq = words.stream()
    .collect(Collectors.groupingBy(
        w -> w.charAt(0),
        Collectors.counting()));
new TreeMap<>(freq).forEach(
    (k, v) -> System.out.println(k + "=" + v));""",
            "c=3\nd=3\n" +
            "(cat appears twice + car once = 3 words starting with 'c'; " +
            "dog + door + deer = 3 words starting with 'd'. TreeMap sorts keys alphabetically.)"),

        new Question(TRACE, "Generics", "09", HARD,
            "Write the exact output produced when the following code runs. " +
            "Pay attention to method overloading resolution.",
            """
static String describe(Object o)  { return "Object: "  + o; }
static String describe(String s)  { return "String: "  + s; }
static String describe(Integer i) { return "Integer: " + i; }

System.out.println(describe("hello"));
System.out.println(describe(42));
System.out.println(describe((Object) "test"));
System.out.println(describe(Integer.valueOf(7)));""",
            "String: hello\nInteger: 42\nObject: test\nInteger: 7\n" +
            "(The compiler picks the most specific applicable overload at compile time. " +
            "\"hello\" matches String; 42 auto-boxes to Integer; cast to Object forces Object overload; " +
            "Integer.valueOf(7) is already boxed, matches Integer.)")
        );
    }

    // =========================================================================
    // DESIGN (14)
    // =========================================================================

    private static List<Question> designQuestions() {
        return List.of(

        // ---- EASY (2) -------------------------------------------------------

        new Question(DESIGN, "Classes", "03", EASY,
            "Design a simple BankAccount class. It should store an owner name and a balance. " +
            "List the fields, constructor, and methods you would include " +
            "(deposit, withdraw, getBalance). Describe what should happen if withdraw " +
            "is called with an amount greater than the balance.",
            "",
            "Fields: String owner, double balance. " +
            "Constructor: BankAccount(String owner, double initialBalance). " +
            "Methods: void deposit(double amount) — add to balance; " +
            "void withdraw(double amount) — throw IllegalArgumentException if amount>balance, else subtract; " +
            "double getBalance() — return balance. " +
            "Consider adding toString() for display."),

        new Question(DESIGN, "Methods", "01", EASY,
            "Describe how you would design a static utility class MathUtils with three methods: " +
            "int clamp(int value, int min, int max) that keeps value in [min,max], " +
            "boolean isPrime(int n) that tests primality, and " +
            "int gcd(int a, int b) that computes the greatest common divisor. " +
            "For each method, state the algorithm in plain English.",
            "",
            "clamp: return Math.max(min, Math.min(max, value)). " +
            "isPrime: handle n<2 false; trial-divide 2..sqrt(n), return false if any divides evenly, else true. " +
            "gcd: Euclidean algorithm — while b != 0 { int t=b; b=a%b; a=t; } return a. " +
            "All methods static; constructor private to prevent instantiation."),

        // ---- MEDIUM (5) -----------------------------------------------------

        new Question(DESIGN, "Data Structures", "02", MEDIUM,
            "Design a command-line student grade tracker for a teacher. " +
            "The system must: store students with their grades per assignment, " +
            "compute each student's average and letter grade (A≥90, B≥80, C≥70, D≥60, F otherwise), " +
            "and generate a class summary sorted by average descending. " +
            "Describe the classes and data structures you would use and justify your choices.",
            "",
            "class GradeBook: Map<String,List<Double>> grades (name→scores). " +
            "Methods: addScore, double average(name), char letterGrade(name), void printReport(). " +
            "Use LinkedHashMap to preserve entry order; sort a copy by average for the report. " +
            "Alternatively: record Student(String name, List<Double> scores); store in List<Student> " +
            "and sort with a Comparator."),

        new Question(DESIGN, "OOP Design", "05", MEDIUM,
            "Design a contact book application. Users can add contacts (name, phone, email), " +
            "search by name (partial match), list all contacts sorted by last name, " +
            "and delete a contact. Describe every class and interface, " +
            "the data structure backing the contact list, and how search is implemented.",
            "",
            "record Contact(String name, String phone, String email). " +
            "class ContactBook: List<Contact> contacts (or TreeMap<String,Contact> by name). " +
            "Methods: add(Contact), List<Contact> search(String query) filters by name.contains(query), " +
            "List<Contact> sorted() returns a copy sorted by last name (split on last space). " +
            "delete(String name) removes first match. Interface Displayable: void display() for UI flexibility."),

        new Question(DESIGN, "Information Theory", "10", MEDIUM,
            "Design a lossless text compression utility. It should: read a text file, " +
            "compute character frequencies, build a Huffman tree, encode the file as a " +
            "compact bit stream, and produce a self-contained output file that includes " +
            "the tree (or frequency table) so the original can be reconstructed. " +
            "Describe every class and the binary file format.",
            "",
            "class FrequencyCounter: Map<Character,Integer> frequencies from input. " +
            "sealed interface HuffNode permits HuffLeaf, HuffBranch. " +
            "class HuffmanTree: build from PriorityQueue<HuffNode> ordered by frequency; " +
            "Map<Character,String> buildCodeTable() walks tree. " +
            "class BitOutputStream wraps OutputStream for bit-level writing. " +
            "class HuffmanFile format: header = frequency table (char-count pairs), " +
            "body = encoded bits, footer = total bit count for correct flush."),

        new Question(DESIGN, "Collections", "02", MEDIUM,
            "Design a social-network graph where users follow each other. " +
            "The system must: add users, follow/unfollow, check if A follows B, " +
            "list all people a user follows, list all followers of a user, " +
            "and compute the shortest follow-chain between two users (degree of separation). " +
            "Which data structures would you use? How does the BFS for degree-of-separation work?",
            "",
            "Map<String, Set<String>> following (user → set of who they follow). " +
            "Map<String, Set<String>> followers (user → set of who follows them). " +
            "Maintain both maps in sync on follow/unfollow for O(1) lookups. " +
            "BFS for degree: queue starts with source user, visited set prevents cycles, " +
            "expand each user's following set level by level, return level count when target found."),

        new Question(DESIGN, "OOP Design", "05", MEDIUM,
            "Design a simple task scheduler. Tasks have a name, a priority (LOW/MEDIUM/HIGH), " +
            "a due date, and a status (PENDING/DONE). Users can add tasks, mark done, " +
            "list all pending tasks sorted by priority then due date, and list overdue tasks. " +
            "Describe every class, enum, and the data structure backing the scheduler.",
            "",
            "enum Priority { LOW, MEDIUM, HIGH } enum Status { PENDING, DONE } " +
            "record Task(String name, Priority priority, LocalDate dueDate, Status status). " +
            "class TaskScheduler: List<Task> tasks. " +
            "pending(): filter status==PENDING, sort by priority.ordinal() desc then dueDate asc. " +
            "overdue(): filter status==PENDING && dueDate.isBefore(LocalDate.now()). " +
            "Tasks are immutable records; markDone returns a new Task with status=DONE."),

        // ---- HARD (7) -------------------------------------------------------

        new Question(DESIGN, "OOP Design", "05", HARD,
            "Design a Library Management System that can: add and remove books, " +
            "register members, check out a book to a member (preventing double checkout), " +
            "and return a book. Use at least one interface and one enum. " +
            "Describe every class, interface, and enum; list key fields and methods; " +
            "and explain the relationships between them.",
            "",
            "enum BookStatus { AVAILABLE, CHECKED_OUT }. " +
            "interface Loanable { void checkOut(Member m); void returnItem(); boolean isAvailable(); }. " +
            "class Book implements Loanable: isbn, title, author, BookStatus status, Member currentMember. " +
            "class Member: memberId, name, List<Book> loans. " +
            "class Library: Map<String,Book> catalog, List<Member> members; " +
            "checkOut(isbn, memberId) verifies availability then calls book.checkOut(member). " +
            "Library aggregates Members and Books; Book holds a reference to its current Member."),

        new Question(DESIGN, "Concurrency", "09", HARD,
            "Design a multi-threaded word-frequency counter for a large list of text files. " +
            "Count word frequencies across all files in parallel, merge into a single map, " +
            "and report the top-10 most frequent words. " +
            "Describe the partitioning strategy, which java.util.concurrent classes you use, " +
            "how you safely merge results, and how you find the top-10.",
            "",
            "One Callable<Map<String,Long>> per file submitted to ExecutorService. " +
            "Each callable reads its file, splits on whitespace, builds a local frequency map. " +
            "Collect futures; after all complete merge into ConcurrentHashMap using merge(word, count, Long::sum). " +
            "Top-10: sort entries by value descending, take first 10. " +
            "ConcurrentHashMap avoids contention; sequential merge after all futures is simpler."),

        new Question(DESIGN, "Design Patterns", "10", HARD,
            "Design a plugin system for a text-processing tool. " +
            "Plugins transform text (uppercase, word count, line numbering, find-and-replace). " +
            "New plugins must be addable without modifying any existing code. " +
            "Describe every interface and class, which design pattern(s) you apply, " +
            "and how a caller registers and invokes plugins.",
            "",
            "Pattern: Strategy (each plugin is a strategy) + Chain of Responsibility (pipeline). " +
            "@FunctionalInterface interface TextPlugin { String apply(String text); } " +
            "class PluginRunner: List<TextPlugin> plugins; addPlugin; String run(String text) pipes each in order. " +
            "Plugins are lambdas: String::toUpperCase, text->IntStream.range... etc. " +
            "Open/Closed: adding a plugin = new lambda, zero changes to PluginRunner."),

        new Question(DESIGN, "Persistence", "09", HARD,
            "Design the data layer for a simple e-commerce shopping cart. " +
            "A cart holds line items; each item references a product (name, SKU, price) " +
            "and a quantity. The cart must: compute total cost, apply a percentage discount, " +
            "generate a plain-text receipt, and save/load to disk. " +
            "Describe your class hierarchy, any interfaces, and your persistence strategy.",
            "",
            "record Product(String sku, String name, double price) implements Serializable. " +
            "record LineItem(Product product, int qty) { double subtotal(){return product.price()*qty;} }. " +
            "class Cart implements Serializable: List<LineItem> items, String discountCode; " +
            "addItem, removeItem, double total() applies discount, String receipt() formats items. " +
            "save/load via ObjectOutputStream/InputStream. Or use JSON (e.g. Jackson) for human-readable files."),

        new Question(DESIGN, "Information Representation", "10", HARD,
            "Design a file integrity checker. Given a directory of files, the system should: " +
            "compute a SHA-256 hash for each file, store hashes in a manifest, " +
            "and on a subsequent run detect added, removed, and modified files. " +
            "Handle large files efficiently. Describe all classes and the manifest format.",
            "",
            "class FileManifest: Map<String,String> pathToHash. Serialized as text: one 'path=hex' line each. " +
            "class IntegrityChecker: build(Path dir) walks the tree, hashes each file with " +
            "MessageDigest.getInstance(\"SHA-256\") reading in 8 KB chunks (avoids loading large files). " +
            "compare(old, current) returns Report with sets: added, removed, modified, unchanged. " +
            "Main: if manifest exists load it, build current, compare and print report, save current."),

        new Question(DESIGN, "Algorithms", "10", HARD,
            "Design a simple spell checker. Given a dictionary of correct words, the system " +
            "should: load the dictionary into an efficient data structure, check if a word is " +
            "correctly spelled, and suggest up to 5 corrections for a misspelled word ranked by " +
            "edit distance. Describe the data structures, the edit-distance algorithm, and " +
            "how suggestions are generated and ranked.",
            "",
            "Dictionary: HashSet<String> for O(1) membership test; alternatively a Trie for prefix queries. " +
            "Edit distance: Levenshtein DP table O(mn) per pair. " +
            "Suggestions: for each dictionary word compute levenshtein(input, word); " +
            "collect all with distance <= 2 into a PriorityQueue ordered by distance; return top 5. " +
            "Optimisation: pre-filter by word length difference > threshold before computing full DP."),

        new Question(DESIGN, "Design Patterns", "10", HARD,
            "Design a rate limiter using the token-bucket algorithm. " +
            "The limiter should allow at most N requests per second per client (identified by a key). " +
            "If a client exceeds the rate, the request is rejected. The system must be thread-safe " +
            "and support thousands of distinct clients. " +
            "Describe every class, the token-bucket state, and how concurrency is handled.",
            "",
            "class TokenBucket: double tokens, long lastRefill, double maxTokens, double refillRate. " +
            "synchronized boolean tryAcquire(): compute elapsed time since lastRefill, " +
            "add elapsed*refillRate tokens (capped at max), if tokens>=1 subtract 1 return true else false. " +
            "class RateLimiter: ConcurrentHashMap<String,TokenBucket> buckets. " +
            "boolean allow(String clientKey): buckets.computeIfAbsent(key, k->new TokenBucket(N,N)).tryAcquire(). " +
            "ConcurrentHashMap handles concurrent client creation; TokenBucket synchronizes per-bucket state.")
        );
    }
}
