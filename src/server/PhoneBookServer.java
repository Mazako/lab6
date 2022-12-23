package server;

import model.PhoneBook;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class PhoneBookServer implements Runnable {

    static final int PORT = 8080;

    private final PhoneBook phoneBook = new PhoneBook();

    public PhoneBookServer() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            String hostName = InetAddress.getLocalHost().getHostName();
            System.out.println("Serwer został uruchomiony na hoscie " + hostName);
            while (true) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    new ClientThread(this, socket);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
