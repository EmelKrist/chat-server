package ru.emelkrist.client;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ServerListener extends Thread {
    // server reader stream
    private ObjectInputStream sInput;
    private ClientManager clientManager;

    public ServerListener(ObjectInputStream sInput, ClientManager clientManager) {
        this.sInput = sInput;
        this.clientManager = clientManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String messageFromServer = sInput.readObject().toString();
                clientManager.displayMessage(messageFromServer);
            } catch (IOException e) {
                clientManager.displayMessage("Server has closed the connection");
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
