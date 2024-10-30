package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DictionaryAttack {
    private List<String> passwordList;
    private HashComparer passwordHash;

    public DictionaryAttack(List<String> passwordList, HashComparer passwordHash) {
        this.passwordList = passwordList;
        this.passwordHash = passwordHash;
    }

    /**
     * Starts the Dictionary attack to find the correct password using multiple threads.
     *
     * @return The cracked password if found, or null if not found.
     */
    public String startDictionary() {
        System.out.println("Starting Dictionary Attack...");

        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        int chunkSize = passwordList.size() / numThreads;
        if (passwordList.size() % numThreads != 0) {
            chunkSize++;
        }

        List<Callable<String>> tasks = new ArrayList<>();

        // Create tasks for each chunk of the password list
        for (int i = 0; i < passwordList.size(); i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, passwordList.size());
            List<String> sublist = passwordList.subList(start, end);

            Callable<String> task = () -> {
                for (String password : sublist) {
                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }
                    boolean match = passwordHash.compare(password);
                    if (match) {
                        return password; // Return the password if found
                    }
                }
                return null; // Return null if password not found in this sublist
            };
            tasks.add(task);
        }

        try {
            // Execute all tasks and wait for them to complete
            List<Future<String>> futures = executor.invokeAll(tasks);

            for (Future<String> future : futures) {
                try {
                    String result = future.get();
                    if (result != null) {
                        executor.shutdownNow(); // Cancel all other running tasks
                        return result; // Return the cracked password
                    }
                } catch (InterruptedException | ExecutionException e) {
                    // Handle exceptions from individual tasks
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            // Handle interruption during invokeAll
            e.printStackTrace();
        } finally {
            executor.shutdownNow(); // Ensure the executor is shut down
        }

        return null; // Password not found in the list
    }
}
