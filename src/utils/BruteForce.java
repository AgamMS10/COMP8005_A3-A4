package utils;

public class BruteForce {
    private HashComparer hashComparer;

    // Define the character set to use for brute-force
    private static final String CHAR_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int MAX_PASSWORD_LENGTH = 6; // Adjust as needed

    private String foundPassword = null;

    /**
     * Constructs a BruteForce instance with the specified HashComparer.
     *
     * @param hashComparer The HashComparer instance to use for password comparison.
     */
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

        // Start the brute-force attack
        for (int length = 1; length <= MAX_PASSWORD_LENGTH && foundPassword == null; length++) {
            bruteForceRecursive(new StringBuilder(), length);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Brute-force attack completed in " + (endTime - startTime) / 1000 + " seconds.");

        return foundPassword;
    }

    /**
     * Recursive method to generate all possible combinations.
     *
     * @param prefix    Current password prefix.
     * @param remaining The number of characters remaining to reach the target length.
     */
    private void bruteForceRecursive(StringBuilder prefix, int remaining) {
        if (foundPassword != null) {
            return;
        }

        if (remaining == 0) {
            String candidate = prefix.toString();
            if (hashComparer.compare(candidate)) {
                foundPassword = candidate;
                System.out.println("Password found: " + candidate);
            }
            return;
        }

        for (int i = 0; i < CHAR_SET.length() && foundPassword == null; i++) {
            prefix.append(CHAR_SET.charAt(i));
            bruteForceRecursive(prefix, remaining - 1);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }
}
