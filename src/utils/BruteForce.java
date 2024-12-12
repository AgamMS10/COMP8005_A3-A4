package utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class BruteForce {
    private final HashComparer hashComparer;
    private final char[] charSet;
    private final int numThreads;
    private volatile String foundPassword = null;
    private final AtomicBoolean passwordFound = new AtomicBoolean(false);
    private static final int MAX_PASSWORD_LENGTH = 10; // Adjust as needed

    // Full character set of printable ASCII
    private static final char[] FULL_CHAR_SET;
    static {
        StringBuilder sb = new StringBuilder();
        for (char c = 32; c <= 126; c++) { // ASCII printable characters
            sb.append(c);
        }
        FULL_CHAR_SET = sb.toString().toCharArray();
    }

    /**
     * Default constructor using the entire character set.
     */
    public BruteForce(HashComparer hashComparer) {
        this(hashComparer, 32, 126);
    }

    /**
     * Constructor that accepts a character range.
     *
     * @param hashComparer the HashComparer instance
     * @param startChar ASCII start index of char set
     * @param endChar ASCII end index of char set
     */
    public BruteForce(HashComparer hashComparer, int startChar, int endChar) {
        this.hashComparer = hashComparer;
        this.numThreads = Runtime.getRuntime().availableProcessors();
        this.charSet = createCharSubSet(startChar, endChar);
    }

    private char[] createCharSubSet(int start, int end) {
        char[] subset = new char[end - start + 1];
        for (int i = start; i <= end; i++) {
            subset[i - start] = (char) i;
        }
        return subset;
    }

    public static char[] getFullCharSet() {
        return FULL_CHAR_SET;
    }

    /**
     * Starts the brute-force attack using multiple threads.
     *
     * @return The cracked password if found, otherwise null.
     */
    public String crackPassword() {
        System.out.println("Starting brute-force attack with " + numThreads + " thread(s) and chars from '"
                + (int)charSet[0] + "' to '" + (int)charSet[charSet.length-1] + "'...");

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        try {
            for (int length = 1; length <= MAX_PASSWORD_LENGTH; length++) {
                if (passwordFound.get()) break; // Early exit if password is found
        
                // Generate initial tasks by fixing the first character
                int initialTaskCount = charSet.length;
                for (char c : charSet) {
                    if (passwordFound.get()) break; // Early exit if password is found
                    char[] candidate = new char[length];
                    candidate[0] = c;
                    completionService.submit(new BruteForceTask(candidate, 1, length));
                }
        
                // Wait for all tasks at this length
                for (int i = 0; i < initialTaskCount; i++) {
                    if (passwordFound.get()) break; // Break waiting if password found
                    Future<String> future = completionService.take(); // Blocks until a task is completed
                    String result = future.get();
                    if (result != null) {
                        foundPassword = result;
                        passwordFound.set(true);
                        break;
                    }
                }
        
                if (passwordFound.get()) break; // Double-check and exit early
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow(); // Force stop all remaining threads
        }        

        long endTime = System.currentTimeMillis();
        System.out.println("Brute-force attack completed in " + (endTime - startTime) / 1000 + " seconds.");

        return foundPassword;
    }

    private class BruteForceTask implements Callable<String> {
        private final char[] candidate;
        private final int index;
        private final int length;

        public BruteForceTask(char[] candidate, int index, int length) {
            this.candidate = candidate.clone(); // Clone to prevent shared state
            this.index = index;
            this.length = length;
        }

        @Override
        public String call() {
            return bruteForceIterative(candidate, index, length);
        }

        private String bruteForceIterative(char[] candidate, int index, int length) {
            if (passwordFound.get()) return null; // Exit if password is already found
        
            if (index == length) {
                String attempt = new String(candidate);
                if (hashComparer.compare(attempt)) {
                    passwordFound.set(true); // Signal that password is found
                    return attempt;
                }
                return null;
            }
        
            for (char c : charSet) {
                if (passwordFound.get()) return null; // Exit if password is already found
                candidate[index] = c;
                String result = bruteForceIterative(candidate, index + 1, length);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        
    }
    public boolean isPasswordFound() {
        return passwordFound.get();
    }
    
}
