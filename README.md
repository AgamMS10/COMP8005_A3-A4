# COMP 8005 A3-A4 Java Password Cracking Tool

This Java project is designed to test common password cracking techniques on a shadow password file. The tool implements dictionary and brute-force attacks to find the password for a given username, using various utilities to process password hashes and compare them.

## Features

- **Dictionary Attack:** Attempts to find the password by comparing hashed values from a list of common passwords.
- **Brute-Force Attack:** Attempts all possible combinations of characters to break the password.
- **Password Hash Comparison:** Compares the provided hash with the hash generated from guessed passwords.
- **Shadow File Parsing:** Reads the shadow file to retrieve password hash and salt for the given username.

## Dependencies

The project requires the following libraries:

- `commons-codec-1.17.1.jar` - For handling various encoding mechanisms, including password hashing.
- `logback-classic-1.5.11.jar` - Logging framework for logging messages.
- `logback-core-1.5.11.jar` - Core library for Logback.
- `password4j-1.8.2.jar` - For performing cryptographic hashing and password comparison.
- `slf4j-api-2.0.16.jar` - Simple Logging Facade for Java, used by Logback.

You can find these dependencies in the `lib/libs` folder.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/AgamMS10/COMP8005_A3-A4.git
   ```
2. Compile the project using a Java build tool or manually:

   ```bash
   javac -cp "lib/libs/*:src" -d bin src/**/*.java
   ```

3. Run the project:
   ```bash
   java -cp "bin:lib/libs/*" App
   ```

## Usage

1. Run the application, and provide the following inputs:

   - Username (to search in the shadow file)
   - Path to the shadow file (default: `src/shadow`)
   - Path to the password list file (default: `lib/common_passwords.txt`)

2. The tool will attempt to crack the password using dictionary and brute-force methods.

## Directory Structure

```plaintext
|-- src
|   |-- utils
|   |   |-- PasswordListLoader.java
|   |   |-- ShadowParser.java
|   |   |-- DictionaryAttack.java
|   |   |-- HashComparer.java
|   |   `-- BruteForce.java
|   |-- App.java
|   `-- shadow
`-- lib
    |-- common_passwords.txt
    `-- libs
        |-- commons-codec-1.17.1.jar
        |-- logback-classic-1.5.11.jar
        |-- logback-core-1.5.11.jar
        |-- password4j-1.8.2.jar
        `-- slf4j-api-2.0.16.jar
```

## License

License ?
