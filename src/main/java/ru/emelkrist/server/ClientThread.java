package ru.emelkrist.server;

import ru.emelkrist.models.ChatMessage;
import ru.emelkrist.utils.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ClientThread extends Thread {
    private final Socket socket;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private ServerManager serverManager;
    private String username;
    private int id;
    private boolean isConnected;
    private String connectionDate;

    public ClientThread(ServerManager serverManager, Socket socket, int id) {
        this.id = id;
        this.socket = socket;
        this.serverManager = serverManager;
        initializeState();
    }

    /**
     * Method to initialize client thread state.
     */
    private void initializeState() {
        try {
            sOutput = new ObjectOutputStream(socket.getOutputStream());
            sInput = new ObjectInputStream(socket.getInputStream());
            username = sInput.readObject().toString();
            connectionDate = new Date().toString();
            serverManager.processMessage(ServerManager.notifSignal + username + " has joined the chat room." + ServerManager.notifSignal);
            isConnected = true;
        } catch (IOException e) {
            serverManager.displayMessage("Exception creation of I/O streams for client with id " + id + ".Description: " + e.getMessage());
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method ot run the client thread.
     */
    @Override
    public void run() {
        while (isConnected) {
            Optional<ChatMessage> optionalChatMessage = readMessage();
            if (optionalChatMessage.isEmpty()) {
                break;
            }
            processMessage(optionalChatMessage.get());
        }
        serverManager.disableClientById(id);
        closeStreams();
    }

    /**
     * Method to process the input message.
     *
     * @param chatMessage message
     */
    private void processMessage(ChatMessage chatMessage) {
        String message = chatMessage.getMessage();
        MessageType messageType = chatMessage.getType();
        switch (messageType) {
            case COMMUNICATION -> {
                boolean isConfirm = serverManager.processMessage(username + ": " + message);
                if (!isConfirm) {
                    writeMessage(ServerManager.notifSignal + "Sorry! No such user exist." + ServerManager.notifSignal);
                }
            }
            case LOGOUT -> {
                serverManager.processMessage(ServerManager.notifSignal + username + " is disconnected." + ServerManager.notifSignal);
                isConnected = false;
            }
            case WHOISIN -> {
                writeMessage("List of the users connected at " + new Date());
                List<ClientThread> clientThreads = serverManager.getClientThreads();
                for (int i = 0; i < clientThreads.size(); i++) {
                    ClientThread clientThread = clientThreads.get(i);
                    writeMessage((i + 1) + ") " + clientThread.getUsername() + " since " + connectionDate);
                }
            }
        }
    }

    /**
     * Method to read message from the client input stream.
     *
     * @return optional chat message
     */
    private Optional<ChatMessage> readMessage() {
        ChatMessage chatMessage;
        try {
            chatMessage = (ChatMessage) sInput.readObject();
        } catch (IOException e) {
            serverManager.displayMessage("Error reading streams of user with id " + id + ".Description: " + e.getMessage());
            return Optional.empty();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        return Optional.of(chatMessage);
    }

    /**
     * Method to close all streams.
     */
    public void closeStreams() {
        try {
            sInput.close();
            sOutput.close();
            socket.close();
        } catch (IOException e) {
            serverManager.displayMessage("Closing client data streams exception: " + e.getMessage());
        }
    }

    /**
     * Method to write to the client output stream.
     *
     * @param message message to write
     * @return true/false
     */
    public boolean writeMessage(String message) {
        if (!socket.isConnected()) {
            closeStreams();
            return false;
        }

        try {
            sOutput.writeObject(message);
        } catch (IOException e) {
            serverManager.displayMessage("Error sending message to " + username + ".Description: " + e.getMessage());
        }
        return true;
    }

    public String getUsername() {
        return username;
    }
}
