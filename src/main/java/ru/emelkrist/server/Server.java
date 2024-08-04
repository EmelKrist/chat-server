package ru.emelkrist.server;

public class Server {
    private static int port = 1500;

    public static void main(String[] args) {
        initializeArguments(args);
        new ServerManager(port).runServer();
    }

    /**
     * Method to init input arguments (port).
     *
     * @param args array of arguments
     */
    private static void initializeArguments(String[] args) {
        switch (args.length) {
            case 1:
                try {
                    port = Integer.parseInt(args[0]);
                } catch (Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;
        }
    }
}
