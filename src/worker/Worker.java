package worker;

import master.MasterInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import utils.BruteForce;
import utils.HashComparer;

public class Worker extends UnicastRemoteObject implements WorkerInterface {
    private volatile boolean stopFlag = "false".equals(System.getProperty("worker.stopFlag"));
    private final String workerId;
    private MasterInterface master;

    protected Worker(String workerId) throws RemoteException {
        super();
        this.workerId = workerId;
    }

    @Override
    public void executeTask(String taskId, String taskDetails) throws RemoteException {
        stopFlag = false; // Reset the stop flag for new tasks
        System.out.println("Worker " + workerId + " received task " + taskId + ": " + taskDetails);
    
        // Start a new thread to handle the brute force operation.
        new Thread(() -> {
            try {
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
    
                if (bruteForce.isPasswordFound() && crackedPassword != null) {
                    master.receiveResult(workerId, taskId, crackedPassword);
                } else {
                    master.receiveResult(workerId, taskId, "Password not found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    master.receiveResult(workerId, taskId, "Error occurred: " + e.getMessage());
                } catch (RemoteException re) {
                    re.printStackTrace();
                }
            }
        }).start();
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
    @Override
    public void stopTask(String taskId) throws RemoteException {
        System.out.println("Worker " + workerId + " received stop signal for task " + taskId);
        stopFlag = true;
    }


    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java worker.Worker <workerId> <masterHost>");
            return;
        }

        String workerId = args[0];
        String masterHost = args[1];
        String hostName = "192.168.0.20";
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
