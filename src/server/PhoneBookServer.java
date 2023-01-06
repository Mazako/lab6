package server;

import model.PhoneBook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PhoneBookServer {
    public static final int PORT = 6666;
    private PhoneBook phoneBook = new PhoneBook();
    private ServerSocket serverSocket;

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Roczpoczęto działanie serwera na porcie:  " + serverSocket.getLocalPort());
        while (true) {
            ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
            new Thread(clientHandler).start();
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    private static class ClientHandler implements Runnable {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            System.out.println("Rozpoczęto połączenie z klientem: " + clientSocket.getInetAddress().getHostName());
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String userCommand;
                while ((userCommand = in.readLine()) != null) {
                    if ("bye".equalsIgnoreCase(userCommand)) {
                        out.println("Koniec.");
                        break;
                    }
//                    processCommand(userCommand);
                    out.println(userCommand);
                }
                clientSocket.close();
                out.close();
                in.close();
                System.out.println("Zakończono połączenie z klientem: " + clientSocket.getInetAddress().getHostName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void processCommand(String userCommand) {
            String[] commandAndParameters = userCommand.split(" ");
            String command = commandAndParameters[0];
            switch (command) {
                case "ADD":

            }

        }
    }
}
