
# Functions Overview

This document describes the core methods in the project and explains their purpose and functionality.

## 1. `App.java`

This is the main class that drives the application by interacting with the user and initiating password cracking techniques.

### `main()`
```plaintext
Pseudocode:
- Initialize Scanner for user input
- Prompt user for username
- Prompt user for shadow file path and password list file path
- Parse the shadow file to get the hashed password and salt for the given username
- Load the list of passwords from the provided password list
- Use DictionaryAttack to attempt to crack the password
- If dictionary attack fails, use BruteForce to attempt to crack the password
```

## 2. `PasswordListLoader.java`

This class is responsible for loading a list of common passwords from a file.

### `loadPasswords()`
```plaintext
Pseudocode:
- Open the file specified in the constructor
- Read each line from the file and add it to a list of passwords
- Return the list of passwords
```

## 3. `ShadowParser.java`

This class parses the shadow file and retrieves the password hash for a given username.

### `getShadowEntry()`
```plaintext
Pseudocode:
- Open the shadow file
- Read each line to find a match for the given username
- If a matching entry is found, parse the line to extract the password hash
- Return the shadow entry containing the username and password hash
```

## 4. `DictionaryAttack.java`

This class implements a dictionary-based password cracking approach.

### `attempt()`
```plaintext
Pseudocode:
- For each password in the password list:
    - Hash the password using the appropriate hashing algorithm
    - Compare the generated hash with the hash from the shadow file
    - If a match is found, return the password
- If no match is found, return null
```

## 5. `BruteForce.java`

This class implements a brute-force password cracking technique by trying every possible combination of characters.

### `attempt()`
```plaintext
Pseudocode:
- Define the set of characters to be used for brute-forcing
- Iterate through all possible combinations of characters
- Hash each combination and compare it with the target hash
- If a match is found, return the password
- If no match is found after all combinations, return null
```

## 6. `HashComparer.java`

This class provides functionality to compare password hashes.

### `compare()`
```plaintext
Pseudocode:
- Take two hashes as input
- Compare the two hashes byte by byte
- Return true if they match, false otherwise
```

These methods and their respective classes make up the core functionality of the password-cracking tool.
