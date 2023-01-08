package server;

import model.CommunicationSignals;
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
            long id = 0;
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                id = staticUserId++;
                CommunicationSignals signal;
                boolean closed = false;
                while (!closed) {
                    signal = (CommunicationSignals) in.readObject();
                    switch (signal) {
                        case SAVE_ME:
                            User user = (User) in.readObject();
                            activeUsers.put(id, user);
                            break;
                        case CLOSE_TERMINAL_CONNECTION:
                            out.writeObject(CommunicationSignals.BYE);
                            closed = true;
                            break;
                        case SEND_ACTIVE_USERS:
                            User[] users = activeUsers.values().toArray(User[]::new);
                            out.writeObject(users);
                    }
                }
                System.out.println("Koniec połączenia");
                activeUsers.remove(id);
                out.close();
                in.close();
                clientSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                activeUsers.remove(id);
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
                        .reduce("Aktywni użytkownicy: ", (subtotal, user) -> subtotal + " " + user);
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
