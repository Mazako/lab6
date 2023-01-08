package server;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        new ActiveUsersTerminal().start();
    }
}
