import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import master.MasterInterface;
import utils.ShadowParser;

public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Connect to the master
            String masterHost = "localhost"; // Adjust if needed
            Registry registry = LocateRegistry.getRegistry(masterHost, 1099);
            MasterInterface master = (MasterInterface) registry.lookup("Master");
            System.out.println("Connected to master at " + masterHost + ":1099");

            System.out.print("Enter the username to test: ");
            String username = scanner.nextLine();
            if (username.isEmpty()) {
                System.out.println("Username cannot be empty.");
                scanner.close();
                return;
            }

            String shadowFilePath;
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

            ShadowParser shadowParser = new ShadowParser(shadowFilePath);
            ShadowParser.ShadowEntry entry = shadowParser.getShadowEntry(username);

            if (entry == null) {
                System.out.println("Username not found in the shadow file.");
                scanner.close();
                return;
            }

            String hashToCrack = entry.getPasswordHash();

            // Assign the task to master
            String taskId = "Task_" + System.currentTimeMillis();
            master.assignTask(taskId, hashToCrack);
            System.out.println("Task " + taskId + " assigned to master.");
            System.out.println("Waiting for results...");

            // Poll for results
            String result = null;
            while (result == null) {
                Thread.sleep(2000);
                result = master.getResult(taskId);
            }

            if ("Password not found".equals(result)) {
                System.out.println("Failed to crack the password using dictionary and brute-force.");
            } else {
                System.out.println("Cracked Password: " + result);
            }

            System.out.println("Exiting the program.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
