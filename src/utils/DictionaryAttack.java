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
                        return password;
                    }
                }
                return null;
            };
            tasks.add(task);
        }

        try {
            String result = executor.invokeAny(tasks);
            executor.shutdownNow(); // Cancel all other running tasks
            return result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }

        String result = "Dictionary attack failed.";

        return result; // Password not found in the list
    }
}
