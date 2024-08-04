package ru.emelkrist.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerManager {
    private static int uniqueId;
    private final int port;
    private final SimpleDateFormat formatter;
    private List<ClientThread> clientThreads;
    private boolean isRunning;
    private ServerSocket serverSocket;
    public static final String notifSignal = " *** ";

    public ServerManager(int port) {
        this.port = port;
        formatter = new SimpleDateFormat("HH:mm:ss");
        clientThreads = new ArrayList<>();
    }

    /**
     * Method to run server.
     */
    void runServer() {
        isRunning = true;
        try {
            serverSocket = new ServerSocket(port);
            while (isRunning) {
                displayMessage("Server is waiting for clients on port: " + port + ".");
                Socket socket = serverSocket.accept();
                if (!isRunning) break;
                ClientThread newClientThread = new ClientThread(this, socket, ++uniqueId);
                clientThreads.add(newClientThread);
                newClientThread.start();
            }
            stopServer();
        } catch (IOException e) {
            displayMessage("Server socket exception: " + e.getMessage());
        }
    }

    /**
     * Method to stop the server.
     */
    private void stopServer() {
        isRunning = false;
        try {
            serverSocket.close();
            for (int i = 0; i < clientThreads.size(); i++) {
                ClientThread clientThread = clientThreads.get(i);
                clientThread.closeStreams();
            }
        } catch (IOException e) {
            displayMessage("Stopping server exception: " + e.getMessage());
        }
    }

    /**
     * Method to display the message to the console.
     *
     * @param message displayed message
     */
    void displayMessage(String message) {
        String time = formatter.format(new Date());
        System.out.println(time + "| " + message);
    }

    /**
     * Method for multi-threaded processing of a received message.
     *
     * @param message
     * @return true/false
     */
    synchronized boolean processMessage(String message) {
        String[] messagePartitions = message.split(" ", 3);
        boolean isPrivateMessage =
                messagePartitions[1].charAt(0) == '@'
                        ? true
                        : false;

        if (isPrivateMessage) {
            if (!sendPrivateMessage(messagePartitions)) return false;
        } else {
            sendBroadcastMessage(message);
        }
        return true;
    }

    /**
     * Method to send a private message to a specific client.
     *
     * @param messagePartitions parted message
     * @return true/false
     */
    private boolean sendPrivateMessage(String[] messagePartitions) {
        String time = formatter.format(new Date());
        String fullMessage = time + "| " + messagePartitions[0] + messagePartitions[2] + "\n";
        String recipientUsername = messagePartitions[1].substring(1, messagePartitions[1].length());
        boolean recipientIsFound = false;

        for (int i = clientThreads.size() - 1; i >= 0; i--) {
            ClientThread client = clientThreads.get(i);
            String clientUsername = client.getUsername();
            if (recipientUsername.equals(clientUsername)) {
                // try to send the message to the client
                // or remove him from the list if sending if failure
                if (!client.writeMessage(fullMessage)) {
                    clientThreads.remove(i);
                    displayMessage("Disconnected client " + client.getUsername() + " removed from the list.");
                }
                recipientIsFound = true;
                break;
            }
        }
        return recipientIsFound;
    }

    /**
     * Method to send a broadcast message to all clients.
     *
     * @param message message
     */
    private void sendBroadcastMessage(String message) {
        String time = formatter.format(new Date());
        displayMessage(message);
        String fullMessage = time + "| " + message + "\n";

        for (int i = clientThreads.size() - 1; i >= 0; i--) {
            ClientThread client = clientThreads.get(i);
            if (!client.writeMessage(fullMessage)) {
                clientThreads.remove(i);
                displayMessage("Disconnected client " + client.getUsername() + " removed from the list.");
            }
        }
    }

    /**
     * Method for logging client out of the server by their ID.
     *
     * @param id client identifier
     */
    void disableClientById(int id) {
        for (int i = 0; i < clientThreads.size(); i++) {
            ClientThread client = clientThreads.get(i);
            if (id == client.getId()) {
                String clientUsername = client.getUsername();
                clientThreads.remove(i);
                String message = notifSignal + clientUsername
                        + " has lest the chat room." + notifSignal;
                sendBroadcastMessage(message);
                break;
            }
        }
    }

    List<ClientThread> getClientThreads() {
        return clientThreads;
    }
}
