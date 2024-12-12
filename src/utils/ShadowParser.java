package utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShadowParser {
    private String shadowFilePath;

    public ShadowParser(String shadowFilePath) {
        this.shadowFilePath = shadowFilePath;
    }

    /**
     * Represents an entry in the shadow file.
     */
    public static class ShadowEntry {
        private String username;
        private String passwordHash;
    
        // Getters and Setters
    
        public String getUsername() {
            return username;
        }
    
        public String getPasswordHash() {
            return passwordHash;
        }
    
        public void setUsername(String username) {
            this.username = username;
        }
    
        public void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }
    }
    

    /**
     * Retrieves the shadow entry for the specified username.
     *
     * @param username The username to search for.
     * @return ShadowEntry object containing password hash and salt, or null if not found.
     * @throws IOException If an I/O error occurs.
     */
    public ShadowEntry getShadowEntry(String username) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(shadowFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(username + ":")) {
                    String[] parts = line.split(":");
                    if (parts.length < 2) {
                        continue; // Invalid entry
                    }
                    String passwordHash = parts[1];
    
                    if (!passwordHash.isEmpty()) {
                        ShadowEntry entry = new ShadowEntry();
                        entry.setUsername(username);
                        entry.setPasswordHash(passwordHash);
                        return entry;
                    }
                    // Handle cases with empty password hashes or special formats if needed
                }
            }
        }
        return null; // Username not found
    }
    
}
