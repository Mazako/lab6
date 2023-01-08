package server;

import model.CommunicationSignals;
import model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ActiveUsersTerminal {
    public static final int PORT = 6666;

    public static final int ACTIVE_USERS_TIMEOUT_MILIS = 5000;
    private ServerSocket serverSocket;
    private final Map<Integer, User> activeUsers = new ConcurrentHashMap<>();

    private volatile boolean isServerRunning = true;
    private final AllowedPortsPool allowedPortsPool = new AllowedPortsPool();

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
            int activePort = 0;
            try {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                CommunicationSignals signal;
                boolean closed = false;
                while (!closed) {
                    signal = (CommunicationSignals) in.readObject();
                    switch (signal) {
                        case SAVE_ME:
                            User user = (User) in.readObject();
                            activePort = allowedPortsPool.getNextPort();
                            user.setPort(activePort);
                            activeUsers.put(activePort, user);
                            out.writeObject(activePort);
                            break;
                        case CLOSE_TERMINAL_CONNECTION:
                            out.writeObject(CommunicationSignals.BYE);
                            closed = true;
                            break;
                        case SEND_ACTIVE_USERS:
                            User[] users = activeUsers.values().toArray(User[]::new);
                            out.writeObject(users);
                            break;
                        case GET_USER_BY_PORT:
                            int port = (int) in.readObject();
                            System.out.println(port);
                            User searchedUser = activeUsers.get(port);
                            out.writeObject(searchedUser);
                            break;
                    }
                }
                System.out.println("Koniec połączenia");
                User removedUser = activeUsers.remove(activePort);
                allowedPortsPool.addPort(removedUser.getPort());
                out.close();
                in.close();
                clientSocket.close();
            } catch (IOException | ClassNotFoundException e) {
                activeUsers.remove(activePort);
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
