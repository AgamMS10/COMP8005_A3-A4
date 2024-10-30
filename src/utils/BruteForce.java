package utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BruteForce {
    private final HashComparer hashComparer;
    private static final char[] CHAR_SET;
    private static final int MAX_PASSWORD_LENGTH = 10; // Adjust as needed
    private volatile String foundPassword = null;
    private final AtomicBoolean passwordFound = new AtomicBoolean(false);
    private int numThreads;

    // Static block to initialize CHAR_SET with all 256 byte values
    static {
        CHAR_SET = new char[256];
        for (int i = 0; i < 256; i++) {
            CHAR_SET[i] = (char) i;
        }
    }

    public BruteForce(HashComparer hashComparer) {
        this.hashComparer = hashComparer;
        this.numThreads = Runtime.getRuntime().availableProcessors();
    }

    // Overloaded constructor to accept number of threads
    public BruteForce(HashComparer hashComparer, int numThreads) {
        this.hashComparer = hashComparer;
        this.numThreads = numThreads;
    }

    /**
     * Starts the brute-force attack using multiple threads.
     *
     * @return The cracked password if found, otherwise null.
     */
    public String crackPassword() {
        System.out.println("Starting brute-force attack with " + numThreads + " thread(s)...");
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        try {
            for (int length = 1; length <= MAX_PASSWORD_LENGTH; length++) {
                if (foundPassword != null) break; // Early exit if password is found

                // Generate initial tasks by fixing the first character
                generateAndSubmitTasks(executor, length);

                // Wait for all tasks at this length to finish or until password is found
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

                // If password not found, prepare executor for next length
                if (foundPassword == null) {
                    executor = Executors.newFixedThreadPool(numThreads);
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Brute-force attack completed in " + (endTime - startTime) / 1000 + " seconds.");

        return foundPassword;
    }

    private void generateAndSubmitTasks(ExecutorService executor, int length) {
        for (char c : CHAR_SET) {
            if (foundPassword != null) break; // Early exit if password is found
            char[] candidate = new char[length];
            candidate[0] = c;
            Runnable task = new BruteForceTask(candidate, 1, length);
            executor.execute(task);
        }
    }

    private class BruteForceTask implements Runnable {
        private final char[] candidate;
        private final int index;
        private final int length;

        public BruteForceTask(char[] candidate, int index, int length) {
            this.candidate = candidate;
            this.index = index;
            this.length = length;
        }

        @Override
        public void run() {
            bruteForceIterative(candidate, index, length);
        }

        private void bruteForceIterative(char[] candidate, int index, int length) {
            if (passwordFound.get()) return;

            if (index == length) {
                String attempt = new String(candidate);
                if (hashComparer.compare(attempt)) {
                    foundPassword = attempt;
                    passwordFound.set(true);
                    // System.out.println("Password found: " + attempt); // Uncomment for debugging
                }
                return;
            }

            for (char c : CHAR_SET) {
                if (passwordFound.get()) return;
                candidate[index] = c;
                bruteForceIterative(candidate, index + 1, length);
            }
        }
    }
}
