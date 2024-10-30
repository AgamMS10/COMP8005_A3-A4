import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import utils.DictionaryAttack;
import utils.HashComparer;
import utils.PasswordListLoader;
import utils.ShadowParser;
import utils.BruteForce;

// Added imports for graphing
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class App {
    public static void main(String[] args) {
        // Initialize Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // Prompt user for the username
        System.out.print("Enter the username to test: ");
        String username = scanner.nextLine();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            scanner.close();
            return;
        }

        // Prompt user for the shadow file path and validate it
        String shadowFilePath = null;
        while (true) {
            System.out.print("Enter the path to the shadow file (default: src/shadow): ");
            shadowFilePath = scanner.nextLine();
            if (shadowFilePath.isEmpty()) {
                System.out.println("Using default shadow file path.");
                shadowFilePath = "src/shadow";
            }
            File shadowFile = new File(shadowFilePath);
            if (!shadowFile.exists()) {
                System.out.println("Shadow file does not exist. Please enter a valid path.");
            } else {
                break;
            }
        }

        // Parse the shadow file to get the hashed password and salt
        ShadowParser shadowParser = null;
        try {
            shadowParser = new ShadowParser(shadowFilePath);
        } catch (Exception e) {
            System.err.println("An error occurred while parsing the shadow file: " + e.getMessage());
            scanner.close();
            return;
        }

        // Check if the username exists in the shadow file
        ShadowParser.ShadowEntry entry = null;
        try {
            entry = shadowParser.getShadowEntry(username);
        } catch (IOException e) {
            System.err.println("An error occurred while retrieving the shadow entry: " + e.getMessage());
            scanner.close();
            return;
        }

        if (entry == null) {
            System.out.println("Username not found in the shadow file.");
            scanner.close();
            return;
        }

        // Prompt user for the password list file path and validate it
        String passwordListPath = null;
        while (true) {
            System.out.print("Enter the path to the password list file (default: lib/common_passwords.txt): ");
            passwordListPath = scanner.nextLine();
            if (passwordListPath.isEmpty()) {
                System.out.println("Using default password list file path.");
                passwordListPath = "lib/common_passwords.txt";
            }
            File passwordFile = new File(passwordListPath);
            if (!passwordFile.exists()) {
                System.out.println("Password list file does not exist. Please enter a valid path.");
            } else {
                break;
            }
        }

        try {
            // Load the list of common passwords
            PasswordListLoader loader = new PasswordListLoader(passwordListPath);
            List<String> passwordList = null;
            try {
                passwordList = loader.loadPasswords();
            } catch (IOException e) {
                System.err.println("An error occurred while loading the password list: " + e.getMessage());
                scanner.close();
                return;
            }

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
                System.out.println("Password cracked via Dictionary attack! The password is: " + dictionaryPassword);
            } else {
                Thread.sleep(300);
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

                    // Prompt the user to generate performance graphs
                    System.out.print("Do you want to see performance graphs comparing different thread counts? (yes/no): ");
                    String graphResponse = scanner.nextLine();
                    if (graphResponse.equalsIgnoreCase("yes")) {
                        runPerformanceTests(passwordHash);
                    } else {
                        System.out.println("Exiting the program.");
                    }
                } else {
                    System.out.println("Exiting the program.");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            // Optionally, print stack trace for debugging
            // e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // Method to run performance tests and generate graphs
    private static void runPerformanceTests(HashComparer passwordHash) {
        System.out.println("Running performance tests...");

        int[] threadCounts = {1, 2, 3, 4};
        long[] executionTimes = new long[threadCounts.length];

        for (int i = 0; i < threadCounts.length; i++) {
            int numThreads = threadCounts[i];
            System.out.println("Testing with " + numThreads + " thread(s)...");

            BruteForce bruteForce = new BruteForce(passwordHash, numThreads);

            long startTime = System.currentTimeMillis();
            bruteForce.crackPassword();
            long endTime = System.currentTimeMillis();

            long duration = endTime - startTime;
            executionTimes[i] = duration;

            // System.out.println("Execution time with " + numThreads + " thread(s): " + duration + " ms"); // Debugging
        }

        // Generate the graph
        generatePerformanceGraph(threadCounts, executionTimes);
    }

    // Method to generate performance graph using JFreeChart
    private static void generatePerformanceGraph(int[] threadCounts, long[] executionTimes) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < threadCounts.length; i++) {
            dataset.addValue(executionTimes[i], "Execution Time", threadCounts[i] + " Threads");
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Brute-Force Performance Comparison",
                "Number of Threads",
                "Execution Time (ms)",
                dataset
        );

        try {
            ChartUtils.saveChartAsJPEG(new File("BruteForcePerformance.jpg"), barChart, 800, 600);
            System.out.println("Performance graph generated: BruteForcePerformance.jpg");
        } catch (IOException e) {
            System.err.println("Failed to save the performance graph: " + e.getMessage());
        }
    }
}
