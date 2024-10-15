package utils;


import java.util.List;

public class DictionaryAttack {
    private List<String> passwordList;
    private HashComparer passwordHash;

    public DictionaryAttack(List<String> passwordList, HashComparer passwordHash) {
        this.passwordList = passwordList;
        this.passwordHash = passwordHash;
    }
    

    /**
     * Starts the brute-force attack to find the correct password.
     *
     * @return The cracked password if found, or null if not found.
     */
    public String startDictionary() {
        for (String password : passwordList) {
            boolean match = passwordHash.compare(password);
            if (match) {
                return password; // Password cracked
            }
        }
        return null; // Password not found in the list
    }
    
}
