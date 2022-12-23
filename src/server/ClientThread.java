package server;

import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientThread implements Runnable{

    private Socket socket;
    private String name;
    private PhoneBookServer server;
    private ObjectOutputStream objectOutputStream;

    public ClientThread(PhoneBookServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {

    }
}
