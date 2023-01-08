package server;

import model.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ActiveUsersTerminal {
    public static final int PORT = 6666;

    private static long staticUserId = 1L;

    public static final int ACTIVE_USERS_TIMEOUT_MILIS = 5000;
    private ServerSocket serverSocket;
    private final Map<Long, User> activeUsers = new ConcurrentHashMap<>();

    private volatile boolean isServerRunning = true;

    public void start() throws IOException {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Uruchomiono terminal aktywnych użytkowników na porcie " + serverSocket.getLocalPort());
            new Thread(new ActiveUsersThread()).start();
            while (true) {
                ClientConnectionThread clientConnectionThread = new ClientConnectionThread(serverSocket.accept());
                new Thread(clientConnectionThread).start();
            }
        } catch (IOException e) {
            System.out.println("Wystąpił wyjątek serwera: " + e);
            System.out.println("Nasąpi zamknięcie serwera");
            stop();
        }
    }

    public void stop() throws IOException {
        isServerRunning = false;
        serverSocket.close();
    }

    private class ClientConnectionThread implements Runnable {
        private final Socket clientSocket;

        public ClientConnectionThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            System.out.println("Rozpoczęto połączenie z klientem: " + clientSocket.getInetAddress().getHostName());
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                User newUser = (User) in.readObject();
                long id = staticUserId++;
                activeUsers.put(id, newUser);
                while (true) {
                    ArrayList<User> activeUsersLocalCopy = new ArrayList<>(activeUsers.values());
                    out.writeObject(activeUsersLocalCopy);
                    Thread.sleep(5000);
                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ActiveUsersThread implements Runnable {
        @Override
        public void run() {
            while (isServerRunning) {
                String activeUsersInfo = activeUsers.values()
                        .stream()
                        .map(User::toString)
                        .reduce("Aktywni użytkownicy: ", (subtotal, user) -> subtotal + user);
                System.out.println(activeUsersInfo);
                try {
                    Thread.sleep(ACTIVE_USERS_TIMEOUT_MILIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
