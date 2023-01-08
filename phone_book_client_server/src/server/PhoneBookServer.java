/*
 *  Laboratorium 4
 *
 *   Autor: Michal Maziarz, 263913
 *    Data: Styczeń 2023 r.
 */
package server;

import model.PhoneBook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

public class PhoneBookServer {
    public static final int PORT = 6666;
    private final PhoneBook phoneBook = new PhoneBook();
    private ServerSocket serverSocket;

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        System.out.println("Roczpoczęto działanie serwera na porcie:  " + serverSocket.getLocalPort());
        while (!serverSocket.isClosed()) {
            try {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept(), phoneBook, serverSocket);
                new Thread(clientHandler).start();
            } catch (SocketException e) {

            }
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private final PhoneBook phoneBook;
        private final ServerSocket serverSocketCallback;
        public ClientHandler(Socket clientSocket, PhoneBook phoneBook, ServerSocket serverSocketCallback) {
            this.clientSocket = clientSocket;
            this.phoneBook = phoneBook;
            this.serverSocketCallback = serverSocketCallback;
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
                        out.println("Koniec połączenia");
                        out.println("STOP");
                        break;
                    }
                    System.out.println("Komenda uzytkownika: " + userCommand);
                    processCommand(userCommand);
                }
                System.out.println("Zakończono połączenie z klientem: " + clientSocket.getLocalAddress().getHostName());
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void processCommand(String userCommand) throws IOException {
            String[] commandAndParameters = userCommand.split(" ");
            String command = commandAndParameters[0];
            String[] params = commandAndParameters.length > 1 ?
                    Arrays.copyOfRange(commandAndParameters, 1, commandAndParameters.length) :
                    null;
            String response;
            switch (command) {
                case "PUT":
                    response = phoneBookPut(params);
                    break;
                case "LIST":
                    response = phoneBook.list();
                    break;
                case "DELETE":
                    response = phoneBookDelete(params);
                    break;
                case "GET":
                    response = phoneBookGet(params);
                    break;
                case "CLOSE":
                    response = "OK Zamykam połączenie z serwerem. Nowi userzy już się nie połączą";
                    serverSocketCallback.close();
                    break;
                case "REPLACE":
                    response = phoneBookReplace(params);
                    break;
                case "SAVE":
                    response = phoneBookSave(params);
                    break;
                case "LOAD":
                    response = phoneBookLoad(params);
                    break;
                case "FILES":
                    response = phoneBook.listFiles();
                    break;
                default:
                    response = "ERROR nieznana komenda";
                    break;
            }
            out.println(response);

        }

        private String phoneBookLoad(String[] params) {
            if (params == null || params.length != 1) {
                return badParamsMessage(PhoneBook.loadInstruction);
            }
            return phoneBook.load(params[0]);
        }

        private String phoneBookSave(String[] params) {
            if (params == null || params.length != 1) {
                return badParamsMessage(PhoneBook.saveInstruction);
            }
            return phoneBook.save(params[0]);
        }

        private String phoneBookReplace(String[] params) {
            if (params == null || params.length != 2) {
                return badParamsMessage(PhoneBook.replaceInstruction);
            }
            return phoneBook.replace(params[0], params[1]);
        }

        private String phoneBookGet(String[] params) {
            if (params == null || params.length != 1) {
                return badParamsMessage(PhoneBook.getInstruction);
            }
            return phoneBook.get(params[0]);
        }

        private String phoneBookDelete(String[] params) {
            if (params == null || params.length != 1) {
                return badParamsMessage(PhoneBook.deleteInstruction);
            }
            return phoneBook.delete(params[0]);
        }

        private String phoneBookPut(String[] params) {
            if (params == null || params.length != 2) {
                return badParamsMessage(PhoneBook.putInstruction);
            }
            return phoneBook.put(params[0], params[1]);
        }

        private String badParamsMessage(String instuction) {
            return "ERROR Zła ilość parametrów\n" +
                    "Instrukcja: " + instuction;
        }
    }
}
