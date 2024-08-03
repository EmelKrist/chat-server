package ru.emelkrist.client;

import ru.emelkrist.models.ChatMessage;
import ru.emelkrist.utils.MessageType;

import java.util.Scanner;

public class Client {
    private static int port = 1500;
    private static String server = "localhost";
    private static String username = "Anonymous";

    public static void main(String[] args) {
        initializeArguments(args);
        run();
    }

    /**
     * Method for managing a client.
     */
    public static void run() {
        ClientManager client = new ClientManager(server, port, username);
        if (client.runClient()) {
            printWelcomeMessage();

            Scanner scan = new Scanner(System.in);
            while (true) {
                System.out.println("> ");
                String message = scan.nextLine();

                if (message.equalsIgnoreCase("LOGOUT")) {
                    client.sendMessage(new ChatMessage(MessageType.LOGOUT, ""));
                    break;
                } else if (message.equalsIgnoreCase("WHOISIN")) {
                    client.sendMessage(new ChatMessage(MessageType.WHOISIN, ""));
                } else {
                    client.sendMessage(new ChatMessage(MessageType.COMMUNICATION, message));
                }
            }

            scan.close();
            client.disconnect();
        }
    }

    /**
     * Method to init input arguments (server, port, username).
     *
     * @param args array of arguments
     */
    public static void initializeArguments(String[] args) {
        switch (args.length) {
            case 3: {
                // for > javac Client username port server
                server = args[2];
            }
            case 2: {
                // for > javac Client username port
                try {
                    port = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                    return;
                }
            }
            case 1: {
                // for > javac Client username
                username = args[0];
            }
            case 0: {
                // for > javac Client
                break;
            }
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
                return;
        }
    }

    /**
     * Method to print welcome message to the console.
     */
    private static void printWelcomeMessage() {
        System.out.println("\nHello.! Welcome to the chat-server.");
        System.out.println("Instructions:");
        System.out.println("1. Simply type the message to send broadcast to all active clients");
        System.out.println("2. Type '@username<space>yourmessage' without quotes to send message to desired client");
        System.out.println("3. Type 'WHOISIN' without quotes to see list of active clients");
        System.out.println("4. Type 'LOGOUT' without quotes to logoff from server");
    }
}
