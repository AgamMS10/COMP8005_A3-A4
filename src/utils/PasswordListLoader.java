
package utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList; 
import java.util.List;

public class PasswordListLoader {
    private String passwordListPath;

    public PasswordListLoader(String passwordListPath) {
        this.passwordListPath = passwordListPath;
    }

    /**
     * Loads passwords from the specified file.
     *
     * @return A list of passwords.
     * @throws IOException If an I/O error occurs.
     */
    public List<String> loadPasswords() throws IOException {
        List<String> passwords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(passwordListPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    passwords.add(line.trim());
                }
            }
        }
        return passwords;
    }
}
