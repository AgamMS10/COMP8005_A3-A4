package worker;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface WorkerInterface extends Remote {
    void executeTask(String taskId, String taskDetails) throws RemoteException;
}
