package common;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface CommonInterface extends Remote {
    void submitResult(String workerId, String taskId, String result) throws RemoteException;
}
