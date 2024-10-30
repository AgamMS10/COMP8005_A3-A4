package utils;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.codec.digest.Sha2Crypt;

import com.password4j.BcryptFunction;
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
                System.out.println("Algorithm ID: " + algorithmId);
                System.out.println("Salt: " + salt);
                System.err.println("Password Hash: " + passwordHash);
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
                // System.out.println("MD5 Hashing"); // Debugging
                String md5HashedPassword = Md5Crypt.md5Crypt(attemptedPassword.getBytes(), "$1$" + salt);
                // System.out.println("MD5 Hashed Password: " + md5HashedPassword); // Debugging
                return md5HashedPassword.equals(passwordHash);

            case "2a": // Blowfish variants (bcrypt)
            case "2b":
            case "2y":
            case "2":
                // For bcrypt, the target hash contains the salt and cost factors
                // System.out.println("BCrypt Hashing"); // Debugging
                BcryptFunction bcryptFunction = BcryptFunction.getInstanceFromHash(passwordHash);
                // System.out.println("BCrypt Hashed Password: " + bcryptFunction.hash(attemptedPassword)); // Debugging
                return Password.check(attemptedPassword, passwordHash).with(bcryptFunction);
            

            case "5": // SHA-256
                // System.out.println("SHA-256 Hashing"); // Debugging
                String sha256HashedPassword = Sha2Crypt.sha256Crypt(attemptedPassword.getBytes(), "$5$" + salt);
                // System.out.println("SHA-256 Hashed Password: " + sha256HashedPassword); // Debugging
                return sha256HashedPassword.equals(passwordHash);

            case "6": // SHA-512
                // System.out.println("SHA-512 Hashing"); // Debugging
                String sha512HashedPassword = Sha2Crypt.sha512Crypt(attemptedPassword.getBytes(), "$6$" + salt);
                // System.out.println("SHA-512 Hashed Password: " + sha512HashedPassword); // Debugging
                return sha512HashedPassword.equals(passwordHash);

            default:
                // Unsupported algorithm
                throw new UnsupportedOperationException("Unsupported hash algorithm: " + algorithmId);
        }
    }
}