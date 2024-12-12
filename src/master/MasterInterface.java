package master;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MasterInterface extends Remote {
    void registerWorker(String workerId, worker.WorkerInterface worker) throws RemoteException;
    void assignTask(String taskId, String taskDetails) throws RemoteException;
    void receiveResult(String workerId, String taskId, String result) throws RemoteException;
    String getResult(String taskId) throws RemoteException;
}
