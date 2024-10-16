import java.util.List;
import java.util.Scanner;

import utils.DictionaryAttack;
import utils.HashComparer;
import utils.PasswordListLoader;
import utils.ShadowParser;
import utils.BruteForce;


public class App {
    public static void main(String[] args) {
        // Initialize Scanner for user input
        Scanner scanner = new Scanner(System.in);
        
        // Prompt user for the username
        System.out.print("Enter the username to test: ");
        String username = scanner.nextLine();
        
        // Prompt user for the shadow file path
        System.out.print("Enter the path to the shadow file (default: src/shadow): ");
        String shadowFilePath = scanner.nextLine();
        if (shadowFilePath.isEmpty()) {
            shadowFilePath = "src/shadow";
        }
        
        // Prompt user for the password list file path
        System.out.print("Enter the path to the password list file (default: lib/common_passwords.txt): ");
        String passwordListPath = scanner.nextLine();
        if (passwordListPath.isEmpty()) {
            passwordListPath = "lib/common_passwords.txt";
        }
        
        // Close the scanner as it's no longer needed

        
        try {
            // Parse the shadow file to get the hashed password and salt
            ShadowParser shadowParser = new ShadowParser(shadowFilePath);
            ShadowParser.ShadowEntry entry = shadowParser.getShadowEntry(username);
            
            if (entry == null) {
                System.out.println("Username not found in the shadow file.");
                scanner.close();
                return;
            }
            
            // Load the list of common passwords
            PasswordListLoader loader = new PasswordListLoader(passwordListPath);
            List<String> passwordList = loader.loadPasswords();
            System.out.println("Loaded " + passwordList.size() + " passwords from the list.");
            // Initialize the hash comparer
            HashComparer passwordHash = new HashComparer(entry.getPasswordHash());
            
            // Initialize and start the dictionary attack
            DictionaryAttack dictionary = new DictionaryAttack(
                passwordList, 
                passwordHash
            );
            String dictionaryPassword = dictionary.startDictionary();
            
            if (dictionaryPassword != null) {
                System.out.println("Password cracked In Dictionary! The password is: " + dictionaryPassword);
                scanner.close();
            } else {
                System.out.print("Dictionary attack failed. Do you want to start a brute-force attack? (yes/no): ");
                String response = scanner.nextLine();
                if (response.equalsIgnoreCase("yes")) {
                    // Initialize and start the brute-force attack
                    BruteForce bruteForce = new BruteForce(passwordHash);
                    String bruteCrackedPassword = bruteForce.crackPassword();
    
                    if (bruteCrackedPassword != null) {
                        System.out.println("Password cracked via brute-force! The password is: " + bruteCrackedPassword);
                    } else {
                        System.out.println("Failed to crack the password using brute-force.");
                    }
                }
                scanner.close();
            }
            
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            // Optionally, print stack trace for debugging
            // e.printStackTrace();
        }
    }
}
