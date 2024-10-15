package utils;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.codec.digest.Sha2Crypt;
import com.password4j.Password;

public class HashComparer {
    private String passwordHash;
    private String algorithmId;
    private String salt;

    /**
     * Constructs a HashComparer with the specified target hash.
     *
     * @param passwordHash The hash to compare against.
     */
    public HashComparer(String passwordHash) {
        this.passwordHash = passwordHash;
        parseTargetHash();
    }

    /**
     * Parses the target hash to extract the algorithm identifier and salt.
     */
    private void parseTargetHash() {
        if (passwordHash.startsWith("$")) {
            String[] parts = passwordHash.split("\\$");
            if (parts.length >= 4) {
                algorithmId = parts[1];
                salt = parts[2];
            } else {
                throw new IllegalArgumentException("Invalid target hash format.");
            }
        } else {
            throw new IllegalArgumentException("Invalid target hash format.");
        }
    }

    /**
     * Compares the hash of the attempted password with the target hash.
     *
     * @param attemptedPassword The password to test.
     * @return True if the hashes match; false otherwise.
     */
    public boolean compare(String attemptedPassword) {
        switch (algorithmId) {
            case "1": // MD5
                String md5HashedPassword = Md5Crypt.md5Crypt(attemptedPassword.getBytes(), "$1$" + salt);
                return md5HashedPassword.equals(passwordHash);

            case "2a": // Blowfish variants (bcrypt)
            case "2b":
            case "2y":
            case "2":
                // For bcrypt, the target hash contains the salt and cost factors
                return Password.check(attemptedPassword, passwordHash).withBcrypt();

            case "5": // SHA-256
                String sha256HashedPassword = Sha2Crypt.sha256Crypt(attemptedPassword.getBytes(), "$5$" + salt);
                return sha256HashedPassword.equals(passwordHash);

            case "6": // SHA-512
                String sha512HashedPassword = Sha2Crypt.sha512Crypt(attemptedPassword.getBytes(), "$6$" + salt);
                return sha512HashedPassword.equals(passwordHash);

            default:
                // Unsupported algorithm
                throw new UnsupportedOperationException("Unsupported hash algorithm: " + algorithmId);
        }
    }
}
