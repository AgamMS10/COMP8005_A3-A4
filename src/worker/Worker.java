package worker;

import master.MasterInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import utils.BruteForce;
import utils.HashComparer;

public class Worker extends UnicastRemoteObject implements WorkerInterface {

    private final String workerId;
    private MasterInterface master;

    protected Worker(String workerId) throws RemoteException {
        super();
        this.workerId = workerId;
    }

    @Override
    public void executeTask(String taskId, String taskDetails) throws RemoteException {
        System.out.println("Worker " + workerId + " received task " + taskId + ": " + taskDetails);

        // taskDetails might look like: "HASH|startChar:endChar"
        String[] parts = taskDetails.split("\\|");
        String hash = parts[0];
        int startChar = 32;
        int endChar = 126;

        if (parts.length > 1) {
            String rangePart = parts[1];
            String[] range = rangePart.split(":");
            startChar = Integer.parseInt(range[0]);
            endChar = Integer.parseInt(range[1]);
        }

        HashComparer hashComparer = new HashComparer(hash);
        BruteForce bruteForce = new BruteForce(hashComparer, startChar, endChar);
        String crackedPassword = bruteForce.crackPassword();

        if (crackedPassword != null) {
            master.receiveResult(workerId, taskId, crackedPassword);
        } else {
            master.receiveResult(workerId, taskId, "Password not found");
        }
    }

    public void registerWithMaster(String masterHost) {
        try {
            Registry registry = LocateRegistry.getRegistry(masterHost, 1099);
            master = (MasterInterface) registry.lookup("Master");
            master.registerWorker(workerId, this);
            System.out.println("Worker " + workerId + " registered with master.");
        } catch (Exception e) {
            System.err.println("Worker " + workerId + " failed to register with master: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java worker.Worker <workerId> <masterHost>");
            return;
        }

        String workerId = args[0];
        String masterHost = args[1];
        String hostName = "192.168.1.208";
        System.setProperty("java.rmi.server.hostname", hostName);

        System.out.println(System.getProperty("java.rmi.server.hostname"));
        try {
            Worker worker = new Worker(workerId);
            worker.registerWithMaster(masterHost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
