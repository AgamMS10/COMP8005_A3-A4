package utils;

public class BruteForce {
    private final HashComparer hashComparer;
    private static final String CHAR_SET;
    private static final int MAX_PASSWORD_LENGTH = 10; // Adjust as needed
    private String foundPassword = null;

    // Static block to initialize CHAR_SET with all 256 byte values
    static {
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < 256; i++) {
            sb.append((char) i);
        }
        CHAR_SET = sb.toString();
    }

    public BruteForce(HashComparer hashComparer) {
        this.hashComparer = hashComparer;
    }

    /**
     * Starts the brute-force attack.
     *
     * @return The cracked password if found, otherwise null.
     */
    public String crackPassword() {
        System.out.println("Starting brute-force attack...");
        long startTime = System.currentTimeMillis();

        outer:
        for (int length = 1; length <= MAX_PASSWORD_LENGTH; length++) {
            char[] candidate = new char[length];
            bruteForceIterative(candidate, 0, length);
            if (foundPassword != null) break outer;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Brute-force attack completed in " + (endTime - startTime) / 1000 + " seconds.");

        return foundPassword;
    }

    /**
     * Iterative method to generate all possible combinations.
     *
     * @param candidate Current password candidate.
     * @param index     Current character index.
     * @param length    Total length of the password.
     */
    private void bruteForceIterative(char[] candidate, int index, int length) {
        if (index == length) {
            String attempt = new String(candidate);
            if (hashComparer.compare(attempt)) {
                foundPassword = attempt;
                // System.out.println("Password found: " + attempt); // Uncomment for debugging
            }
            return;
        }

        for (char c : CHAR_SET.toCharArray()) {
            candidate[index] = c;
            bruteForceIterative(candidate, index + 1, length);
            if (foundPassword != null) return; // Early exit on success
        }
    }
}
