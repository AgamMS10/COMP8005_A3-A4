package master;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;

import worker.WorkerInterface;
import utils.BruteForce;
import utils.DictionaryAttack;
import utils.HashComparer;
import utils.PasswordListLoader;

public class Master extends UnicastRemoteObject implements MasterInterface {

    private final Map<String, WorkerInterface> workers = new ConcurrentHashMap<>();
    private final Map<String, String> taskResults = new ConcurrentHashMap<>();
    private List<String> passwordList;

    protected Master() throws RemoteException {
        super();
    }

    public void setPasswordList(List<String> passwordList) {
        this.passwordList = passwordList;
    }

    @Override
    public void registerWorker(String workerId, WorkerInterface worker) throws RemoteException {
        workers.put(workerId, worker);
        System.out.println("Worker registered: " + workerId + ". Total workers: " + workers.size());
    }

    @Override
    public void assignTask(String taskId, String taskDetails) throws RemoteException {
        System.out.println("Master: Attempting dictionary attack before brute-force...");

        // Run dictionary attack locally
        HashComparer hashComparer = new HashComparer(taskDetails);
        DictionaryAttack dictionaryAttack = new DictionaryAttack(passwordList, hashComparer);
        String foundPassword = dictionaryAttack.startDictionary();

        if (foundPassword != null) {
            System.out.println("Dictionary attack succeeded. Password: " + foundPassword);
            // Store the result so the client can retrieve it
            taskResults.put(taskId, foundPassword);
            return;
        }

        System.out.println("Dictionary attack failed. Assigning brute-force task to workers.");

        if (workers.isEmpty()) {
            System.out.println("No workers registered. Cannot perform brute-force.");
            taskResults.put(taskId, "Password not found");
            return;
        }

        // Divide the character set among workers
        char[] fullCharSet = BruteForce.getFullCharSet();
        int numWorkers = workers.size();
        int segmentSize = fullCharSet.length / numWorkers;
        int remainder = fullCharSet.length % numWorkers;

        int startIndex = 0;
        for (Map.Entry<String, WorkerInterface> entry : workers.entrySet()) {
            String workerId = entry.getKey();
            WorkerInterface w = entry.getValue();
            int endIndex = startIndex + segmentSize + (remainder > 0 ? 1 : 0);
            remainder = Math.max(0, remainder - 1);

            char[] subCharSet = Arrays.copyOfRange(fullCharSet, startIndex, endIndex);
            startIndex = endIndex;

            int startChar = (int) subCharSet[0];
            int endChar = (int) subCharSet[subCharSet.length - 1];
            String subTaskDetails = taskDetails + "|" + startChar + ":" + endChar;

            try {
                System.out.println("Task " + taskId + " assigned to worker " + workerId + " with range " + startChar + "-" + endChar);
                w.executeTask(taskId, subTaskDetails);
            } catch (RemoteException e) {
                System.err.println("Failed to assign task to worker " + workerId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void receiveResult(String workerId, String taskId, String result) throws RemoteException {
        taskResults.put(taskId, result);
        if (!"Password not found".equals(result)) {
            System.out.println("Received a successful crack from " + workerId + ". Password: " + result);

            // Schedule stopping tasks in a separate thread after returning from this method
            new Thread(() -> {
                try {
                    Thread.sleep(100); // small delay to ensure receiveResult returns
                } catch (InterruptedException ignored) {}
                for (Map.Entry<String, WorkerInterface> entry : workers.entrySet()) {
                    WorkerInterface worker = entry.getValue();
                    try {
                        worker.stopTask(taskId);
                    } catch (RemoteException e) {
                        System.err.println("Failed to notify worker to stop: " + e.getMessage());
                    }
                }
            }).start();
        }
    }

    @Override
    public String getResult(String taskId) throws RemoteException {
        return taskResults.get(taskId);
    }

    public static void main(String[] args) {
        int port = 1099; // Specify the port number
        String masterIp = "192.168.1.74"; // Replace with your desired IP or hostname

        try {   
            System.setProperty("java.rmi.server.hostname", masterIp);

            Registry registry = LocateRegistry.createRegistry(port);

            Master master = new Master();
    
            // Load the dictionary once at startup
            PasswordListLoader loader = new PasswordListLoader("lib/common_passwords.txt");
            List<String> passwordList = loader.loadPasswords();
            master.setPasswordList(passwordList);
    
            registry.rebind("Master", master);
            System.out.println("Master is ready and bound to registry.");
            System.out.println("Master is running on port: " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
