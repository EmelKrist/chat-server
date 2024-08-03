package ru.emelkrist.client;

import ru.emelkrist.models.ChatMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientManager {
    private final String server;
    private final int port;
    private String username;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    public ClientManager(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /**
     * Method to run client.
     *
     * @return true/false
     */
    boolean runClient() {
        // connect to server
        try {
            socket = new Socket(server, port);
            // handle exception if it failed
        } catch (Exception e) {
            displayMessage("Error connecting to server: " + e.getMessage());
            return false;
        }
        // display a message about successful connection
        displayMessage("Connection successfully accepted on "
                + socket.getInetAddress() + ":"
                + socket.getPort());
        // create both data streams
        if (!initializeStreams()) return false;
        // create thread to listen the server
        new ServerListener(sInput, this).start();
        // client login
        return login();
    }

    /**
     * Method to initialize I/O streams.
     *
     * @return true/false
     */
    private boolean initializeStreams() {
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            displayMessage("I/O streams creation exception: " + e.getMessage());
            disconnect();
            return false;
        }
        return true;
    }

    /**
     * Method to display message to console.
     *
     * @param message message to display
     */
    void displayMessage(String message) {
        System.out.println(message);
    }

    /**
     * Method to send message to the server.
     *
     * @param message message to send
     */
    public void sendMessage(ChatMessage message) {
        try {
            sOutput.writeObject(message);
        } catch (IOException e) {
            displayMessage("Error sending message to server: " + e.getMessage());
        }
    }

    /**
     * Method to login in the system.
     *
     * @return true/false
     */
    private boolean login() {
        /** send username to the server
         P.S other messages will be ChatMessage objects but
         not String objects */
        try {
            // todo возможно, что стоит удалять не по никнейму, а объектом
            sOutput.writeObject(username);
        } catch (IOException e) {
            displayMessage("Login error: " + e.getMessage());
            disconnect();
            return false;
        }

        return true;
    }

    /**
     * Method to disconnect from the server.
     */
    public void disconnect() {
        try {
            if (sInput != null) sInput.close();
            if (sOutput != null) sOutput.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
